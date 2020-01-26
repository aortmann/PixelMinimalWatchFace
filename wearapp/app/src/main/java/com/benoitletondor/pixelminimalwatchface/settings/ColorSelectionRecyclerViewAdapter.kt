package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage

class ColorSelectionRecyclerViewAdapter(
    private val colors: List<ComplicationColors>,
    private val storage: Storage
) : RecyclerView.Adapter<ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.color_config_list_item,
                parent,
                false
            ),
            storage
        )
    }

    override fun onBindViewHolder(viewHolder: ColorViewHolder, position: Int) {
        val colors = colors[position]

        viewHolder.setItem(colors)
    }

    override fun getItemCount(): Int {
        return colors.size
    }

}

class ColorViewHolder(view: View, private val storage: Storage) : RecyclerView.ViewHolder(view), View.OnClickListener {
    private val leftColorView: View = view.findViewById(R.id.colorLeft)
    private val rightColorView: View = view.findViewById(R.id.colorRight)

    private var item: ComplicationColors? = null

    init {
        view.setOnClickListener(this)
    }

    fun setItem(item: ComplicationColors) {
        this.item = item

        leftColorView.setBackgroundColor(item.leftColor)
        rightColorView.setBackgroundColor(item.rightColor)
    }

    override fun onClick(view: View) {
        storage.setComplicationColors(item!!)

        val activity = view.context as Activity
        activity.setResult(Activity.RESULT_OK)
        activity.finish()
    }
}