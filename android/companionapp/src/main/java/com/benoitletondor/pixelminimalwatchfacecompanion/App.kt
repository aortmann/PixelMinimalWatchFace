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
package com.benoitletondor.pixelminimalwatchfacecompanion

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.injection.appModule
import com.benoitletondor.pixelminimalwatchfacecompanion.injection.viewModelModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class App : Application() {
    private val billing: Billing by inject()

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            modules(listOf(appModule, viewModelModule))
        }

        // Activity counter for app foreground & background
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            private var activityCounter = 0

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

            override fun onActivityStarted(activity: Activity) {
                if (activityCounter == 0) {
                    onAppForeground()
                }

                activityCounter++
            }

            override fun onActivityResumed(activity: Activity) {}

            override fun onActivityPaused(activity: Activity) {}

            override fun onActivityStopped(activity: Activity) {
                if (activityCounter == 1) {
                    onAppBackground()
                }

                activityCounter--
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun onAppForeground() {
        billing.updatePremiumStatusIfNeeded()
    }

    private fun onAppBackground() {

    }
}