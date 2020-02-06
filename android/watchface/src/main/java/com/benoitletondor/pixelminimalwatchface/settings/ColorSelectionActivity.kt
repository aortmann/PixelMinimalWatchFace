/*
 *   Copyright 2020 Benoit LETONDOR
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.os.Bundle
import androidx.wear.widget.WearableLinearLayoutManager
import com.benoitletondor.pixelminimalwatchface.Injection
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColorsProvider
import kotlinx.android.synthetic.main.activity_color_selection_config.*

class ColorSelectionActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_selection_config)

        val availableColors = ComplicationColorsProvider.getAllComplicationColors(this)
        val adapter = ColorSelectionRecyclerViewAdapter(availableColors) { selectedColor ->
            Injection.storage().setComplicationColors(selectedColor)

            setResult(RESULT_OK)
            finish()
        }

        colors_recycler_view.isEdgeItemsCenteringEnabled = true
        colors_recycler_view.layoutManager = WearableLinearLayoutManager(this)
        colors_recycler_view.setHasFixedSize(true)
        colors_recycler_view.adapter = adapter
    }
}