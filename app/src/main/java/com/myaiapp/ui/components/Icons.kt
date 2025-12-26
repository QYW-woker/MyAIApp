package com.myaiapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 图标映射器
 * 将字符串图标名称映射到Material Icons
 */
object IconMapper {
    private val iconMap = mapOf(
        // 分类图标
        "restaurant" to Icons.Outlined.Restaurant,
        "shopping_bag" to Icons.Outlined.ShoppingBag,
        "train" to Icons.Outlined.Train,
        "gamepad" to Icons.Outlined.SportsEsports,
        "home" to Icons.Outlined.Home,
        "activity" to Icons.Outlined.LocalHospital,
        "graduation_cap" to Icons.Outlined.School,
        "phone" to Icons.Outlined.Phone,
        "sparkles" to Icons.Outlined.AutoAwesome,
        "dumbbell" to Icons.Outlined.FitnessCenter,
        "users" to Icons.Outlined.People,
        "plane" to Icons.Outlined.Flight,
        "paw_print" to Icons.Outlined.Pets,
        "gift" to Icons.Outlined.CardGiftcard,
        "more_horizontal" to Icons.Outlined.MoreHoriz,
        "briefcase" to Icons.Outlined.Work,
        "award" to Icons.Outlined.EmojiEvents,
        "laptop" to Icons.Outlined.Laptop,
        "trending_up" to Icons.Outlined.TrendingUp,
        "landmark" to Icons.Outlined.AccountBalance,
        "heart" to Icons.Outlined.Favorite,
        "rotate_ccw" to Icons.Outlined.Refresh,

        // 资产账户图标
        "wallet" to Icons.Outlined.AccountBalanceWallet,
        "credit_card" to Icons.Outlined.CreditCard,
        "smartphone" to Icons.Outlined.Smartphone,
        "message_circle" to Icons.Outlined.Chat,
        "bank" to Icons.Outlined.AccountBalance,
        "chart" to Icons.Outlined.ShowChart,
        "money" to Icons.Outlined.AttachMoney,

        // 导航图标
        "book" to Icons.Outlined.Book,
        "calendar" to Icons.Outlined.CalendarMonth,
        "settings" to Icons.Outlined.Settings,
        "pie_chart" to Icons.Outlined.PieChart,
        "receipt" to Icons.AutoMirrored.Outlined.ReceiptLong,
        "add" to Icons.Outlined.Add,
        "close" to Icons.Outlined.Close,
        "check" to Icons.Outlined.Check,
        "arrow_back" to Icons.Outlined.ArrowBack,
        "arrow_forward" to Icons.Outlined.ArrowForward,
        "edit" to Icons.Outlined.Edit,
        "delete" to Icons.Outlined.Delete,
        "search" to Icons.Outlined.Search,
        "filter" to Icons.Outlined.FilterList,
        "sort" to Icons.Outlined.Sort,
        "share" to Icons.Outlined.Share,
        "download" to Icons.Outlined.Download,
        "upload" to Icons.Outlined.Upload,
        "camera" to Icons.Outlined.CameraAlt,
        "image" to Icons.Outlined.Image,
        "mic" to Icons.Outlined.Mic,
        "notification" to Icons.Outlined.Notifications,
        "lock" to Icons.Outlined.Lock,
        "fingerprint" to Icons.Outlined.Fingerprint,
        "info" to Icons.Outlined.Info,
        "help" to Icons.Outlined.Help,
        "warning" to Icons.Outlined.Warning,
        "error" to Icons.Outlined.Error,

        // 其他
        "savings" to Icons.Outlined.Savings,
        "target" to Icons.Outlined.TrackChanges,
        "repeat" to Icons.Outlined.Repeat,
        "tag" to Icons.Outlined.Label,
        "note" to Icons.Outlined.Note,
        "attach" to Icons.Outlined.AttachFile,
        "clock" to Icons.Outlined.Schedule,
        "sync" to Icons.Outlined.Sync,
        "backup" to Icons.Outlined.Backup,
        "restore" to Icons.Outlined.Restore,
        "theme" to Icons.Outlined.Palette,
        "language" to Icons.Outlined.Language,
        "currency" to Icons.Outlined.CurrencyExchange,
        "transfer" to Icons.Outlined.SwapHoriz,
        "income" to Icons.Outlined.ArrowDownward,
        "expense" to Icons.Outlined.ArrowUpward,
        "balance" to Icons.Outlined.AccountBalance
    )

    fun getIcon(name: String): ImageVector {
        return iconMap[name] ?: Icons.Outlined.Circle
    }
}
