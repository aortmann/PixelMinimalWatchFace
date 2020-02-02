package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import android.support.wearable.phone.PhoneDeviceType
import android.support.wearable.view.ConfirmationOverlay
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.BuildConfig.COMPANION_APP_PLAYSTORE_URL
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.R
import com.google.android.wearable.intent.RemoteIntent
import kotlinx.android.synthetic.main.activity_complication_config.*

class ComplicationConfigActivity : Activity() {
    private lateinit var adapter: ComplicationConfigRecyclerViewAdapter
    private val storage = Injection.storage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complication_config)

        adapter = ComplicationConfigRecyclerViewAdapter(this, storage, {
            openAppInStoreOnPhone()
        }, { use24hTimeFormat ->
            storage.setUse24hTimeFormat(use24hTimeFormat)
        })

        wearable_recycler_view.isEdgeItemsCenteringEnabled = true
        wearable_recycler_view.layoutManager = LinearLayoutManager(this)
        wearable_recycler_view.setHasFixedSize(true)
        wearable_recycler_view.adapter = adapter
    }

    override fun onDestroy() {
        adapter.onDestroy()

        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            val complicationProviderInfo: ComplicationProviderInfo? = data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            adapter.updateSelectedComplication(complicationProviderInfo)
        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.updatePreviewColors()
        }
    }

    private fun openAppInStoreOnPhone() {
        when (PhoneDeviceType.getPhoneDeviceType(applicationContext)) {
            PhoneDeviceType.DEVICE_TYPE_ANDROID -> {
                // Create Remote Intent to open Play Store listing of app on remote device.
                val intentAndroid = Intent(Intent.ACTION_VIEW)
                    .addCategory(Intent.CATEGORY_BROWSABLE)
                    .setData(Uri.parse(COMPANION_APP_PLAYSTORE_URL))

                RemoteIntent.startRemoteActivity(
                    applicationContext,
                    intentAndroid,
                    object : ResultReceiver(Handler()) {
                        override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                            if (resultCode == RemoteIntent.RESULT_OK) {
                                ConfirmationOverlay()
                                    .setFinishedAnimationListener {
                                        finish()
                                    }
                                    .setType(ConfirmationOverlay.OPEN_ON_PHONE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_device))
                                    .showOn(this@ComplicationConfigActivity)
                            } else if (resultCode == RemoteIntent.RESULT_FAILED) {
                                ConfirmationOverlay()
                                    .setType(ConfirmationOverlay.FAILURE_ANIMATION)
                                    .setDuration(3000)
                                    .setMessage(getString(R.string.open_phone_url_android_not_reachable))
                                    .showOn(this@ComplicationConfigActivity)
                            }
                        }
                    }
                )
            }
            PhoneDeviceType.DEVICE_TYPE_IOS -> {
                Toast.makeText(this@ComplicationConfigActivity, R.string.open_phone_url_ios_device, Toast.LENGTH_LONG).show()
            }
            PhoneDeviceType.DEVICE_TYPE_ERROR_UNKNOWN -> {
                Toast.makeText(this@ComplicationConfigActivity, R.string.open_phone_url_fail, Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}