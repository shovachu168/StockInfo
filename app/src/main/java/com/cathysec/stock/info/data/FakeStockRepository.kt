package com.cathysec.stock.info.data

import com.cathysec.stock.info.model.Stock
import java.math.BigDecimal

/**
 * In-memory [StockRepository] used before the TWSE API is integrated.
 * Data is taken from real TWSE data on 2026/11/04, except the first entry which is a
 * made-up boundary case using realistic upper bounds (five-digit prices and the largest
 * plausible daily trade figures) to check that the layout does not break.
 */
class FakeStockRepository : StockRepository {

    override suspend fun getStocks(): List<Stock> = STOCKS

    private companion object {

        val STOCKS = listOf(
            stock("99999", "邊界測試", "99999.99", "99999.99", "99999.99", "90000.00", "9999.99", "98765.43", 9_999_999L, 9_999_999_999L, 9_999_999_999_999L),
            stock("2330", "台積電", "2320.00", "2260.00", "2320.00", "2235.00", "-35.0000", "2295.00", 508399L, 53879371L, 122293100290L),
            stock("2317", "鴻海", "247.50", "240.00", "247.50", "236.50", "-5.5000", "245.50", 50635L, 62806651L, 15094838874L),
            stock("2454", "聯發科", "3740.00", "3715.00", "3760.00", "3455.00", "40.0000", "3786.67", 161878L, 16244007L, 58987405310L),
            stock("2412", "中華電", "159.50", "161.50", "161.50", "159.00", "2.5000", "160.00", 9283L, 16362974L, 2625358905L),
            stock("2881", "富邦金", "154.00", "154.50", "156.00", "152.00", "0.5000", "156.00", 11066L, 22981239L, 3529621965L),
            stock("2603", "長榮", "264.50", "268.00", "268.50", "261.00", "1.5000", "269.67", 8505L, 9077701L, 2403597163L),
            stock("0050", "元大台灣50", "101.55", "99.85", "101.65", "97.55", "-0.7500", "101.52", 836670L, 447215925L, 44367305387L),
            stock("1101", "台泥", "27.80", "27.25", "28.00", "26.80", "-0.4500", "27.68", 16560L, 57045650L, 1553237473L),
            stock("2308", "台達電", "1290.00", "1220.00", "1305.00", "1160.00", "-50.0000", "1281.67", 101218L, 26439420L, 32298617830L),
            stock("2882", "國泰金", "113.00", "111.00", "114.00", "110.00", "-2.0000", "113.33", 10244L, 27965632L, 3115706863L),
            stock("2059", "川湖", "9355.00", "8600.00", "9360.00", "8320.00", "-640.0000", "9086.67", 21749L, 1611332L, 13878235390L),
            stock("2886", "兆豐金", "53.10", "53.50", "54.10", "52.40", "0.3000", "53.60", 10732L, 29970174L, 1597933966L),
            stock("1301", "台塑", "69.40", "69.10", "69.80", "66.70", "0.4000", "70.30", 14319L, 24387428L, 1667821246L),
            stock("1303", "南亞", "273.00", "276.50", "283.00", "261.00", "5.5000", "282.83", 89373L, 119222395L, 32488729028L),
            stock("2002", "中鋼", "19.30", "19.15", "19.40", "18.95", "-0.2000", "19.30", 9449L, 36205641L, 692280491L),
            stock("3711", "日月光投控", "635.00", "627.00", "641.00", "595.00", "11.0000", "639.00", 44048L, 32020635L, 19817214963L),
            stock("2382", "廣達", "314.00", "302.50", "314.00", "299.00", "-9.0000", "310.67", 20487L, 18779674L, 5695087801L),
            stock("2357", "華碩", "895.00", "877.00", "900.00", "858.00", "-12.0000", "892.67", 7368L, 3980079L, 3479655126L),
            stock("3008", "大立光", "5100.00", "5010.00", "5100.00", "4730.00", "65.0000", "5148.33", 19171L, 2669539L, 13160298910L),
            stock("00625K", "富邦上証+R", null, null, null, null, "0.0000", null, null, null, null),
        )

        @Suppress("LongParameterList")
        fun stock(
            code: String,
            name: String,
            opening: String?,
            closing: String?,
            highest: String?,
            lowest: String?,
            change: String?,
            monthlyAverage: String?,
            transaction: Long?,
            tradeVolume: Long?,
            tradeValue: Long?,
        ) = Stock(
            code = code,
            name = name,
            openingPrice = opening?.let(::BigDecimal),
            closingPrice = closing?.let(::BigDecimal),
            highestPrice = highest?.let(::BigDecimal),
            lowestPrice = lowest?.let(::BigDecimal),
            change = change?.let(::BigDecimal),
            monthlyAveragePrice = monthlyAverage?.let(::BigDecimal),
            transaction = transaction,
            tradeVolume = tradeVolume,
            tradeValue = tradeValue,
        )
    }
}
