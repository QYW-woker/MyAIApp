package com.myaiapp.ui.screens.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myaiapp.data.local.FileStorageManager
import com.myaiapp.data.local.model.Reminder
import com.myaiapp.data.local.model.ReminderType
import com.myaiapp.notification.ReminderManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ReminderSettingsUiState(
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderHour: Int = 20,
    val dailyReminderMinute: Int = 0,
    val budgetAlertEnabled: Boolean = true,
    val savingsReminderEnabled: Boolean = false,
    val customReminders: List<Reminder> = emptyList()
)

class ReminderSettingsViewModel(
    private val context: Context
) : ViewModel() {

    private val storageManager = FileStorageManager(context)
    private val reminderManager = ReminderManager(context)

    private val _uiState = MutableStateFlow(ReminderSettingsUiState())
    val uiState: StateFlow<ReminderSettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
        loadReminders()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val settings = storageManager.getSettings()
                _uiState.update {
                    it.copy(
                        budgetAlertEnabled = settings.enableBudgetAlert,
                        dailyReminderEnabled = settings.enableNotifications
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadReminders() {
        viewModelScope.launch {
            try {
                val reminders = storageManager.getReminders()
                    .filter { it.type != ReminderType.RECORD }
                    .sortedBy { it.time }
                _uiState.update { it.copy(customReminders = reminders) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDailyReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(dailyReminderEnabled = enabled) }

            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(enableNotifications = enabled))

                if (enabled) {
                    val state = _uiState.value
                    reminderManager.scheduleDailyReminder(
                        state.dailyReminderHour,
                        state.dailyReminderMinute
                    )
                } else {
                    reminderManager.cancelDailyReminder()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    dailyReminderHour = hour,
                    dailyReminderMinute = minute
                )
            }

            // 如果提醒已启用，重新调度
            if (_uiState.value.dailyReminderEnabled) {
                reminderManager.scheduleDailyReminder(hour, minute)
            }
        }
    }

    fun setBudgetAlertEnabled(enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(budgetAlertEnabled = enabled) }

            try {
                val settings = storageManager.getSettings()
                storageManager.saveSettings(settings.copy(enableBudgetAlert = enabled))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSavingsReminderEnabled(enabled: Boolean) {
        _uiState.update { it.copy(savingsReminderEnabled = enabled) }
    }

    fun addReminder(title: String, type: ReminderType, time: Long, repeatType: String) {
        viewModelScope.launch {
            try {
                val reminder = Reminder(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    title = title,
                    content = "",
                    time = time,
                    repeatType = repeatType,
                    isEnabled = true
                )

                storageManager.addReminder(reminder)
                reminderManager.scheduleReminder(reminder)
                loadReminders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                val updatedReminder = reminder.copy(isEnabled = !reminder.isEnabled)
                val reminders = storageManager.getReminders().toMutableList()
                val index = reminders.indexOfFirst { it.id == reminder.id }
                if (index >= 0) {
                    reminders[index] = updatedReminder
                    storageManager.saveReminders(reminders)

                    if (updatedReminder.isEnabled) {
                        reminderManager.scheduleReminder(updatedReminder)
                    } else {
                        reminderManager.cancelReminder(updatedReminder)
                    }

                    loadReminders()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            try {
                reminderManager.cancelReminder(reminder)
                storageManager.deleteReminder(reminder.id)
                loadReminders()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

class ReminderSettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReminderSettingsViewModel(context.applicationContext) as T
    }
}
