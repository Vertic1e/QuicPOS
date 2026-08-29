package com.quicpos.app.printing

import android.graphics.Bitmap

/**
 * ESC/POS command bytes for thermal printer control.
 * Reference: ESC/POS Application Programming Guide
 */
object EscPosCommands {
    // Printer Control
    val INIT = byteArrayOf(0x1B, 0x40) // ESC @ - Initialize printer
    val LF = byteArrayOf(0x0A)         // Line feed
    val CR = byteArrayOf(0x0D)         // Carriage return
    val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x00) // GS V 0 - Full cut
    val CUT_PAPER_PARTIAL = byteArrayOf(0x1D, 0x56, 0x01) // GS V 1 - Partial cut
    val FEED_AND_CUT = byteArrayOf(0x1D, 0x56, 0x41, 0x03) // Feed 3 lines + cut

    // Text Formatting
    val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)  // ESC E 1
    val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00) // ESC E 0
    val UNDERLINE_ON = byteArrayOf(0x1B, 0x2D, 0x01)  // ESC - 1
    val UNDERLINE_OFF = byteArrayOf(0x1B, 0x2D, 0x00) // ESC - 0

    // Text Size
    val TEXT_NORMAL = byteArrayOf(0x1D, 0x21, 0x00)     // GS ! 0 - Normal
    val TEXT_DOUBLE_HEIGHT = byteArrayOf(0x1D, 0x21, 0x01) // GS ! 1
    val TEXT_DOUBLE_WIDTH = byteArrayOf(0x1D, 0x21, 0x10)  // GS ! 16
    val TEXT_DOUBLE_SIZE = byteArrayOf(0x1D, 0x21, 0x11)   // GS ! 17 - Double width+height

    // Text Alignment
    val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)   // ESC a 0
    val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01) // ESC a 1
    val ALIGN_RIGHT = byteArrayOf(0x1B, 0x61, 0x02)  // ESC a 2

    // Cash Drawer
    val OPEN_CASH_DRAWER = byteArrayOf(0x1B, 0x70, 0x00, 0x19, (0xFF).toByte())

    // Feed
    fun feedLines(n: Int): ByteArray = byteArrayOf(0x1B, 0x64, n.toByte()) // ESC d n

    // Charset
    val CHARSET_UTF8 = byteArrayOf(0x1B, 0x74, 0x00) // ESC t 0

    /**
     * Build a complete byte array from multiple commands and text.
     */
    fun buildCommand(vararg parts: ByteArray): ByteArray {
        val totalSize = parts.sumOf { it.size }
        val result = ByteArray(totalSize)
        var offset = 0
        for (part in parts) {
            System.arraycopy(part, 0, result, offset, part.size)
            offset += part.size
        }
        return result
    }

    /**
     * Convert text to bytes using the printer's charset.
     */
    fun textToBytes(text: String): ByteArray {
        return text.toByteArray(Charsets.UTF_8)
    }

    /**
     * Convert an Android Bitmap into ESC/POS GS v 0 raster bit image bytes.
     * Compatible with 58mm (384 dots) and 80mm (576 dots) thermal printers.
     */
    fun bitmapToRasterBitImage(bitmap: Bitmap, maxWidth: Int = 384): ByteArray {
        val targetWidth = if (bitmap.width > maxWidth) maxWidth else (bitmap.width / 8) * 8
        val width = if (targetWidth <= 0) 8 else targetWidth
        val height = ((bitmap.height.toDouble() / bitmap.width.toDouble()) * width).toInt().coerceAtLeast(8)
        
        val scaled = Bitmap.createScaledBitmap(bitmap, width, height, true)
        val widthBytes = (width + 7) / 8
        val xL = (widthBytes % 256).toByte()
        val xH = (widthBytes / 256).toByte()
        val yL = (height % 256).toByte()
        val yH = (height / 256).toByte()

        // GS v 0 m xL xH yL yH
        val header = byteArrayOf(
            0x1B, 0x61, 0x01, // Center align
            0x1D, 0x76, 0x30, 0x00,
            xL, xH, yL, yH
        )

        val imageBytes = ByteArray(widthBytes * height)
        var byteIdx = 0

        for (y in 0 until height) {
            for (xByte in 0 until widthBytes) {
                var b = 0
                for (bit in 0 until 8) {
                    val x = xByte * 8 + bit
                    if (x < width) {
                        val pixel = scaled.getPixel(x, y)
                        val alpha = (pixel shr 24) and 0xFF
                        val r = (pixel shr 16) and 0xFF
                        val g = (pixel shr 8) and 0xFF
                        val bVal = pixel and 0xFF
                        val luminance = (0.299 * r + 0.587 * g + 0.114 * bVal)
                        // Black dot if pixel is dark enough and not transparent
                        if (alpha > 100 && luminance < 170) {
                            b = b or (1 shl (7 - bit))
                        }
                    }
                }
                imageBytes[byteIdx++] = b.toByte()
            }
        }

        return buildCommand(header, imageBytes, LF)
    }
}
