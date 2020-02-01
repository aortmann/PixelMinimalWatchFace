package com.benoitletondor.pixelminimalwatchface.model

import androidx.annotation.ColorInt

data class ComplicationColors(@ColorInt val leftColor: Int,
                              @ColorInt val rightColor: Int,
                              val label: String,
                              val isDefault: Boolean) {

    constructor(@ColorInt color: Int, label: String) : this(color, color, label,false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComplicationColors

        if (leftColor != other.leftColor) return false
        if (rightColor != other.rightColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = leftColor
        result = 31 * result + rightColor
        return result
    }

}