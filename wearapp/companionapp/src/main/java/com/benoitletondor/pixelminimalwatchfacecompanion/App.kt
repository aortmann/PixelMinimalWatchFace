package com.benoitletondor.pixelminimalwatchfacecompanion

import android.app.Application
import com.benoitletondor.pixelminimalwatchfacecompanion.injection.appModule
import com.benoitletondor.pixelminimalwatchfacecompanion.injection.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule))
        }
    }
}