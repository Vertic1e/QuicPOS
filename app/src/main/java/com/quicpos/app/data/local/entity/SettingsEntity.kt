package com.quicpos.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val key: String,
    val value: String
) {
    companion object {
        // Setting Keys
        const val KEY_BUSINESS_NAME = "business_name"
        const val KEY_POS_REGISTER_NAME = "pos_register_name"
        const val KEY_CURRENCY_CODE = "currency_code"
        const val KEY_CURRENCY_SYMBOL = "currency_symbol"
        const val KEY_DARK_MODE = "dark_mode"
        const val KEY_THEME = "theme"
        const val KEY_LAYOUT_MODE = "layout_mode" // "GRID" or "LIST"
        const val KEY_LANGUAGE = "language"
        const val KEY_USE_BARCODE_SCANNER = "use_barcode_scanner"

        // Printer Settings
        const val KEY_PRINTER_NAME = "printer_name"
        const val KEY_PRINTER_MODEL = "printer_model"
        const val KEY_PRINTER_TYPE = "printer_type" // "BUILT_IN", "BLUETOOTH", "TCP"
        const val KEY_PRINTER_ADDRESS = "printer_address"
        const val KEY_PRINTER_PORT = "printer_port"
        const val KEY_PAPER_WIDTH = "paper_width" // "58mm", "80mm"
        const val KEY_PRINT_MODE = "print_mode" // "STANDARD", "RASTER", "RAW"
        const val KEY_ESC_INIT_CMD = "esc_init_cmd"
        const val KEY_ESC_CUT_CMD = "esc_cut_cmd"
        const val KEY_ESC_DRAWER_CMD = "esc_drawer_cmd"

        const val KEY_RECEIPT_HEADER = "receipt_header"
        const val KEY_RECEIPT_FOOTER = "receipt_footer"
        const val KEY_RECEIPT_LOGO_URI = "receipt_logo_uri"
        const val KEY_RECEIPT_SHOW_LOGO = "receipt_show_logo"
        const val KEY_LOCK_PIN = "lock_pin"
        const val KEY_RECEIPT_COUNTER = "receipt_counter"
        const val KEY_DUAL_CURRENCY_ENABLED = "dual_currency_enabled"
        const val KEY_SECONDARY_CURRENCY_CODE = "secondary_currency_code"
        const val KEY_SECONDARY_CURRENCY_SYMBOL = "secondary_currency_symbol"
        const val KEY_EXCHANGE_RATE = "exchange_rate" // e.g. 4000 KHR = 1 USD
        const val KEY_ITEM_SIZE = "item_size" // "SMALL", "MEDIUM", "LARGE"
        const val KEY_GRID_COLUMNS = "grid_columns" // 2, 3, 4, 0 (0 = Auto)
        const val KEY_FONT_SIZE_SCALE = "font_size_scale" // 0.8 - 1.8
        const val KEY_BACK_OFFICE_PIN = "back_office_pin"

        // Default Values
        const val DEFAULT_BUSINESS_NAME = "QuicPOS Store"
        const val DEFAULT_POS_REGISTER = "POS 1"
        const val DEFAULT_CURRENCY_CODE = "KHR"
        const val DEFAULT_CURRENCY_SYMBOL = "៛"
        const val DEFAULT_DUAL_CURRENCY_ENABLED = "true"
        const val DEFAULT_SECONDARY_CURRENCY_CODE = "USD"
        const val DEFAULT_SECONDARY_CURRENCY_SYMBOL = "$"
        const val DEFAULT_EXCHANGE_RATE = "4000.0"
        const val DEFAULT_DARK_MODE = "true"
        const val DEFAULT_THEME = "NIGHT_BLUE"
        const val DEFAULT_LAYOUT_MODE = "GRID"
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_USE_SCANNER = "true"

        const val DEFAULT_PRINTER_NAME = "Main Thermal Printer"
        const val DEFAULT_PRINTER_MODEL = "Generic ESC/POS"
        const val DEFAULT_PRINTER_TYPE = "BUILT_IN"
        const val DEFAULT_PRINTER_PORT = "9100"
        const val DEFAULT_PAPER_WIDTH = "58mm"
        const val DEFAULT_PRINT_MODE = "STANDARD"
        const val DEFAULT_ESC_INIT_CMD = "1B40"
        const val DEFAULT_ESC_CUT_CMD = "1D5601"
        const val DEFAULT_ESC_DRAWER_CMD = "1B700019FF"

        const val DEFAULT_RECEIPT_HEADER = "Thank you for shopping!"
        const val DEFAULT_RECEIPT_FOOTER = "Please come again"
        const val DEFAULT_RECEIPT_LOGO_URI = ""
        const val DEFAULT_RECEIPT_SHOW_LOGO = "false"
        const val DEFAULT_RECEIPT_COUNTER = "0"
        const val DEFAULT_ITEM_SIZE = "MEDIUM"
        const val DEFAULT_GRID_COLUMNS = "3"
        const val DEFAULT_FONT_SIZE_SCALE = "1.0"
        const val DEFAULT_BACK_OFFICE_PIN = "000000"
    }
}
