# BarcodeApp

Android 条形码商品管理应用，基于 ZXing 实现扫码功能。

## 功能

- **扫码查询** — 扫描条形码，查询已录入的商品信息
- **扫码录入** — 扫描条形码，录入新商品（名称、价格）
- **商品管理** — 查看、编辑、删除商品
- **商品列表** — RecyclerView 展示所有商品，支持 DiffUtil 高效更新
- **CSV 导入导出** — 批量导入/导出商品数据

## 技术栈

- **语言**: Java
- **最低版本**: Android 7.0 (API 24)
- **条码扫描**: [ZXing Android Embedded](https://github.com/journeyapps/zxing-android-embedded)
- **数据库**: SQLite (WAL 模式)
- **UI**: Material Design Components

## 构建

1. 克隆仓库
   ```bash
   git clone https://github.com/chfabfu/scan_apk.git
   ```
2. 用 Android Studio 打开项目
3. 同步 Gradle 并运行

## CSV 格式

导入文件需命名为 `products_import.csv`，放在应用外部存储目录，格式：

```csv
barcode,name,price
6901234567890,矿泉水,2.5
6909876543210,方便面,4.0
```

导出文件为 `products_export.csv`，保存在同一目录。

## License

[MIT](LICENSE)
