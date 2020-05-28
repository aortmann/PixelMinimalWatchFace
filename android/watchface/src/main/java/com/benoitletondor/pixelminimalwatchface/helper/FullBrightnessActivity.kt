package com.benoitletondor.pixelminimalwatchface.helper

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_SCREEN_OFF
import android.content.IntentFilter
import android.os.Bundle
import android.support.wearable.input.RotaryEncoder
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View

class FullBrightnessActivity : Activity() {
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val attributes = window.attributes
        attributes.screenBrightness = 1.0f
        window.attributes = attributes

        registerReceiver(broadcastReceiver, IntentFilter(ACTION_SCREEN_OFF))
        window.decorView.setOnTouchListener { _, _ ->
            finish()
            true
        }
        window.decorView.setOnGenericMotionListener(View.OnGenericMotionListener { _, ev ->
            if ( ev.action == MotionEvent.ACTION_SCROLL && RotaryEncoder.isFromRotaryEncoder(ev) ) {
                finish()
                return@OnGenericMotionListener true
            }

            return@OnGenericMotionListener false
        })
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.repeatCount == 0) {
            when (keyCode) {
                KeyEvent.KEYCODE_STEM_1 -> {
                    finish()
                    true
                }
                KeyEvent.KEYCODE_STEM_2 -> {
                    finish()
                    true
                }
                KeyEvent.KEYCODE_STEM_3 -> {
                    finish()
                    true
                }
                else -> {
                    super.onKeyDown(keyCode, event)
                }
            }
        } else {
            super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        window.decorView.setOnTouchListener(null)
        window.decorView.setOnGenericMotionListener(null)

        super.onDestroy()
    }
}