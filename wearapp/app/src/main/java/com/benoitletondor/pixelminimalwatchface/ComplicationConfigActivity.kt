package com.benoitletondor.pixelminimalwatchface

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.benoitletondor.pixelminimalwatchface.Injection.Storage

class ComplicationConfigActivity : Activity() {
    private lateinit var mWearableRecyclerView: WearableRecyclerView
    private lateinit var mAdapter: ComplicationConfigRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complication_config)

        mAdapter = ComplicationConfigRecyclerViewAdapter(
            applicationContext,
            Storage
        )

        mWearableRecyclerView = findViewById(R.id.wearable_recycler_view)
        mWearableRecyclerView.isEdgeItemsCenteringEnabled = true
        mWearableRecyclerView.layoutManager = LinearLayoutManager(this)
        mWearableRecyclerView.setHasFixedSize(true)
        mWearableRecyclerView.adapter = mAdapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            val complicationProviderInfo: ComplicationProviderInfo? = data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            mAdapter.updateSelectedComplication(complicationProviderInfo)
        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            mAdapter.updatePreviewColors()
        }
    }

    companion object {
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}