package com.benoitletondor.pixelminimalwatchface

import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.model.StorageImpl

object Injection {
    private val storage = StorageImpl()

    fun storage(): Storage = storage
    fun watchFaceDrawer(): WatchFaceDrawer = WatchFaceDrawerImpl()
}