package com.benoitletondor.pixelminimalwatchfacecompanion.injection

import com.benoitletondor.pixelminimalwatchfacecompanion.view.main.MainViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get()) }
}