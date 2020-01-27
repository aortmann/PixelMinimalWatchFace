package com.benoitletondor.pixelminimalwatchfacecompanion.storage

interface Storage {
    fun isUserPremium(): Boolean
    fun setUserPremium(premium: Boolean)
}