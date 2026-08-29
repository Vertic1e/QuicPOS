package com.quicpos.app.printing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.quicpos.app.data.repository.SettingsRepository
import com.quicpos.app.domain.model.Receipt
import com.quicpos.app.domain.model.TicketLine
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.InetSocketAddress
import java.net.Socket
import javax.inject.Inject
import javax.inject.Singleton

data class DiscoveredPrinter(
    val name: String,
    val address: String,
    val type: String, // "BLUETOOTH", "TCP"
    val port: Int = 9100,
    val isPaired: Boolean = false
)

/**
 * Unified printer manager that supports Built-in (Sunmi / Internal Thermal / Serial),
 * Bluetooth, and Network (TCP) printers.
 * Handles device discovery, connection lifecycle, receipt formatting, and logo bit image rasterization.
 */
@Singleton
class PrinterManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {
    private val bluetoothConnection = BluetoothPrinterConnection()
    private val tcpConnection = TcpPrinterConnection()
    private val printMutex = Mutex()

    sealed class PrinterState {
        data object Disconnected : PrinterState()
        data object Connecting : PrinterState()
        data object Connected : PrinterState()
        data class Error(val message: String) : PrinterState()
    }

    var state: PrinterState = PrinterState.Disconnected
        private set

    val isConnected: Boolean
        get() = state is PrinterState.Connected

    /**
     * Search for nearby available or paired printers.
     */
    suspend fun searchPrinters(type: String): List<DiscoveredPrinter> = withContext(Dispatchers.IO) {
        val result = mutableListOf<DiscoveredPrinter>()

        if (type == "BLUETOOTH" || type == "ALL") {
            val paired = bluetoothConnection.getPairedDevices()
            for ((name, mac) in paired) {
                result.add(
                    DiscoveredPrinter(
                        name = name,
                        address = mac,
                        type = "BLUETOOTH",
                        isPaired = true
                    )
                )
            }
        }

        if (type == "TCP" || type == "ALL") {
            // Quick subnet probe on common POS printer IPs (192.168.1.x, port 9100)
            val ipProbes = (100..115).map { "192.168.1.$it" } + listOf("192.168.0.100", "192.168.0.101", "10.0.2.2")
            val networkPrinters = probeNetworkPrinters(ipProbes, port = 9100)
            result.addAll(networkPrinters)
        }

        result
    }

    private suspend fun probeNetworkPrinters(ipList: List<String>, port: Int): List<DiscoveredPrinter> = coroutineScope {
        ipList.map { ip ->
            async(Dispatchers.IO) {
                try {
                    Socket().use { socket ->
                        socket.connect(InetSocketAddress(ip, port), 250)
                        DiscoveredPrinter(
                            name = "Network ESC/POS Printer ($ip)",
                            address = ip,
                            port = port,
                            type = "TCP"
                        )
                    }
                } catch (e: Exception) {
                    null
                }
            }
        }.awaitAll().filterNotNull()
    }

    /**
     * Connect to the configured printer.
     */
    suspend fun connect(): Result<Unit> {
        state = PrinterState.Connecting

        val printerType = settingsRepository.getPrinterType()
        val printerAddress = settingsRepository.getPrinterAddress()
        val printerPort = settingsRepository.getPrinterPort()

        if (printerType == "BUILT_IN") {
            // Built-in printer is always ready
            state = PrinterState.Connected
            return Result.success(Unit)
        }

        if (printerAddress.isBlank()) {
            state = PrinterState.Error("No printer configured")
            return Result.failure(IllegalStateException("No printer configured"))
        }

        val result = when (printerType) {
            "BLUETOOTH" -> bluetoothConnection.connect(printerAddress)
            "TCP" -> tcpConnection.connect(printerAddress, printerPort)
            else -> {
                state = PrinterState.Error("Unknown printer type: $printerType")
                return Result.failure(IllegalStateException("Unknown printer type"))
            }
        }

        state = if (result.isSuccess) {
            PrinterState.Connected
        } else {
            PrinterState.Error(result.exceptionOrNull()?.message ?: "Connection failed")
        }

        return result
    }

    /**
     * Disconnect from the printer.
     */
    suspend fun disconnect() {
        bluetoothConnection.disconnect()
        tcpConnection.disconnect()
        state = PrinterState.Disconnected
    }

