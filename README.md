# StockInfo

台灣證券交易所上市個股日成交資訊 App（Android 工程師實作考題）。
資料來源為證交所 OpenAPI，合併多支 API 後以卡片列表呈現，支援排序、下拉重新整理，並可點擊卡片查看本益比、殖利率與股價淨值比。

## 版本介紹

| 1.0.0 | 考題基本功能：個股列表、收盤價與月平均價比較、漲跌色、依代號排序、點擊顯示本益比 / 殖利率 / 淨值比、載入中 / 錯誤 / 重試 / 下拉重新整理 |

## 功能

- **個股列表**：一列一張卡片，顯示開盤價、收盤價、最高價、最低價、漲跌價差、月平均價、成交筆數、成交股數、成交金額。
- **顏色規則**（台股慣例，紅漲綠跌）
  - 收盤價高於月平均價顯示紅色，低於顯示綠色，相同為預設色。
  - 漲跌價差為正顯示紅色，為負顯示綠色，為 0 為預設色。
- **排序**：右上角篩選 icon 開啟底部選單，依股票代號排序，預設降序，可切換升序。
- **估值資訊**：點擊卡片顯示本益比、殖利率、股價淨值比。
  - 資料載入中：顯示「正在抓取資料中」，完成後自動切換為內容。
  - 載入失敗：顯示錯誤訊息與重試按鈕。
  - 找不到對應資料：顯示「找不到對應資料」。
  - 點對話框外面即可關閉。
- **載入狀態**：載入中顯示進度圈；失敗時區分「沒有網路」「連線逾時」「一般錯誤」並提供重試；支援下拉重新整理，更新失敗時保留舊清單並以 Snackbar 提示。

## 使用的 API

| API | 用途 |
|---|---|
| `GET /v1/exchangeReport/STOCK_DAY_ALL` | 個股日成交資訊（主清單、收盤價、漲跌價差等） |
| `GET /v1/exchangeReport/STOCK_DAY_AVG_ALL` | 月平均價 |
| `GET /v1/exchangeReport/BWIBBU_ALL` | 本益比、殖利率、股價淨值比 |

Base URL：`https://openapi.twse.com.tw/v1/`

## 技術棧

| 用途 | 套件 |
|---|---|
| 語言 | Kotlin |
| 架構 | MVVM、單向資料流 |
| 畫面 | XML + ViewBinding（列表與卡片）、Jetpack Compose（排序底部選單、估值對話框）、Material 3 |
| 網路 | Retrofit + OkHttp（含 logging-interceptor） |
| JSON | Moshi（KSP codegen） |
| 依賴注入 | Koin |
| 非同步 | Coroutines + Flow |
| 單元測試 | JUnit4、MockK、Turbine、kotlinx-coroutines-test、MockWebServer、koin-test |

版本統一管理於 `gradle/libs.versions.toml`。`minSdk 26`、`targetSdk 36`、`compileSdk 37`。

## 資料流

```
View ──事件──▶ ViewModel ──▶ Repository ──▶ TwseApiService（Retrofit）
  ▲               │
  └──UiState──────┘ （StateFlow）
```

1. `StockListActivity` 只負責顯示與轉送使用者事件（重試、下拉更新、排序、點擊卡片）。
2. `StockListViewModel` 持有畫面狀態，以 `StateFlow` 輸出 `StockListUiState`（Loading / Success / Error）。
3. `TwseStockRepository` 並發呼叫 API、合併資料；估值資料的載入狀態以 `StateFlow` 保存在 Repository。
4. API 回傳的 `Response` 類別只存在於 `data/`，轉換成 `model/` 的 `Stock` 後才往上傳。

## 專案結構

```
com.cathysec.stock.info
├── di/          Koin module
├── model/       Stock、StockValuation、SortOrder、PriceTrend、LoadError 等資料模型
├── data/        Repository、TwseApiService（Retrofit）
│   └── response/  對應 API 回傳格式的 Response 類別
├── viewmodel/   ViewModel、UiState、顯示用的 UiModel
├── view/        Activity、Adapter、StockFieldView、Compose 元件（SortBottomSheet、StockValuationDialog）
├── theme/       Compose 主題（StockTheme）
└── util/        可單元測試的純商業邏輯（Parse、Merge、Format、Sort、PriceTrend、LoadError）
```

測試資料夾 `src/test` 依相同結構擺放。

## 設計決策與取捨

- **兩支 API 並發，缺一不可**：`STOCK_DAY_ALL` 與 `STOCK_DAY_AVG_ALL` 以 `async` 並發請求，兩支都成功才顯示列表；任一失敗整個列表視為錯誤，避免畫面上有些卡片沒有月平均價而誤導使用者。月平均價找不到對應代號時為 null（顯示 `-`），不算錯誤。
- **收盤價以 `STOCK_DAY_ALL` 為準**：卡片顯示與顏色比較使用同一個值。
- **價格一律無條件捨去到兩位小數**：解析時就正規化，讓顏色判斷與顯示文字一致，不會出現「0.0001 顯示紅色 0.00」。
- **股票代號以字串排序**：代號有 `0050`、`00625K` 這類非純數字，用字串比較較不會出錯。重新整理後維持目前排序方向。
- **`BWIBBU_ALL` 於啟動時在背景載入**：與列表完全獨立，成功或失敗都不影響列表；點擊卡片時依目前狀態顯示內容、載入中或重試，不必等使用者點擊才發請求。
- **效能**：排序與轉成顯示用資料放在背景執行緒（`Dispatchers.Default`），並讓每個執行緒重用一個 `DecimalFormat`，列表準備期間載入圈圈不會卡住。
- **Util 為純 Kotlin**：商業邏輯（解析、合併、格式化、排序、漲跌判斷）都不依賴 Android 框架，可直接用 JVM 單元測試；Util 回傳語意值（如 `UP / DOWN / FLAT`），由畫面層對應成顏色。
- **只做單元測試**：未加入 UI 測試。

## 建置與測試

本機若沒有系統 JDK，可使用 Android Studio 內建的 JDK：

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

./gradlew assembleDebug          # 建置
./gradlew testDebugUnitTest      # 單元測試
```

單元測試涵蓋「正確、錯誤、極端值、超出範圍」四類情境，包含解析與格式化、資料合併、排序、漲跌判斷、錯誤分類、Repository（含 MockWebServer 測試 API）、ViewModel 狀態等。
