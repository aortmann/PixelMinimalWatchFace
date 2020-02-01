package com.benoitletondor.pixelminimalwatchfacecompanion.view.onboarding

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Observer
import com.benoitletondor.pixelminimalwatchfacecompanion.R
import kotlinx.android.synthetic.main.activity_onboarding.*
import org.koin.android.viewmodel.ext.android.viewModel

class OnboardingActivity : AppCompatActivity() {

    private val viewModel: OnboardingViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewModel.finishEventStream.observe(this, Observer {
            finish()
        })

        onboarding_finish_cta.setOnClickListener {
            viewModel.onOnboardingFinishButtonPressed()
        }
    }
}
