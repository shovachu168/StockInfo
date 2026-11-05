package com.cathysec.stock.info.view

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.core.content.withStyledAttributes
import com.cathysec.stock.info.R
import com.cathysec.stock.info.databinding.ViewStockFieldBinding

/**
 * A single "label ... value" row in a stock card.
 * - app:label: label text on the start side
 * - app:valueMaxTextSize: maximum text size of the value; long values shrink automatically instead of being cut off
 */
class StockFieldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : LinearLayout(context, attrs, defStyleAttr) {

    private val binding = ViewStockFieldBinding.inflate(LayoutInflater.from(context), this)

    var value: CharSequence
        get() = binding.tvValue.text
        set(value) {
            binding.tvValue.text = value
        }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        context.withStyledAttributes(attrs, R.styleable.StockFieldView, defStyleAttr) {
            binding.tvLabel.text = getString(R.styleable.StockFieldView_label)
            if (hasValue(R.styleable.StockFieldView_valueMaxTextSize)) {
                setValueMaxTextSize(getDimensionPixelSize(R.styleable.StockFieldView_valueMaxTextSize, 0))
            }
        }
    }

    fun setValueColor(@ColorInt color: Int) {
        binding.tvValue.setTextColor(color)
    }

    private fun setValueMaxTextSize(maxTextSizePx: Int) {
        val valueView = binding.tvValue
        valueView.setAutoSizeTextTypeUniformWithConfiguration(
            valueView.autoSizeMinTextSize,
            maxTextSizePx.coerceAtLeast(valueView.autoSizeMinTextSize),
            AUTO_SIZE_STEP_PX,
            TypedValue.COMPLEX_UNIT_PX,
        )
    }

    private companion object {
        const val AUTO_SIZE_STEP_PX = 1
    }
}
