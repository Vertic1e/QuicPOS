package com.quicpos.app.ui.backoffice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quicpos.app.data.repository.ItemRepository
import com.quicpos.app.data.repository.ReceiptRepository
import com.quicpos.app.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BackOfficeViewModel @Inject constructor(
    private val receiptRepository: ReceiptRepository,
    private val itemRepository: ItemRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackOfficeUiState())
    val uiState: StateFlow<BackOfficeUiState> = _uiState.asStateFlow()

    init {
        val today = System.currentTimeMillis()
        _uiState.update { it.copy(formattedDate = formatDate(today)) }
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = settingsRepository.getSettings()
            _uiState.update {
                it.copy(
                    currencyCode = settings.currencyCode,
                    currencySymbol = settings.currencySymbol
                )
            }
        }
    }

    fun onPinDigit(digit: String) {
        val current = _uiState.value.enteredPin
        if (current.length < 6) {
            val updated = current + digit
            _uiState.update { it.copy(enteredPin = updated, pinError = null) }
            if (updated.length == 6) {
                verifyPin(updated)
            }
        }
    }

    fun onPinDelete() {
        val current = _uiState.value.enteredPin
        if (current.isNotEmpty()) {
            _uiState.update { it.copy(enteredPin = current.dropLast(1), pinError = null) }
        }
    }

    fun onPinClear() {
        _uiState.update { it.copy(enteredPin = "", pinError = null) }
    }

    private fun verifyPin(pin: String) {
        viewModelScope.launch {
            val isValid = settingsRepository.verifyBackOfficePin(pin)
            if (isValid) {
                _uiState.update {
                    it.copy(
                        isAuthenticated = true,
                        enteredPin = "",
                        pinError = null
                    )
                }
                loadStatsForDate(_uiState.value.selectedDateMillis)
            } else {
                _uiState.update {
                    it.copy(
                        enteredPin = "",
                        pinError = "Invalid PIN. Try again."
                    )
                }
            }
        }
    }

    fun lockBackOffice() {
        _uiState.update {
            it.copy(
                isAuthenticated = false,
                enteredPin = "",
                pinError = null
            )
        }
    }

    fun setChangePinDialogOpen(open: Boolean) {
        _uiState.update { it.copy(isChangePinDialogOpen = open) }
    }

    fun updatePin(newPin: String) {
        viewModelScope.launch {
            settingsRepository.setBackOfficePin(newPin)
            _uiState.update { it.copy(isChangePinDialogOpen = false) }
        }
    }

    fun changeDate(deltaDays: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.selectedDateMillis
            add(Calendar.DAY_OF_YEAR, deltaDays)
        }
        val newMillis = cal.timeInMillis
        _uiState.update {
            it.copy(
                selectedDateMillis = newMillis,
                formattedDate = formatDate(newMillis),
                selectedHourTooltip = null
            )
        }
        loadStatsForDate(newMillis)
    }

    fun selectHourTooltip(hourlySale: HourlySale?) {
        _uiState.update { it.copy(selectedHourTooltip = hourlySale) }
    }

    fun loadStatsForDate(dateMillis: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Day bounds
            val cal = Calendar.getInstance().apply {
                timeInMillis = dateMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            // Prev day bounds
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val endOfPrevDay = cal.timeInMillis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfPrevDay = cal.timeInMillis

            // Query receipts
            val todayReceipts = receiptRepository.getReceiptsByDateRangeSync(startOfDay, endOfDay)
                .filter { it.status == "COMPLETED" }
            val prevReceipts = receiptRepository.getReceiptsByDateRangeSync(startOfPrevDay, endOfPrevDay)
                .filter { it.status == "COMPLETED" }

            val todayCount = todayReceipts.size
            val prevCount = prevReceipts.size
            val countDiff = if (prevCount > 0) ((todayCount - prevCount).toDouble() / prevCount) * 100.0 else 0.0

            val todayNet = todayReceipts.sumOf { it.totalAmount }
            val prevNet = prevReceipts.sumOf { it.totalAmount }
            val netDiff = if (prevNet > 0) ((todayNet - prevNet) / prevNet) * 100.0 else 0.0

            val todayAvg = if (todayCount > 0) todayNet / todayCount else 0.0
            val prevAvg = if (prevCount > 0) prevNet / prevCount else 0.0
            val avgDiff = if (prevAvg > 0) ((todayAvg - prevAvg) / prevAvg) * 100.0 else 0.0

            // Hourly distribution (0..23)
            val hourlyMap = mutableMapOf<Int, Double>()
            for (h in 0..23) hourlyMap[h] = 0.0

            val hourCal = Calendar.getInstance()
            for (r in todayReceipts) {
                hourCal.timeInMillis = r.timestamp
                val hour = hourCal.get(Calendar.HOUR_OF_DAY)
                hourlyMap[hour] = (hourlyMap[hour] ?: 0.0) + r.totalAmount
            }

            val hourlyList = (0..23).map { HourlySale(it, hourlyMap[it] ?: 0.0) }
            val maxHourly = hourlyList.maxOfOrNull { it.amount } ?: 0.0

            // Query item lines for today
            val todayLines = receiptRepository.getCompletedReceiptLinesInRange(startOfDay, endOfDay)
            val itemsGrouped = todayLines.groupBy { it.itemName }
                .map { (name, lines) ->
                    val totalQty = lines.sumOf { it.quantity }
                    val totalAmt = lines.sumOf { it.lineTotal }
                    ItemSaleSummary(
                        itemName = name,
                        quantity = totalQty,
                        totalAmount = totalAmt
                    )
                }
                .sortedByDescending { it.totalAmount }

            _uiState.update {
                it.copy(
                    receiptsCount = todayCount,
                    receiptsDiffPercent = countDiff,
                    netSales = todayNet,
                    netSalesDiffPercent = netDiff,
                    averageSale = todayAvg,
                    averageSaleDiffPercent = avgDiff,
                    hourlySales = hourlyList,
                    maxHourlySale = maxHourly,
                    itemsSold = itemsGrouped,
                    isLoading = false
                )
            }
        }
    }

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("d MMMM", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
