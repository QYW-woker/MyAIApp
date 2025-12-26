# AI智能记账APP

一款简洁强大的Android记账应用，采用iOS HIG设计风格，支持AI智能分类和截图OCR识别。

## 功能特性

### 核心功能
- 📝 **快捷记账** - 支出/收入/转账，数字键盘快速输入
- 📊 **统计分析** - 周/月/年统计，分类占比图表
- 💰 **资产管理** - 多账户管理，净资产追踪
- 📅 **账单日历** - 日历视图查看每日收支
- 💵 **预算管理** - 设置月度预算，超支提醒
- 🎯 **存钱计划** - 目标存钱，进度追踪

### AI功能
- 🤖 **智能分类** - DeepSeek API自动识别消费分类
- 📷 **截图记账** - ML Kit OCR识别付款截图

### 其他特性
- 🔒 **隐私保护** - 应用锁，生物识别
- 📱 **桌面小组件** - 快速记账入口
- 💾 **本地存储** - JSON文件存储，无需数据库
- 🎨 **iOS风格UI** - Material 3 + iOS HIG设计

## 技术栈

- **语言**: Kotlin
- **UI框架**: Jetpack Compose + Material 3
- **存储**: 本地JSON文件
- **AI**: DeepSeek API / Groq API
- **OCR**: Google ML Kit (中文)
- **小组件**: Glance
- **架构**: MVVM

## 项目结构

```
app/src/main/java/com/myaiapp/
├── data/
│   ├── local/
│   │   ├── FileStorageManager.kt   # JSON文件存储
│   │   └── model/Models.kt         # 数据模型
│   └── remote/
│       ├── AIApiService.kt         # AI API接口
│       └── AIRepository.kt         # AI仓库
├── ui/
│   ├── screens/
│   │   ├── home/          # 首页
│   │   ├── record/        # 记账
│   │   ├── records/       # 明细
│   │   ├── statistics/    # 统计
│   │   ├── assets/        # 资产
│   │   ├── budget/        # 预算
│   │   ├── savings/       # 存钱计划
│   │   ├── calendar/      # 日历
│   │   └── settings/      # 设置
│   ├── components/        # 通用组件
│   ├── navigation/        # 导航
│   └── theme/             # 主题
├── ocr/
│   └── ScreenshotRecognizer.kt
├── widget/
│   └── QuickRecordWidget.kt
└── util/
    └── Formatters.kt
```

## 开始使用

### 环境要求
- Android Studio Hedgehog | 2023.1.1+
- JDK 17
- Android SDK 34
- Kotlin 1.9.20

### 构建步骤

1. 克隆仓库
```bash
git clone https://github.com/yourusername/MyAIApp.git
cd MyAIApp
```

2. 用Android Studio打开项目

3. 等待Gradle同步完成

4. 运行到模拟器或真机

### 配置AI功能

在设置页面配置DeepSeek API：
1. 获取API Key: https://platform.deepseek.com/
2. 打开APP → 设置 → AI设置
3. 填入API Key

## 数据存储

所有数据存储在本地：
```
/data/data/com.myaiapp/files/MyAIAPP/
├── config/          # 设置
├── accounts/        # 账本和账户
├── records/         # 交易记录
├── budget/          # 预算
├── savings/         # 存钱计划
└── backup/          # 备份
```

## 预设分类

### 支出
餐饮、购物、交通、娱乐、居住、医疗、教育、通讯、美容、运动、社交、旅行、宠物、礼物、其他

### 收入
工资、奖金、副业、投资、利息、礼金、退款、其他

## 贡献

欢迎提交Issue和Pull Request！

## 许可证

MIT License
