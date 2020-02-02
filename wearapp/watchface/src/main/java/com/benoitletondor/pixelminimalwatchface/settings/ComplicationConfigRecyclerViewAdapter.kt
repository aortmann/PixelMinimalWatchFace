package com.benoitletondor.pixelminimalwatchface.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.support.wearable.complications.ProviderInfoRetriever.OnProviderInfoReceivedCallback
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.benoitletondor.pixelminimalwatchface.BuildConfig
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationId
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getComplicationIds
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.getSupportedComplicationTypes
import com.benoitletondor.pixelminimalwatchface.R
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage
import java.util.concurrent.Executors

private const val TYPE_HEADER = 0
private const val TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG = 1
private const val TYPE_COLOR_CONFIG = 2
private const val TYPE_FOOTER = 3
private const val TYPE_BECOME_PREMIUM = 4
private const val TYPE_HOUR_FORMAT = 5

class ComplicationConfigRecyclerViewAdapter(
    private val context: Context,
    private val storage: Storage,
    private val premiumClickListener: () -> Unit,
    private val hourFormatSelectionListener: (Boolean) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedComplicationLocation: ComplicationLocation? = null

    private val watchFaceComponentName = ComponentName(context, PixelMinimalWatchFace::class.java)
    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())
    private lateinit var previewAndComplicationsViewHolder: PreviewAndComplicationsViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_HEADER -> return HeaderViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_header,
                    parent,
                    false
                )
            )
            TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG -> {
                previewAndComplicationsViewHolder =
                    PreviewAndComplicationsViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.config_list_preview_and_complications_item, parent, false)) { location ->
                        selectedComplicationLocation = location

                        val selectedComplicationId = getComplicationId(location)
                        if (selectedComplicationId >= 0) {
                            val supportedTypes = getSupportedComplicationTypes(location)

                            (context as Activity).startActivityForResult(
                                ComplicationHelperActivity.createProviderChooserHelperIntent(
                                    context,
                                    watchFaceComponentName,
                                    selectedComplicationId,
                                    *supportedTypes
                                ),
                                ComplicationConfigActivity.COMPLICATION_CONFIG_REQUEST_CODE
                            )
                        }
                    }

                return previewAndComplicationsViewHolder
            }
            TYPE_COLOR_CONFIG -> return ColorPickerViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_color_item,
                    parent,
                    false
                )
            )
            TYPE_FOOTER -> return FooterViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_footer,
                    parent,
                    false
                )
            )
            TYPE_BECOME_PREMIUM -> return PremiumViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_premium,
                    parent,
                    false
                ),
                premiumClickListener
            )
            TYPE_HOUR_FORMAT -> return HourFormatViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.config_list_hour_format,
                    parent,
                    false
                ),
                hourFormatSelectionListener
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
            TYPE_HOUR_FORMAT -> {
                val use24hTimeFormat = storage.getUse24hTimeFormat()
                (viewHolder as HourFormatViewHolder).setHourFormatSwitchChecked(use24hTimeFormat)
            }
        }
    }

    private fun initializesColorsAndComplications() {
        val complicationIds = getComplicationIds()

        providerInfoRetriever.retrieveProviderInfo(
            object : OnProviderInfoReceivedCallback() {
                override fun onProviderInfoReceived(watchFaceComplicationId: Int, complicationProviderInfo: ComplicationProviderInfo?) {

                    previewAndComplicationsViewHolder.updateComplicationViews(
                        if (watchFaceComplicationId == getComplicationId(ComplicationLocation.LEFT)) ComplicationLocation.LEFT else ComplicationLocation.RIGHT,
                        complicationProviderInfo,
                        storage.getComplicationColors()
                    )
                }
            },
            watchFaceComponentName,
            *complicationIds
        )
    }

    override fun getItemViewType(position: Int): Int {
        return if( storage.isUserPremium() ) {
            when (position) {
                0 -> TYPE_HEADER
                1 -> TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG
                2 -> TYPE_COLOR_CONFIG
                3 -> TYPE_HOUR_FORMAT
                else -> TYPE_FOOTER
            }
        } else {
            when (position) {
                0 -> TYPE_HEADER
                1 -> TYPE_BECOME_PREMIUM
                2 -> TYPE_HOUR_FORMAT
                else -> TYPE_FOOTER
            }
        }

    }

    override fun getItemCount(): Int {
        return if( storage.isUserPremium() ) {
            5
        } else {
            4
        }
    }

    /** Updates the selected complication id saved earlier with the new information.  */
    fun updateSelectedComplication(complicationProviderInfo: ComplicationProviderInfo?) { // Checks if view is inflated and complication id is valid.
        val selectedComplicationLocation = selectedComplicationLocation

        if ( selectedComplicationLocation != null ) {
            previewAndComplicationsViewHolder.updateComplicationViews(
                selectedComplicationLocation,
                complicationProviderInfo,
                storage.getComplicationColors()
            )
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        providerInfoRetriever.init()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        onDestroy()
    }

    fun onDestroy() {
        providerInfoRetriever.release()
    }

    fun updatePreviewColors() {
        previewAndComplicationsViewHolder.updateComplicationsAccentColor(storage.getComplicationColors())
    }
}

