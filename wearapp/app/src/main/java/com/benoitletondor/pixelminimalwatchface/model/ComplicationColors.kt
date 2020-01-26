package com.benoitletondor.pixelminimalwatchface.model

import androidx.annotation.ColorInt

data class ComplicationColors(@ColorInt val leftColor: Int,
                              @ColorInt val rightColor: Int,
                              val isDefault: Boolean) {

    constructor(@ColorInt color: Int) : this(color, color, false)
}