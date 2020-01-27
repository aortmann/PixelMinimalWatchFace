package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.graphics.*
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.text.format.DateUtils
import android.text.format.DateUtils.FORMAT_SHOW_DATE
import android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY
import android.util.ArrayMap
import android.util.SparseArray
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.LEFT_COMPLICATION_ID
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.RIGHT_COMPLICATION_ID
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import java.text.SimpleDateFormat
import java.util.*

interface WatchFaceDrawer {
    fun onCreate(context: Context)

    fun onApplyWindowInsets(insets: WindowInsets)
    fun onSurfaceChanged(width: Int, height: Int)
    fun setComplicationDrawable(complicationId: Int, complicationDrawable: ComplicationDrawable)
    fun setComplicationsColors(complicationColors: ComplicationColors, complicationsData: SparseArray<ComplicationData>)

    fun draw(canvas: Canvas,
             currentTime: Date,
             muteMode: Boolean,
             ambient:Boolean,
             lowBitAmbient: Boolean,
             burnInProtection: Boolean)
}

class WatchFaceDrawerImpl : WatchFaceDrawer {
    private lateinit var context: Context
    private var drawingState: DrawingState = DrawingState.NoScreenData
    private val complicationsDrawable: MutableMap<Int, ComplicationDrawable> = ArrayMap()

    private lateinit var wearOSLogoPaint: Paint
    private lateinit var timePaint: Paint
    private lateinit var datePaint: Paint
    @ColorInt private var backgroundColor: Int = 0
    @ColorInt private var timeColor: Int = 0
    @ColorInt private var timeColorDimmed: Int = 0
    @ColorInt private var dateColor: Int = 0
    @ColorInt private var dateColorDimmed: Int = 0
    @ColorInt private var complicationTitleColor: Int = 0
    private lateinit var wearOSLogo: Bitmap
    private lateinit var wearOSLogoAmbient: Bitmap
    private lateinit var productSansRegularFont: Typeface
    private lateinit var timeFormatter: SimpleDateFormat

