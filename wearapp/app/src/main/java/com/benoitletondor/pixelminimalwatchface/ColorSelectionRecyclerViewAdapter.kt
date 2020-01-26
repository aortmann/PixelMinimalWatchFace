package com.benoitletondor.pixelminimalwatchface

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class ColorSelectionRecyclerViewAdapter(
    private val mColorOptionsDataSet: List<ComplicationColors>,
    private val mStorage: Storage
) : RecyclerView.Adapter<ColorSelectionRecyclerViewAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        return ColorViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.color_config_list_item, parent, false),
            mStorage
        )
    }

    override fun onBindViewHolder(viewHolder: ColorViewHolder, position: Int) {
        val colors = mColorOptionsDataSet[position]

        viewHolder.setColors(colors)
    }

    override fun getItemCount(): Int {
        return mColorOptionsDataSet.size
    }

    class ColorViewHolder(view: View, storage: Storage) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val mLeftColorView: View = view.findViewById(R.id.colorLeft)
        private val mRightColorView: View = view.findViewById(R.id.colorRight)
        private val mStorage: Storage = storage

        private var mColors: ComplicationColors? = null

        init {
            view.setOnClickListener(this)
        }

        fun setColors(colors: ComplicationColors) {
            mColors = colors

            mLeftColorView.setBackgroundColor(colors.leftColor)
            mRightColorView.setBackgroundColor(colors.rightColor)
        }

        override fun onClick(view: View) {
            mStorage.setComplicationColors(mColors!!)

            val activity = view.context as Activity
            activity.setResult(Activity.RESULT_OK)
            activity.finish()
        }
    }

}