package com.benoitletondor.pixelminimalwatchface

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationIds
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getSupportedComplicationTypes
import java.util.concurrent.Executors

private const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 0
private const val TYPE_COLOR_CONFIG = 1

class ComplicationConfigRecyclerViewAdapter(
    private val mContext: Context,
    private val storage: Storage
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ComplicationLocation {
        LEFT, RIGHT
    }

    private val mWatchFaceComponentName: ComponentName = ComponentName(mContext, PixelMinimalWatchFace::class.java)

    private var mSelectedComplicationLocation: ComplicationLocation? = null

    private val mLeftComplicationId: Int = getComplicationId(ComplicationLocation.LEFT)
    private val mRightComplicationId: Int =  getComplicationId(ComplicationLocation.RIGHT)

    private val mProviderInfoRetriever: ProviderInfoRetriever = ProviderInfoRetriever(mContext, Executors.newCachedThreadPool())

    private lateinit var mPreviewAndComplicationsViewHolder: PreviewAndComplicationsViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                mPreviewAndComplicationsViewHolder = PreviewAndComplicationsViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(
                            R.layout.config_list_preview_and_complications_item,
                            parent,
                            false
                        ),
                    object : ComplicationClickListener {
                        override fun onComplicationClicked(location: ComplicationLocation) {
                            mSelectedComplicationLocation = location
                            val selectedComplicationId = getComplicationId(location)
                            if (selectedComplicationId >= 0) {
                                val supportedTypes = getSupportedComplicationTypes(location)

                                val watchFace = ComponentName(mContext, PixelMinimalWatchFace::class.java)
                                (mContext as Activity).startActivityForResult(
                                    ComplicationHelperActivity.createProviderChooserHelperIntent(
                                        mContext,
                                        watchFace,
                                        selectedComplicationId,
                                        *supportedTypes
                                    ),
                                    ComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE
                                )
                            }
                        }
                    }
                )

                return mPreviewAndComplicationsViewHolder!!
            }
            TYPE_COLOR_CONFIG -> return ColorPickerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_color_item,
                    parent,
                    false
                )
            )
        }
        throw IllegalStateException("Unknown option type: $viewType")
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        when (viewHolder.itemViewType) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                val previewAndComplicationsViewHolder = viewHolder as PreviewAndComplicationsViewHolder

                previewAndComplicationsViewHolder.setDefaultComplicationDrawable()
                initializesColorsAndComplications()
            }
        }
    }

    private fun initializesColorsAndComplications() {
        val complicationIds = getComplicationIds()

        mProviderInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {

                    mPreviewAndComplicationsViewHolder.updateComplicationViews(
                        if (watchFaceComplicationId == mLeftComplicationId) ComplicationLocation.LEFT else ComplicationLocation.RIGHT,
                        complicationProviderInfo,
                        storage.getComplicationColors()
                    )
                }
            },
            mWatchFaceComponentName,
            *complicationIds
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG
        } else {
            TYPE_COLOR_CONFIG
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

    /** Updates the selected complication id saved earlier with the new information.  */
    fun updateSelectedComplication(complicationProviderInfo: ComplicationProviderInfo?) { // Checks if view is inflated and complication id is valid.
        val selectedComplicationLocation = mSelectedComplicationLocation

        if ( selectedComplicationLocation != null ) {
            mPreviewAndComplicationsViewHolder.updateComplicationViews(
                selectedComplicationLocation,
                complicationProviderInfo,
                storage.getComplicationColors()
            )
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        mProviderInfoRetriever.init()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        // Required to release retriever for active complication data on detach.
        mProviderInfoRetriever.release()
    }

    fun updatePreviewColors() {
        mPreviewAndComplicationsViewHolder.updateComplicationsAccentColor(storage.getComplicationColors())
    }

    /**
     * Displays watch face preview along with complication locations. Allows user to tap on the
     * complication they want to change and preview updates dynamically.
     */
    class PreviewAndComplicationsViewHolder(
        view: View,
        private val mListener: ComplicationClickListener
    ) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val mLeftComplicationBackground: ImageView = view.findViewById(R.id.left_complication_background)
        private val mRightComplicationBackground: ImageView = view.findViewById(R.id.right_complication_background)
        private val mLeftComplication: ImageButton = view.findViewById(R.id.left_complication)
        private val mRightComplication: ImageButton = view.findViewById(R.id.right_complication)
        private var mDefaultComplicationDrawable: Drawable = view.context.getDrawable(R.drawable.add_complication)!!

        init {
            mLeftComplication.setOnClickListener(this)
            mRightComplication.setOnClickListener(this)
        }

        fun setDefaultComplicationDrawable() {
            mLeftComplication.setImageDrawable(mDefaultComplicationDrawable)
            mLeftComplicationBackground.visibility = View.INVISIBLE
            mRightComplication.setImageDrawable(mDefaultComplicationDrawable)
            mRightComplicationBackground.visibility = View.INVISIBLE
        }

        override fun onClick(view: View) {
            if (view == mLeftComplication) {
                mListener.onComplicationClicked(ComplicationLocation.LEFT)
            } else if (view == mRightComplication) {
                mListener.onComplicationClicked(ComplicationLocation.RIGHT)
            }
        }

        fun updateComplicationViews(location: ComplicationLocation,
                                    complicationProviderInfo: ComplicationProviderInfo?,
                                    complicationColors: ComplicationColors) {

            if (location == ComplicationLocation.LEFT) {
                updateComplicationView(
                    complicationProviderInfo, mLeftComplication,
                    mLeftComplicationBackground, complicationColors
                )
            } else if (location == ComplicationLocation.RIGHT) {
                updateComplicationView(
                    complicationProviderInfo, mRightComplication,
                    mRightComplicationBackground, complicationColors
                )
            }
        }

        private fun updateComplicationView(complicationProviderInfo: ComplicationProviderInfo?,
                                           button: ImageButton,
                                           background: ImageView,
                                           complicationColors: ComplicationColors) {

            if (complicationProviderInfo != null) {
                button.setImageIcon(complicationProviderInfo.providerIcon)
                background.visibility = View.VISIBLE
            } else {
                button.setImageDrawable(mDefaultComplicationDrawable)
                background.visibility = View.INVISIBLE
            }
            updateComplicationsAccentColor(complicationColors)
        }

        fun updateComplicationsAccentColor(colors: ComplicationColors) {
            mRightComplication.setColorFilter(colors.rightColor)
            mLeftComplication.setColorFilter(colors.leftColor)
        }
    }

    interface ComplicationClickListener {
        fun onComplicationClicked(location: ComplicationLocation)
    }

    class ColorPickerViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val launchIntent = Intent(view.context, ColorSelectionActivity::class.java)
            val activity = view.context as Activity

            activity.startActivityForResult(
                launchIntent,
                ComplicationConfigActivity.UPDATE_COLORS_CONFIG_REQUEST_CODE
            )
        }
    }
}