enum class ComplicationLocation {
    LEFT, RIGHT
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

class PreviewAndComplicationsViewHolder(
    view: View,
    private val listener: (location: ComplicationLocation) -> Unit
) : RecyclerView.ViewHolder(view), View.OnClickListener {

    private val leftComplicationBackground: ImageView = view.findViewById(R.id.left_complication_background)
    private val rightComplicationBackground: ImageView = view.findViewById(R.id.right_complication_background)
    private val leftComplication: ImageButton = view.findViewById(R.id.left_complication)
    private val rightComplication: ImageButton = view.findViewById(R.id.right_complication)
    private var addComplicationDrawable: Drawable = view.context.getDrawable(R.drawable.add_complication)!!
    private var addedComplicationDrawable: Drawable = view.context.getDrawable(R.drawable.added_complication)!!

    init {
        leftComplication.setOnClickListener(this)
        rightComplication.setOnClickListener(this)
    }

    fun setDefaultComplicationDrawable() {
        leftComplication.setImageDrawable(addComplicationDrawable)
        rightComplication.setImageDrawable(addComplicationDrawable)
    }

    override fun onClick(view: View) {
        if (view == leftComplication) {
            listener(ComplicationLocation.LEFT)
        } else if (view == rightComplication) {
            listener(ComplicationLocation.RIGHT)
        }
    }

    fun updateComplicationViews(location: ComplicationLocation,
                                complicationProviderInfo: ComplicationProviderInfo?,
                                complicationColors: ComplicationColors) {
        if (location == ComplicationLocation.LEFT) {
            updateComplicationView(
                complicationProviderInfo,
                leftComplication,
                leftComplicationBackground,
                complicationColors
            )
        } else if (location == ComplicationLocation.RIGHT) {
            updateComplicationView(
                complicationProviderInfo,
                rightComplication,
                rightComplicationBackground,
                complicationColors
            )
        }
    }

    private fun updateComplicationView(complicationProviderInfo: ComplicationProviderInfo?,
                                       button: ImageButton,
                                       background: ImageView,
                                       complicationColors: ComplicationColors) {
        if (complicationProviderInfo != null) {
            button.setImageIcon(complicationProviderInfo.providerIcon)
            background.setImageDrawable(addedComplicationDrawable)
        } else {
            button.setImageIcon(null)
            background.setImageDrawable(addComplicationDrawable)
        }

        updateComplicationsAccentColor(complicationColors)
    }

    fun updateComplicationsAccentColor(colors: ComplicationColors) {
        if( rightComplication.drawable == addComplicationDrawable ) {
            rightComplication.setColorFilter(Color.WHITE)
        } else {
            rightComplication.setColorFilter(colors.rightColor)
        }

        if( leftComplication.drawable == addComplicationDrawable ) {
            leftComplication.setColorFilter(Color.WHITE)
        } else {
            leftComplication.setColorFilter(colors.leftColor)
        }
    }
}

class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view)

class FooterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val versionTextView: TextView = view.findViewById(R.id.app_version)

    init {
        versionTextView.text = versionTextView.context.getString(R.string.config_version, BuildConfig.VERSION_NAME)
    }
}

class PremiumViewHolder(view: View,
                        premiumClickListener: () -> Unit) : RecyclerView.ViewHolder(view) {
    private val premiumButton: Button = view.findViewById(R.id.premium_button)

    init {
        premiumButton.setOnClickListener {
            premiumClickListener()
        }
    }
}

class HourFormatViewHolder(view: View,
                           hourFormatClickListener: (Boolean) -> Unit) : RecyclerView.ViewHolder(view) {
    private val hourFormatSwitch: Switch = view as Switch

    init {
        hourFormatSwitch.setOnCheckedChangeListener { _, checked ->
            hourFormatClickListener(checked)
        }
    }

    fun setHourFormatSwitchChecked(checked: Boolean) {
        hourFormatSwitch.isChecked = checked
    }
}