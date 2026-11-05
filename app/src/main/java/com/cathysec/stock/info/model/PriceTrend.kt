package com.cathysec.stock.info.model

/**
 * Direction of a price movement. The UI layer maps it to a color
 * (Taiwan stock market convention: up is red, down is green).
 */
enum class PriceTrend {
    UP,
    DOWN,
    FLAT,
}
