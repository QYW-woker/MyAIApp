package com.myaiapp.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.myaiapp.data.local.model.ReminderType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 提醒广播接收器
 * 接收闹钟触发的广播并显示通知
 */
class ReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_REMINDER = "com.myaiapp.DAILY_REMINDER"
        const val ACTION_SINGLE_REMINDER = "com.myaiapp.SINGLE_REMINDER"
        const val ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val reminderManager = ReminderManager(context)

        when (intent.action) {
            ACTION_DAILY_REMINDER -> {
                handleDailyReminder(context, reminderManager)
            }
            ACTION_SINGLE_REMINDER -> {
                handleSingleReminder(context, intent, reminderManager)
            }
            ACTION_BOOT_COMPLETED -> {
                // 设备重启后重新调度所有提醒
                rescheduleAllReminders(context)
            }
        }
    }

    private fun handleDailyReminder(context: Context, reminderManager: ReminderManager) {
        // 显示每日记账提醒
        val messages = listOf(
            "今天的账记了吗？花点时间记录一下吧~",
            "别忘了记账哦，养成好习惯从今天开始",
            "记账时间到！看看今天花了多少钱",
            "温馨提示：该记账啦，钱都花哪儿了？",
            "每日一记，理财有道！"
        )

        reminderManager.showNotification(
            id = "daily_reminder".hashCode(),
            title = "记账提醒",
            content = messages.random(),
            channelId = ReminderManager.CHANNEL_ID_RECORD
        )
    }

    private fun handleSingleReminder(
        context: Context,
        intent: Intent,
        reminderManager: ReminderManager
    ) {
        val reminderId = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_ID)
        val reminderTypeStr = intent.getStringExtra(ReminderManager.EXTRA_REMINDER_TYPE)
        val reminderType = try {
            ReminderType.valueOf(reminderTypeStr ?: "CUSTOM")
        } catch (e: Exception) {
            ReminderType.CUSTOM
        }

        when (reminderType) {
            ReminderType.RECORD -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "记账提醒",
                    content = "该记账了，不要忘记今天的开支哦~"
                )
            }
            ReminderType.CREDIT_CARD -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "信用卡还款提醒",
                    content = "信用卡还款日快到了，请及时还款",
                    channelId = ReminderManager.CHANNEL_ID_CREDIT
                )
            }
            ReminderType.BUDGET -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "预算提醒",
                    content = "请关注您的预算使用情况",
                    channelId = ReminderManager.CHANNEL_ID_BUDGET
                )
            }
            ReminderType.SAVINGS -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "存钱提醒",
                    content = "今天是存钱日，继续坚持存钱计划吧！",
                    channelId = ReminderManager.CHANNEL_ID_SAVINGS
                )
            }
            ReminderType.DEBT -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "还款提醒",
                    content = "还款日期快到了，请及时还款"
                )
            }
            ReminderType.CUSTOM -> {
                reminderManager.showNotification(
                    id = reminderId?.hashCode() ?: 0,
                    title = "提醒",
                    content = "您设置的提醒时间到了"
                )
            }
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storageManager = com.myaiapp.data.local.FileStorageManager(context)
                val settings = storageManager.getSettings()
                val reminderManager = ReminderManager(context)

                // 重新调度每日提醒（如果启用）
                // 这里假设用户设置了每日提醒时间，实际需要从设置中读取
                // reminderManager.scheduleDailyReminder(20, 0) // 默认晚上8点

                // 重新调度所有自定义提醒
                val reminders = storageManager.getReminders()
                reminders.filter { it.isEnabled && it.time > System.currentTimeMillis() }
                    .forEach { reminder ->
                        reminderManager.scheduleReminder(reminder)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
