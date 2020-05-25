package com.benoitletondor.pixelminimalwatchface.helper

import android.graphics.drawable.Icon

fun Icon.sameAs(otherIcon: Icon): Boolean {
    return toString() == otherIcon.toString()
}