package com.benoitletondor.pixelminimalwatchface.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

    fun setItem(item: ComplicationColors,
                onClickListener: () -> Unit) {
        leftColorView.setBackgroundColor(item.leftColor)
        rightColorView.setBackgroundColor(item.rightColor)

        itemView.setOnClickListener {
            onClickListener()
        }
    }
}