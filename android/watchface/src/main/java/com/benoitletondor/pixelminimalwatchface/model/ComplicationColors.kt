/*
 *   Copyright 2020 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.model

import androidx.annotation.ColorInt

data class ComplicationColors(@ColorInt val leftColor: Int,
                              @ColorInt val middleColor: Int,
                              @ColorInt val rightColor: Int,
                              val label: String,
                              val isDefault: Boolean) {

    constructor(@ColorInt color: Int, label: String) : this(color, color, color, label,false)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComplicationColors

        if (leftColor != other.leftColor) return false
        if (middleColor != other.middleColor) return false
        if (rightColor != other.rightColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = leftColor
        result = 31 * result + middleColor
        result = 31 * result + rightColor
        return result
    }

}