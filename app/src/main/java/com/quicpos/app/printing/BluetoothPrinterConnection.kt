package com.quicpos.app.printing

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

/**
 * Bluetooth SPP (Serial Port Profile) connection for ESC/POS thermal printers.
 * Uses the standard SPP UUID for serial communication.
 */
class BluetoothPrinterConnection {

    companion object {
        // Standard SPP UUID
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val MAX_RETRIES = 3
    }

    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.isConnected == true

    /**
     * Get list of paired Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<Pair<String, String>> {
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter() ?: return emptyList()
            if (!adapter.isEnabled) return emptyList()
            adapter.bondedDevices?.map { device ->
                (device.name ?: "Unknown Bluetooth Device") to device.address
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Connect to a Bluetooth printer by MAC address.
     * @param macAddress The MAC address of the printer (e.g., "00:11:22:33:44:55")
     */
    @SuppressLint("MissingPermission")
    suspend fun connect(macAddress: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect() // Clean up any existing connection

            val adapter = BluetoothAdapter.getDefaultAdapter()
                ?: return@withContext Result.failure(IOException("Bluetooth not available"))

            if (!adapter.isEnabled) {
                return@withContext Result.failure(IOException("Bluetooth is disabled"))
            }

            val device: BluetoothDevice = adapter.getRemoteDevice(macAddress)

            var lastException: Exception? = null
            repeat(MAX_RETRIES) { attempt ->
                try {
                    socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                    adapter.cancelDiscovery() // Cancel discovery before connecting
                    socket?.connect()
                    outputStream = socket?.outputStream
                    return@withContext Result.success(Unit)
                } catch (e: IOException) {
                    lastException = e
                    socket?.close()
                    socket = null
                    if (attempt < MAX_RETRIES - 1) {
                        kotlinx.coroutines.delay(1000L * (attempt + 1)) // Backoff
                    }
                }
            }

            Result.failure(lastException ?: IOException("Failed to connect after $MAX_RETRIES attempts"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send raw bytes to the printer.
     */
    suspend fun write(data: ByteArray): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val stream = outputStream
                ?: return@withContext Result.failure(IOException("Not connected"))
            stream.write(data)
            stream.flush()
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    /**
     * Disconnect from the printer.
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            outputStream?.close()
            socket?.close()
        } catch (e: IOException) {
            // Ignore close errors
        } finally {
            outputStream = null
            socket = null
        }
    }
}