    override fun onCreate(context: Context) {
        this.context = context

        wearOSLogoPaint = Paint()
        backgroundColor = ContextCompat.getColor(context, R.color.face_background)
        timeColor = ContextCompat.getColor(context, R.color.face_time)
        timeColorDimmed = ContextCompat.getColor(context, R.color.face_time_dimmed)
        dateColor = ContextCompat.getColor(context, R.color.face_date)
        dateColorDimmed = ContextCompat.getColor(context, R.color.face_date_dimmed)
        complicationTitleColor = ContextCompat.getColor(context, R.color.complication_title_color)
        wearOSLogo = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo)!!.toBitmap()
        wearOSLogoAmbient = ContextCompat.getDrawable(context, R.drawable.ic_wear_os_logo_ambient)!!.toBitmap()
        productSansRegularFont = ResourcesCompat.getFont(context, R.font.product_sans_regular)!!
        timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        timePaint = Paint().apply {
            typeface = productSansRegularFont
        }
        datePaint = Paint().apply {
            typeface = productSansRegularFont
        }
    }

    override fun onApplyWindowInsets(insets: WindowInsets) {
        timePaint.textSize = context.resources.getDimension(
            if( insets.isRound ) {
                R.dimen.time_text_size_round
            } else {
                R.dimen.time_text_size
            }
        )

        datePaint.textSize = context.resources.getDimension(
            if( insets.isRound ) {
                R.dimen.date_text_size_round
            } else {
                R.dimen.date_text_size
            }
        )
    }

    override fun onSurfaceChanged(width: Int, height: Int) {
        drawingState = DrawingState.NoCache(
            width,
            height,
            width / 2f,
            height / 2f
        )
    }

    override fun setComplicationDrawable(complicationId: Int, complicationDrawable: ComplicationDrawable) {
        complicationsDrawable[complicationId] = complicationDrawable
    }

    override fun setComplicationsColors(complicationColors: ComplicationColors, complicationsData: SparseArray<ComplicationData>) {
        complicationsDrawable.forEach { (complicationId, complicationDrawable) ->
            val complicationData = complicationsData.get(complicationId)

            val primaryComplicationColor = if( complicationId == LEFT_COMPLICATION_ID ) {
                complicationColors.leftColor
            } else {
                complicationColors.rightColor
            }

            complicationDrawable.setTitleColorActive(complicationTitleColor)
            complicationDrawable.setIconColorActive(primaryComplicationColor)
            complicationDrawable.setTextTypefaceActive(productSansRegularFont)
            complicationDrawable.setTitleTypefaceActive(productSansRegularFont)
            if( complicationData == null || complicationData.icon == null ) {
                complicationDrawable.setTextColorActive(primaryComplicationColor)
            } else {
                complicationDrawable.setTextColorActive(complicationTitleColor)
            }
        }
    }

    override fun draw(canvas: Canvas,
                      currentTime: Date,
                      muteMode: Boolean,
                      ambient:Boolean,
                      lowBitAmbient: Boolean,
                      burnInProtection: Boolean) {

        setPaintVariables(muteMode, ambient, lowBitAmbient, burnInProtection)
        drawBackground(canvas)
        when( val drawingState = drawingState ) {
            is DrawingState.NoCache -> drawingState.drawWithoutCache(canvas, currentTime, muteMode, ambient, lowBitAmbient, burnInProtection)
            is DrawingState.Cache -> drawingState.drawWithCache(canvas, currentTime, muteMode, ambient, lowBitAmbient, burnInProtection)
        }
    }

    private fun DrawingState.NoCache.drawWithoutCache(canvas: Canvas,
                                                      currentTime: Date,
                                                      muteMode: Boolean,
                                                      ambient:Boolean,
                                                      lowBitAmbient: Boolean,
                                                      burnInProtection: Boolean) {
        val timeText = timeFormatter.format(currentTime)
        val timeTextBounds = Rect().apply {
            timePaint.getTextBounds(timeText, 0, timeText.length, this)
        }
        val timeYOffset = centerY + (timeTextBounds.height() / 2.0f ) - 5f
        val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
        canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

        val complicationsDrawingCache = drawComplicationsAndBuildCache(canvas, ambient, currentTime, timeYOffset - timeTextBounds.height() - 10f)

        val dateText = DateUtils.formatDateTime(context, currentTime.time, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)
        val dateTextBounds = Rect().apply {
            datePaint.getTextBounds(dateText, 0, dateText.length, this)
        }
        val dateYOffset = timeYOffset + (timeTextBounds.height() / 2) - (dateTextBounds.height() / 2.0f ) + 10f
        val dateXOffset = centerX - (datePaint.measureText(dateText) / 2f)
        canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)

        drawingState = DrawingState.Cache(
            screenWidth,
            screenHeight,
            centerX,
            centerY,
            timeYOffset,
            dateYOffset,
            complicationsDrawingCache
        )
    }

    private fun DrawingState.NoCache.drawComplicationsAndBuildCache(canvas: Canvas,
                                                                    ambient: Boolean,
                                                                    currentTime: Date,
                                                                    bottomY: Float): ComplicationsDrawingCache {
        val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }

        val sizeOfComplication = screenWidth / 5
        val verticalOffset = bottomY.toInt() - sizeOfComplication

        val leftBounds = Rect(
            (centerX - (wearOsImage.width / 2) - 15f - sizeOfComplication).toInt(),
            verticalOffset,
            (centerX - (wearOsImage.width / 2)  - 15f).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationsDrawable[LEFT_COMPLICATION_ID]?.let { leftComplicationDrawable ->
            leftComplicationDrawable.bounds = leftBounds
            if( !ambient ) {
                leftComplicationDrawable.draw(canvas, currentTime.time)
            }
        }

        val rightBounds = Rect(
            (centerX + (wearOsImage.width / 2) + 15f).toInt(),
            verticalOffset,
            (centerX + (wearOsImage.width / 2)  + 15f + sizeOfComplication).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationsDrawable[RIGHT_COMPLICATION_ID]?.let { rightComplicationDrawable ->
            rightComplicationDrawable.bounds = rightBounds
            if( !ambient ) {
                rightComplicationDrawable.draw(canvas, currentTime.time)
            }
        }

        val iconXOffset = centerX - (wearOsImage.width / 2.0f)
        val iconYOffset = leftBounds.top + (leftBounds.height() / 2) - (wearOsImage.height / 2)
        canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset.toFloat(), wearOSLogoPaint)

        return ComplicationsDrawingCache(
            iconXOffset,
            iconYOffset.toFloat()
        )
    }

    private fun DrawingState.Cache.drawWithCache(canvas: Canvas,
                                                 currentTime: Date,
                                                 muteMode: Boolean,
                                                 ambient:Boolean,
                                                 lowBitAmbient: Boolean,
                                                 burnInProtection: Boolean) {
        val timeText = timeFormatter.format(currentTime)
        val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
        canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

        complicationsDrawingCache.drawComplications(canvas, ambient, currentTime)

        val dateText = DateUtils.formatDateTime(context, currentTime.time, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY)
        val dateXOffset = centerX - (datePaint.measureText(dateText) / 2f)
        canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)
    }

    private fun ComplicationsDrawingCache.drawComplications(canvas: Canvas, ambient: Boolean, currentTime: Date) {
        if( !ambient ) {
            complicationsDrawable.values.forEach { complicationDrawable ->
                complicationDrawable.draw(canvas, currentTime.time)
            }
        }

        val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
        canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset, wearOSLogoPaint)
    }

    private fun drawBackground(canvas: Canvas) {
        canvas.drawColor(backgroundColor)
    }

    private fun setPaintVariables(muteMode: Boolean,
                                  ambient:Boolean,
                                  lowBitAmbient: Boolean,
                                  burnInProtection: Boolean) {
        wearOSLogoPaint.isAntiAlias = !ambient

        timePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            style = if( ambient ) { Paint.Style.STROKE } else { Paint.Style.FILL }
            color = if( ambient ) { timeColorDimmed } else { timeColor }
        }

        datePaint.apply {
            isAntiAlias = !(ambient && lowBitAmbient)
            color = if( ambient ) { dateColorDimmed } else { dateColor }
        }
    }

}

private sealed class DrawingState {
    object NoScreenData : DrawingState()
    data class NoCache(val screenWidth: Int,
                       val screenHeight: Int,
                       val centerX: Float,
                       val centerY: Float) : DrawingState()
    data class Cache(val screenWidth: Int,
                     val screenHeight: Int,
                     val centerX: Float,
                     val centerY: Float,
                     val timeYOffset: Float,
                     val dateYOffset: Float,
                     val complicationsDrawingCache: ComplicationsDrawingCache) : DrawingState()
}

private data class ComplicationsDrawingCache(
    val iconXOffset: Float,
    val iconYOffset: Float
)