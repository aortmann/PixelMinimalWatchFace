package com.benoitletondor.pixelminimalwatchface

import androidx.annotation.ColorInt

class ComplicationColors(@ColorInt val leftColor: Int,
                         @ColorInt val rightColor: Int,
                         val isDefault: Boolean) {

    constructor(@ColorInt color: Int) : this(color, color, false)
}