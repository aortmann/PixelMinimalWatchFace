package com.benoitletondor.pixelminimalwatchface

import android.content.*
import android.graphics.*
import android.os.Bundle
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.model.Storage
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.settings.ComplicationLocation
import java.text.SimpleDateFormat
import java.util.*

class PixelMinimalWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        val storage = Injection.Storage
        storage.init(this)

        return Engine(this, storage)
    }

    inner class Engine(private val service: WatchFaceService,
                       private val storage: Storage
    ) : CanvasWatchFaceService.Engine() {
        private lateinit var calendar: Calendar
        private var registeredTimeZoneReceiver = false

        private var centerX = 0F
        private var centerY = 0F
        private var width = 0
        private var height = 0

        @ColorInt private var backgroundColor: Int = 0
        @ColorInt private var timeColor: Int = 0
        @ColorInt private var timeColorDimmed: Int = 0
        @ColorInt private var dateColor: Int = 0
        @ColorInt private var dateColorDimmed: Int = 0
        private lateinit var timePaint: Paint
        private lateinit var datePaint: Paint
        private lateinit var wearOSLogo: Bitmap
        private lateinit var wearOSLogoAmbient: Bitmap
        private lateinit var productSansRegularFont: Typeface
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        private lateinit var complicationsHighlightColors: ComplicationColors
        private lateinit var activeComplicationDataSparseArray: SparseArray<ComplicationData>
        private lateinit var complicationDrawableSparseArray: SparseArray<ComplicationDrawable>

        private var muteMode = false
        private var ambient = false
        private var lowBitAmbient = false
        private var burnInProtection = false

        private val timeZoneReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                calendar.timeZone = TimeZone.getDefault()
                invalidate()
            }
        }

        override fun onCreate(holder: SurfaceHolder) {
            super.onCreate(holder)

            setWatchFaceStyle(
                WatchFaceStyle.Builder(service)
                    .setAcceptsTapEvents(true)
                    .build()
            )

            calendar = Calendar.getInstance()

            initializeComplications()
            initializeBackground()
            initializeWatchFace()
        }

        private fun initializeComplications() {
            complicationsHighlightColors = storage.getComplicationColors()
            activeComplicationDataSparseArray = SparseArray(COMPLICATION_IDS.size)

            val leftComplicationDrawable = ComplicationDrawable(service)
            val rightComplicationDrawable = ComplicationDrawable(service)

            complicationDrawableSparseArray = SparseArray(COMPLICATION_IDS.size)

            complicationDrawableSparseArray.put(LEFT_COMPLICATION_ID, leftComplicationDrawable)
            complicationDrawableSparseArray.put(RIGHT_COMPLICATION_ID, rightComplicationDrawable)

            setComplicationsActiveAndAmbientColors(complicationsHighlightColors)
            setActiveComplications(*COMPLICATION_IDS)
        }

        private fun initializeBackground() {
            backgroundColor = ContextCompat.getColor(service, R.color.face_background)
        }

        private fun setComplicationsActiveAndAmbientColors(complicationColors: ComplicationColors) {
            for (complicationId in COMPLICATION_IDS) {
                val complicationDrawable = complicationDrawableSparseArray.get(complicationId)

                val primaryComplicationColor = if( complicationId == LEFT_COMPLICATION_ID ) {
                    complicationColors.leftColor
                } else {
                    complicationColors.rightColor
                }

                complicationDrawable.setIconColorActive(primaryComplicationColor)
                complicationDrawable.setTextColorActive(primaryComplicationColor)
            }
        }

        private fun initializeWatchFace() {
            wearOSLogo = ContextCompat.getDrawable(service, R.drawable.ic_wear_os_logo)!!.toBitmap()
            wearOSLogoAmbient = ContextCompat.getDrawable(service, R.drawable.ic_wear_os_logo_ambient)!!.toBitmap()
            productSansRegularFont = ResourcesCompat.getFont(service, R.font.product_sans_regular)!!
            timeColor = ContextCompat.getColor(service, R.color.face_time)
            timeColorDimmed = ContextCompat.getColor(service, R.color.face_time_dimmed)
            dateColor = ContextCompat.getColor(service, R.color.face_date)
            dateColorDimmed = ContextCompat.getColor(service, R.color.face_date_dimmed)

            timePaint = Paint().apply {
                typeface = productSansRegularFont
            }

            datePaint = Paint().apply {
                typeface = productSansRegularFont
            }
        }

        override fun onDestroy() {
            wearOSLogo.recycle()
            wearOSLogoAmbient.recycle()

            unregisterReceiver()

            super.onDestroy()
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            lowBitAmbient = properties.getBoolean(
                WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false
            )
            burnInProtection = properties.getBoolean(
                WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false
            )
        }

        override fun onApplyWindowInsets(insets: WindowInsets) {
            super.onApplyWindowInsets(insets)

            timePaint.textSize = service.resources.getDimension(
                if( insets.isRound ) {
                    R.dimen.time_text_size_round
                } else {
                    R.dimen.time_text_size
                }
            )

            datePaint.textSize = service.resources.getDimension(
                if( insets.isRound ) {
                    R.dimen.date_text_size_round
                } else {
                    R.dimen.date_text_size
                }
            )
        }

        override fun onTimeTick() {
            super.onTimeTick()

            invalidate()
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            super.onAmbientModeChanged(inAmbientMode)
            ambient = inAmbientMode

            invalidate()
        }

        override fun onInterruptionFilterChanged(interruptionFilter: Int) {
            super.onInterruptionFilterChanged(interruptionFilter)
            val inMuteMode = interruptionFilter == WatchFaceService.INTERRUPTION_FILTER_NONE

            if (muteMode != inMuteMode) {
                muteMode = inMuteMode

                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            this.width = width
            this.height = height

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            centerX = width / 2f
            centerY = height / 2f
        }

        override fun onComplicationDataUpdate(watchFaceComplicationId: Int, data: ComplicationData) {
            super.onComplicationDataUpdate(watchFaceComplicationId, data)

            // Adds/updates active complication data in the array.
            activeComplicationDataSparseArray.put(watchFaceComplicationId, data)

            // Updates correct ComplicationDrawable with updated data.
            val complicationDrawable = complicationDrawableSparseArray.get(watchFaceComplicationId)
            complicationDrawable.setComplicationData(data)

            if( !ambient ) {
                invalidate()
            }
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TAP -> {
                    COMPLICATION_IDS.forEach { complicationId ->
                        val complicationDrawable: ComplicationDrawable = complicationDrawableSparseArray.get(complicationId)

                        if ( complicationDrawable.onTap(x, y) ) {
                            return
                        }
                    }
                }
            }
        }


        override fun onDraw(canvas: Canvas, bounds: Rect) {
            calendar.timeInMillis = System.currentTimeMillis()

            drawBackground(canvas)
            drawWatchFace(canvas)
        }

        private fun drawBackground(canvas: Canvas) {
            canvas.drawColor(backgroundColor)
        }

        private fun drawWatchFace(canvas: Canvas) {
            timePaint.apply {
                isAntiAlias = !(ambient && lowBitAmbient)
                style = if( ambient ) { Paint.Style.STROKE } else { Paint.Style.FILL }
                color = if( ambient ) { timeColorDimmed } else { timeColor }
            }

            datePaint.apply {
                isAntiAlias = !(ambient && lowBitAmbient)
                color = if( ambient ) { dateColorDimmed } else { dateColor }
            }

            val timeText = timeFormatter.format(calendar.time)
            val timeTextBounds = Rect().apply {
                timePaint.getTextBounds(timeText, 0, timeText.length, this)
            }
            val timeYOffset = centerY + (timeTextBounds.height() / 2.0f ) - 5f
            val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
            canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

            drawComplications(canvas, timeYOffset - timeTextBounds.height() - 10f)

            val dateText = DateUtils.formatDateTime(service, calendar.timeInMillis, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)
            val dateTextBounds = Rect().apply {
                datePaint.getTextBounds(dateText, 0, dateText.length, this)
            }
            val dateYOffset = timeYOffset + (timeTextBounds.height() / 2) - (dateTextBounds.height() / 2.0f ) + 10f
            val dateXOffset = centerX - (datePaint.measureText(dateText) / 2f)
            canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)
        }

        private fun drawComplications(canvas: Canvas, bottomY: Float) {
            val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
            val sizeOfComplication = width / 5

            val verticalOffset = bottomY.toInt() - sizeOfComplication

            val leftBounds = Rect(
                (centerX - (wearOsImage.width / 2) - 15f - sizeOfComplication).toInt(),
                verticalOffset,
                (centerX - (wearOsImage.width / 2)  - 15f).toInt(),
                (verticalOffset + sizeOfComplication)
            )

            val leftComplicationDrawable = complicationDrawableSparseArray.get(LEFT_COMPLICATION_ID)
            leftComplicationDrawable.bounds = leftBounds

            val rightBounds = Rect(
                (centerX + (wearOsImage.width / 2) + 15f).toInt(),
                verticalOffset,
                (centerX + (wearOsImage.width / 2)  + 15f + sizeOfComplication).toInt(),
                (verticalOffset + sizeOfComplication)
            )

            val rightComplicationDrawable = complicationDrawableSparseArray.get(RIGHT_COMPLICATION_ID)
            rightComplicationDrawable.bounds = rightBounds

            if( !ambient ) {
                COMPLICATION_IDS.forEach { complicationId ->
                    val complicationDrawable = complicationDrawableSparseArray.get(complicationId)
                    complicationDrawable.draw(canvas, calendar.timeInMillis)
                }
            }

            val iconXOffset = centerX - (wearOsImage.width / 2.0f)
            val iconYOffset = leftBounds.top + (leftBounds.height() / 2) - (wearOsImage.height / 2)
            canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset.toFloat(), null)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()

                /* Update time zone in case it changed while we weren't visible. */
                calendar.timeZone = TimeZone.getDefault()

                complicationsHighlightColors = storage.getComplicationColors()
                setComplicationsActiveAndAmbientColors(complicationsHighlightColors)

                invalidate()
            } else {
                unregisterReceiver()
            }
        }

        private fun registerReceiver() {
            if (registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = true
            val filter = IntentFilter(Intent.ACTION_TIMEZONE_CHANGED)
            service.registerReceiver(timeZoneReceiver, filter)
        }

        private fun unregisterReceiver() {
            if (!registeredTimeZoneReceiver) {
                return
            }
            registeredTimeZoneReceiver = false
            service.unregisterReceiver(timeZoneReceiver)
        }
    }

    companion object {
        private const val LEFT_COMPLICATION_ID = 100
        private const val RIGHT_COMPLICATION_ID = 101

        private val COMPLICATION_IDS = intArrayOf(
            LEFT_COMPLICATION_ID, RIGHT_COMPLICATION_ID
        )

        private val COMPLICATION_SUPPORTED_TYPES = arrayOf(
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            ),
            intArrayOf(
                ComplicationData.TYPE_RANGED_VALUE,
                ComplicationData.TYPE_ICON,
                ComplicationData.TYPE_SHORT_TEXT,
                ComplicationData.TYPE_SMALL_IMAGE
            )
        )

        fun getComplicationId(complicationLocation: ComplicationLocation): Int {
            return when (complicationLocation) {
                ComplicationLocation.LEFT -> LEFT_COMPLICATION_ID
                ComplicationLocation.RIGHT -> RIGHT_COMPLICATION_ID
            }
        }

        fun getSupportedComplicationTypes(complicationLocation: ComplicationLocation): IntArray {
            return when (complicationLocation) {
                ComplicationLocation.LEFT -> COMPLICATION_SUPPORTED_TYPES[0]
                ComplicationLocation.RIGHT -> COMPLICATION_SUPPORTED_TYPES[1]
            }
        }

        fun getComplicationIds(): IntArray {
            return COMPLICATION_IDS
        }
    }
}


