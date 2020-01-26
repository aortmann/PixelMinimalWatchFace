package com.benoitletondor.pixelminimalwatchface

import android.app.Activity
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import androidx.wear.widget.WearableRecyclerView
import com.benoitletondor.pixelminimalwatchface.Injection.Storage

class ColorSelectionActivity : Activity() {
    private lateinit var colorsRecyclerView: WearableRecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_selection_config)

        val storage = Storage
        val availableColors = ComplicationColorsProvider.getColorOptionsDataSet(this)

        val adapter = ColorSelectionRecyclerViewAdapter(
            availableColors,
            storage
        )

        colorsRecyclerView = findViewById(R.id.colors_recycler_view)
        colorsRecyclerView.isEdgeItemsCenteringEnabled = true
        colorsRecyclerView.layoutManager = WearableLinearLayoutManager(this)
        colorsRecyclerView.setHasFixedSize(true)
        colorsRecyclerView.adapter = adapter
    }
}