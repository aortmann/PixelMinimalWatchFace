package com.benoitletondor.pixelminimalwatchface.model

import androidx.annotation.ColorInt

data class ComplicationColors(@ColorInt val leftColor: Int,
                              @ColorInt val rightColor: Int,
                              val label: String,
                              val isDefault: Boolean) {

    constructor(@ColorInt color: Int, label: String) : this(color, color, label,false)
}