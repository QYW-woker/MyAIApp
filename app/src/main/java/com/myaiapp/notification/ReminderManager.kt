package com.myaiapp.notification

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.myaiapp.MainActivity
import com.myaiapp.R
import com.myaiapp.data.local.model.Reminder
import com.myaiapp.data.local.model.ReminderType
import java.util.Calendar

/**
 * 提醒管理器
 * 负责创建、调度和取消提醒通知
 */
class ReminderManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID_RECORD = "record_reminder"
        const val CHANNEL_ID_BUDGET = "budget_reminder"
        const val CHANNEL_ID_SAVINGS = "savings_reminder"
        const val CHANNEL_ID_CREDIT = "credit_reminder"

        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_REMINDER_TYPE = "reminder_type"

        private const val REQUEST_CODE_BASE = 10000
    }

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createNotificationChannels()
    }

    /**
     * 创建通知渠道
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_ID_RECORD,
                    "记账提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "每日记账提醒通知"
                },
                NotificationChannel(
                    CHANNEL_ID_BUDGET,
                    "预算提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "预算超支提醒通知"
                },
                NotificationChannel(
                    CHANNEL_ID_SAVINGS,
                    "存钱提醒",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "存钱计划提醒通知"
                },
                NotificationChannel(
                    CHANNEL_ID_CREDIT,
                    "还款提醒",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "信用卡还款提醒通知"
                }
            )

            val manager = context.getSystemService(NotificationManager::class.java)
            channels.forEach { manager.createNotificationChannel(it) }
        }
    }

    /**
     * 调度每日记账提醒
     */
    fun scheduleDailyReminder(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // 如果设定的时间已过，调度到明天
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DAILY_REMINDER
            putExtra(EXTRA_REMINDER_TYPE, ReminderType.RECORD.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 设置每日重复闹钟
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    /**
     * 取消每日记账提醒
     */
    fun cancelDailyReminder() {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_DAILY_REMINDER
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_BASE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * 调度单次提醒
     */
    fun scheduleReminder(reminder: Reminder) {
        if (!reminder.isEnabled) return

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SINGLE_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
            putExtra(EXTRA_REMINDER_TYPE, reminder.type.name)
        }

        val requestCode = reminder.id.hashCode() + REQUEST_CODE_BASE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        when (reminder.repeatType) {
            "DAILY" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminder.time,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
            "WEEKLY" -> {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    reminder.time,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
            }
            "MONTHLY" -> {
                // 月度提醒需要特殊处理，使用精确闹钟
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.time,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminder.time,
                        pendingIntent
                    )
                }
            }
            else -> {
                // 单次提醒
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.time,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        reminder.time,
                        pendingIntent
                    )
                }
            }
        }
    }

    /**
     * 取消提醒
     */
    fun cancelReminder(reminder: Reminder) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_SINGLE_REMINDER
            putExtra(EXTRA_REMINDER_ID, reminder.id)
        }

        val requestCode = reminder.id.hashCode() + REQUEST_CODE_BASE

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    /**
     * 显示通知
     */
    fun showNotification(
        id: Int,
        title: String,
        content: String,
        channelId: String = CHANNEL_ID_RECORD
    ) {
        // 点击通知打开应用
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // 检查通知权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        notificationManager.notify(id, notification)
    }

    /**
     * 显示预算超支通知
     */
    fun showBudgetAlert(budgetName: String, percentage: Int) {
        showNotification(
            id = "budget_alert".hashCode(),
            title = "预算提醒",
            content = "$budgetName 已使用 $percentage%，请注意控制支出",
            channelId = CHANNEL_ID_BUDGET
        )
    }

    /**
     * 显示信用卡还款提醒
     */
    fun showCreditCardReminder(cardName: String, amount: Double, dueDate: String) {
        showNotification(
            id = "credit_$cardName".hashCode(),
            title = "信用卡还款提醒",
            content = "$cardName 需在 $dueDate 前还款 ¥${"%.2f".format(amount)}",
            channelId = CHANNEL_ID_CREDIT
        )
    }

    /**
     * 显示存钱提醒
     */
    fun showSavingsReminder(planName: String, amount: Double) {
        showNotification(
            id = "savings_$planName".hashCode(),
            title = "存钱提醒",
            content = "今天该往「$planName」存入 ¥${"%.2f".format(amount)} 啦",
            channelId = CHANNEL_ID_SAVINGS
        )
    }
}
