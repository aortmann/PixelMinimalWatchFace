package com.benoitletondor.pixelminimalwatchfacecompanion.injection

import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.BillingImpl
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.Storage
import com.benoitletondor.pixelminimalwatchfacecompanion.storage.StorageImpl
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.Sync
import com.benoitletondor.pixelminimalwatchfacecompanion.sync.SyncImpl
import org.koin.dsl.module

val appModule = module {
    single<Billing> { BillingImpl(get(), get()) }
    single<Sync> { SyncImpl() }
    single<Storage> { StorageImpl(get()) }
}