package com.benoitletondor.pixelminimalwatchface.rating

import android.app.Activity
import android.os.Bundle

class FeedbackActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RatingPopup(this).show {
            finish()
        }
    }
}