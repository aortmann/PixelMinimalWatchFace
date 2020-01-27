package com.benoitletondor.pixelminimalwatchfacecompanion.injection

import com.benoitletondor.pixelminimalwatchfacecompanion.billing.Billing
import com.benoitletondor.pixelminimalwatchfacecompanion.billing.BillingImpl
import org.koin.dsl.module

val appModule = module {
    single<Billing> { BillingImpl() }
}