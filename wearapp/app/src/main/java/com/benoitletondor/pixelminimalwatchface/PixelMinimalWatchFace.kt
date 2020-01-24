package com.benoitletondor.pixelminimalwatchface

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.os.Bundle
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.view.SurfaceHolder
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.text.SimpleDateFormat
import java.util.*

class PixelMinimalWatchFace : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine(this)
    }

    inner class Engine(private val service: WatchFaceService) : CanvasWatchFaceService.Engine() {
        private lateinit var calendar: Calendar
        private var registeredTimeZoneReceiver = false

        private var centerX = 0F
        private var centerY = 0F

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
        private lateinit var productSansBoldFont: Typeface
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

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

            initializeBackground()
            initializeWatchFace()
        }

        private fun initializeBackground() {
            backgroundColor = ContextCompat.getColor(service, R.color.face_background)
        }

        private fun initializeWatchFace() {
            wearOSLogo = ContextCompat.getDrawable(service, R.drawable.ic_wear_os_logo)!!.toBitmap()
            wearOSLogoAmbient = ContextCompat.getDrawable(service, R.drawable.ic_wear_os_logo_ambient)!!.toBitmap()
            productSansRegularFont = ResourcesCompat.getFont(service, R.font.product_sans_regular)!!
            productSansBoldFont = ResourcesCompat.getFont(service, R.font.product_sans_bold)!!
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

            /* Dim display in mute mode. */
            if (muteMode != inMuteMode) {
                muteMode = inMuteMode

                invalidate()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            /*
             * Find the coordinates of the center point on the screen, and ignore the window
             * insets, so that, on round watches with a "chin", the watch face is centered on the
             * entire screen, not just the usable portion.
             */
            centerX = width / 2f
            centerY = height / 2f
        }

        /**
         * Captures tap event (and tap type). The [WatchFaceService.TAP_TYPE_TAP] case can be
         * used for implementing specific logic to handle the gesture.
         */
        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TOUCH -> {
                    // The user has started touching the screen.
                }
                WatchFaceService.TAP_TYPE_TOUCH_CANCEL -> {
                    // The user has started a different gesture or otherwise cancelled the tap.
                }
                WatchFaceService.TAP_TYPE_TAP -> {
                    // The user has completed the tap gesture.
                    // TODO: Add code to handle the tap gesture.
                }
            }

            invalidate()
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
            val timeYOffset = centerY + (timeTextBounds.height() / 2.0f )
            val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
            canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

            val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
            val iconXOffset = centerX - (wearOsImage.width / 2.0f)
            val iconYOffset = timeYOffset - timeTextBounds.height() - wearOsImage.height - 16f
            canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset, null)

            val dateText = DateUtils.formatDateTime(service, calendar.timeInMillis, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)
            val dateTextBounds = Rect().apply {
                datePaint.getTextBounds(dateText, 0, dateText.length, this)
            }
            val dateYOffset = timeYOffset + (timeTextBounds.height() / 2) - (dateTextBounds.height() / 2.0f ) + 10f
            val dateXOffset = centerX - (datePaint.measureText(dateText) / 2f)
            canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)

            if (visible) {
                registerReceiver()
                /* Update time zone in case it changed while we weren't visible. */
                calendar.timeZone = TimeZone.getDefault()
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
}


