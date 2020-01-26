package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderChooserIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.R

class ComplicationConfigActivity : Activity() {
    private lateinit var wearableRecyclerView: WearableRecyclerView
    private lateinit var adapter: ComplicationConfigRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_complication_config)

        adapter = ComplicationConfigRecyclerViewAdapter(this, Injection.Storage)

        wearableRecyclerView = findViewById(R.id.wearable_recycler_view)
        wearableRecyclerView.isEdgeItemsCenteringEnabled = true
        wearableRecyclerView.layoutManager = LinearLayoutManager(this)
        wearableRecyclerView.setHasFixedSize(true)
        wearableRecyclerView.adapter = adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == COMPLICATION_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            val complicationProviderInfo: ComplicationProviderInfo? = data?.getParcelableExtra(ProviderChooserIntent.EXTRA_PROVIDER_INFO)

            adapter.updateSelectedComplication(complicationProviderInfo)
        } else if (requestCode == UPDATE_COLORS_CONFIG_REQUEST_CODE && resultCode == RESULT_OK) {
            adapter.updatePreviewColors()
        }
    }

    companion object {
        const val COMPLICATION_CONFIG_REQUEST_CODE = 1001
        const val UPDATE_COLORS_CONFIG_REQUEST_CODE = 1002
    }
}