    /**
     * Print a formatted receipt.
     */
    suspend fun printReceipt(
        receipt: Receipt,
        lines: List<TicketLine>,
        businessName: String? = null
    ): Result<Unit> = printMutex.withLock {
        try {
            // Auto-connect if needed
            if (!isConnected) {
                val connectResult = connect()
                if (connectResult.isFailure) return connectResult
            }

            val name = businessName ?: settingsRepository.getBusinessName()
            val header = settingsRepository.getReceiptHeader()
            val footer = settingsRepository.getReceiptFooter()
            val settings = settingsRepository.getSettings()

            val logoBitmap = if (settings.showReceiptLogo && settings.receiptLogoUri.isNotBlank()) {
                loadLogoBitmap(settings.receiptLogoUri)
            } else null

            val formatter = ReceiptFormatter(charWidth = settings.charWidth)

            val data = formatter.formatReceipt(
                receipt = receipt,
                lines = lines,
                businessName = name,
                headerText = header,
                footerText = footer,
                currencyCode = settings.currencyCode,
                isDualCurrencyEnabled = settings.isDualCurrencyEnabled,
                secondaryCurrencyCode = settings.secondaryCurrencyCode,
                exchangeRate = settings.exchangeRate,
                logoBitmap = logoBitmap,
                customInitCmd = settings.escInitCmd,
                customCutCmd = settings.escCutCmd
            )

            sendRawData(data)
        } catch (e: Exception) {
            state = PrinterState.Error(e.message ?: "Print failed")
            Result.failure(e)
        }
    }

    /**
     * Send raw ESC/POS data to the printer.
     */
    suspend fun sendRawData(data: ByteArray): Result<Unit> {
        val printerType = settingsRepository.getPrinterType()
        return when (printerType) {
            "BUILT_IN" -> sendToBuiltInPrinter(data)
            "BLUETOOTH" -> bluetoothConnection.write(data)
            "TCP" -> tcpConnection.write(data)
            else -> Result.failure(IllegalStateException("No printer configured"))
        }
    }

    /**
     * Direct print to built-in thermal printer / Sunmi / serial device.
     */
    private suspend fun sendToBuiltInPrinter(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check common internal thermal printer device paths (Sunmi / Telpo / POS terminals)
            val devPaths = listOf("/dev/ttyGS0", "/dev/ttyS1", "/dev/ttyS0", "/dev/usb/lp0", "/dev/lp0")
            var printed = false
            for (path in devPaths) {
                val file = File(path)
                if (file.exists() && file.canWrite()) {
                    FileOutputStream(file).use { fos ->
                        fos.write(data)
                        fos.flush()
                    }
                    printed = true
                    break
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun loadLogoBitmap(uriString: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                val uri = Uri.parse(uriString)
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            } else if (uriString.startsWith("/")) {
                BitmapFactory.decodeFile(uriString)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Send a test print.
     */
    suspend fun testPrint(): Result<Unit> {
        val settings = settingsRepository.getSettings()
        val logoBitmap = if (settings.showReceiptLogo && settings.receiptLogoUri.isNotBlank()) {
            loadLogoBitmap(settings.receiptLogoUri)
        } else null

        val logoBytes = if (logoBitmap != null) {
            EscPosCommands.bitmapToRasterBitImage(logoBitmap, if (settings.paperWidth == "80mm") 512 else 384)
        } else byteArrayOf()

        val initBytes = EscPosCommands.hexToBytes(settings.escInitCmd).takeIf { it.isNotEmpty() }
            ?: EscPosCommands.INIT
        val cutBytes = EscPosCommands.hexToBytes(settings.escCutCmd).takeIf { it.isNotEmpty() }
            ?: EscPosCommands.CUT_PAPER_PARTIAL

        val divider = if (settings.paperWidth == "80mm") "────────────────────────────────────────────────" else "────────────────────────────────"

        val data = EscPosCommands.buildCommand(
            initBytes,
            logoBytes,
            EscPosCommands.ALIGN_CENTER,
            EscPosCommands.TEXT_DOUBLE_SIZE,
            EscPosCommands.BOLD_ON,
            EscPosCommands.textToBytes(settings.businessName),
            EscPosCommands.LF,
            EscPosCommands.TEXT_NORMAL,
            EscPosCommands.BOLD_OFF,
            EscPosCommands.textToBytes("Test Print Successful"),
            EscPosCommands.LF,
            EscPosCommands.textToBytes(divider),
            EscPosCommands.LF,
            EscPosCommands.textToBytes("Printer: ${settings.printerName}"),
            EscPosCommands.LF,
            EscPosCommands.textToBytes("Model: ${settings.printerModel} (${settings.paperWidth})"),
            EscPosCommands.LF,
            EscPosCommands.textToBytes("Mode: ${settings.printerType}"),
            EscPosCommands.LF,
            EscPosCommands.textToBytes("Printer communication is active."),
            EscPosCommands.LF,
            EscPosCommands.feedLines(4),
            cutBytes
        )

        return sendRawData(data)
    }

    /**
     * Open the connected cash drawer.
     */
    suspend fun openCashDrawer(): Result<Unit> {
        val settings = settingsRepository.getSettings()
        val drawerBytes = EscPosCommands.hexToBytes(settings.escDrawerCmd).takeIf { it.isNotEmpty() }
            ?: EscPosCommands.OPEN_CASH_DRAWER
        return sendRawData(drawerBytes)
    }
}
