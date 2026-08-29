package com.quicpos.app.printing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

/**
 * TCP/IP connection for network ESC/POS thermal printers.
 * Connects directly to the printer's raw TCP socket (typically port 9100).
 */
class TcpPrinterConnection {

    companion object {
        private const val DEFAULT_PORT = 9100
        private const val CONNECTION_TIMEOUT_MS = 5000
        private const val MAX_RETRIES = 2
    }

    private var socket: Socket? = null
    private var outputStream: OutputStream? = null

    val isConnected: Boolean
        get() = socket?.isConnected == true && socket?.isClosed == false

    /**
     * Connect to a network printer by IP address and port.
     * @param ipAddress The IP address of the printer
     * @param port The TCP port (default 9100)
     */
    suspend fun connect(
        ipAddress: String,
        port: Int = DEFAULT_PORT
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            disconnect()

            var lastException: Exception? = null
            repeat(MAX_RETRIES) { attempt ->
                try {
                    val newSocket = Socket()
                    newSocket.connect(
                        InetSocketAddress(ipAddress, port),
                        CONNECTION_TIMEOUT_MS
                    )
                    socket = newSocket
                    outputStream = newSocket.getOutputStream()
                    return@withContext Result.success(Unit)
                } catch (e: IOException) {
                    lastException = e
                    socket?.close()
                    socket = null
                    if (attempt < MAX_RETRIES - 1) {
                        kotlinx.coroutines.delay(1000)
                    }
                }
            }

            Result.failure(lastException ?: IOException("Failed to connect"))
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
