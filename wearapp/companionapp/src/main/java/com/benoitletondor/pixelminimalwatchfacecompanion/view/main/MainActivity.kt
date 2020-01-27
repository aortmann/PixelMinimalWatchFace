package com.benoitletondor.pixelminimalwatchfacecompanion.view.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import org.koin.android.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
