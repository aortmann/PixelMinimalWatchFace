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
package com.benoitletondor.pixelminimalwatchfacecompanion.injection

import com.benoitletondor.pixelminimalwatchfacecompanion.BuildConfig
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.BillingImpl
import com.benoitletondor.pixelminimalwatchfacecompanion.config.Config
import com.benoitletondor.pixelminimalwatchfacecompanion.config.ConfigImpl
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.StorageImpl
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.SyncImpl
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.koin.dsl.module
import java.util.concurrent.TimeUnit

private const val REMOTE_CONFIG_FETCH_THROTTLE_DEFAULT_VALUE_HOURS = 1L

val appModule = module {
    single<Billing> { BillingImpl(get(), get()) }
    single<Sync> { SyncImpl(get()) }
    single<Storage> { StorageImpl(get()) }
    single<Config> {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if( BuildConfig.DEBUG ) {
                0L
            } else {
                TimeUnit.HOURS.toSeconds(REMOTE_CONFIG_FETCH_THROTTLE_DEFAULT_VALUE_HOURS)
            })
            .build()

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings)

        return@single ConfigImpl(firebaseRemoteConfig)
    }
}