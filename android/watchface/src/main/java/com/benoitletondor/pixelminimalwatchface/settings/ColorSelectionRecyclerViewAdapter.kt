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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors

class ColorSelectionRecyclerViewAdapter(
    private val colors: List<ComplicationColors>,
    private val onColorsSelectedListener: (colors: ComplicationColors) -> Unit
) : RecyclerView.Adapter<ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.color_config_list_item, parent, false))
    }

    override fun onBindViewHolder(viewHolder: ColorViewHolder, position: Int) {
        val colors = colors[position]

        viewHolder.setItem(colors) {
            onColorsSelectedListener(colors)
        }
    }

    override fun getItemCount(): Int {
        return colors.size
    }

}

class ColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val leftColorView: View = view.findViewById(R.id.colorLeft)
    private val rightColorView: View = view.findViewById(R.id.colorRight)
    private val colorsContainer: ViewGroup = view.findViewById(R.id.color_config_list_item_colors_container)
    private val colorLabelTextView: TextView = view.findViewById(R.id.color_config_list_item_color_label)

    init {
        colorsContainer.clipToOutline = true
    }

    fun setItem(item: ComplicationColors,
                onClickListener: () -> Unit) {
        leftColorView.setBackgroundColor(item.leftColor)
        rightColorView.setBackgroundColor(item.rightColor)
        colorLabelTextView.text = item.label

        itemView.setOnClickListener {
            onClickListener()
        }
    }
}