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
package com.benoitletondor.pixelminimalwatchface

import android.content.Context
import android.graphics.*
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.rendering.ComplicationDrawable
import android.text.format.DateUtils.*
import android.util.ArrayMap
import android.util.SparseArray
import android.view.WindowInsets
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.LEFT_COMPLICATION_ID
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.MIDDLE_COMPLICATION_ID
import com.benoitletondor.pixelminimalwatchface.PixelMinimalWatchFace.Companion.RIGHT_COMPLICATION_ID
import com.benoitletondor.pixelminimalwatchface.helper.toBitmap
import com.benoitletondor.pixelminimalwatchface.model.ComplicationColors
import com.benoitletondor.pixelminimalwatchface.model.Storage
import java.text.SimpleDateFormat
import java.util.*

interface WatchFaceDrawer {
    fun onCreate(context: Context, storage: Storage)

    fun onApplyWindowInsets(insets: WindowInsets)
    fun onSurfaceChanged(width: Int, height: Int)
    fun setComplicationDrawable(complicationId: Int, complicationDrawable: ComplicationDrawable)
    fun onComplicationColorsUpdate(complicationColors: ComplicationColors, complicationsData: SparseArray<ComplicationData>)
    fun onComplicationDataUpdate(complicationId: Int,
                                 complicationDrawable: ComplicationDrawable,
                                 data: ComplicationData?,
                                 complicationColors: ComplicationColors)

    fun draw(canvas: Canvas,
             currentTime: Date,
             muteMode: Boolean,
             ambient:Boolean,
             lowBitAmbient: Boolean,
             burnInProtection: Boolean)
}

class WatchFaceDrawerImpl : WatchFaceDrawer {
    private lateinit var storage: Storage
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
    private lateinit var timeFormatter24H: SimpleDateFormat
    private lateinit var timeFormatter12H: SimpleDateFormat

    override fun onCreate(context: Context, storage: Storage) {
        this.context = context
        this.storage = storage

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
        timeFormatter24H = SimpleDateFormat("HH:mm", Locale.getDefault())
        timeFormatter12H = SimpleDateFormat("h:mm", Locale.getDefault())
        timePaint = Paint().apply {
            typeface = productSansRegularFont
            strokeWidth = 1.5f
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
        drawingState = DrawingState.NoCacheAvailable(
            width,
            height,
            width / 2f,
            height / 2f
        )
    }

    override fun setComplicationDrawable(complicationId: Int, complicationDrawable: ComplicationDrawable) {
        complicationsDrawable[complicationId] = complicationDrawable
    }

    override fun onComplicationColorsUpdate(complicationColors: ComplicationColors, complicationsData: SparseArray<ComplicationData>) {
        complicationsDrawable.forEach { (complicationId, complicationDrawable) ->
            val primaryComplicationColor = getComplicationPrimaryColor(complicationId, complicationColors)

            complicationDrawable.setTitleColorActive(complicationTitleColor)
            complicationDrawable.setIconColorActive(primaryComplicationColor)
            complicationDrawable.setTextTypefaceActive(productSansRegularFont)
            complicationDrawable.setTitleTypefaceActive(productSansRegularFont)

            onComplicationDataUpdate(complicationId, complicationDrawable, complicationsData.get(complicationId), complicationColors)
        }
    }

    override fun onComplicationDataUpdate(complicationId: Int,
                                          complicationDrawable: ComplicationDrawable,
                                          data: ComplicationData?,
                                          complicationColors: ComplicationColors) {
        val primaryComplicationColor = getComplicationPrimaryColor(complicationId, complicationColors)
        if( data != null && data.icon != null ) {
            complicationDrawable.setTextColorActive(complicationTitleColor)
        } else {
            complicationDrawable.setTextColorActive(primaryComplicationColor)
        }
    }

    @ColorInt
    private fun getComplicationPrimaryColor(complicationId: Int, complicationColors: ComplicationColors): Int {
        return when (complicationId) {
            LEFT_COMPLICATION_ID -> { complicationColors.leftColor }
            MIDDLE_COMPLICATION_ID -> { complicationColors.middleColor }
            else -> { complicationColors.rightColor }
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

        val currentDrawingState = drawingState
        if( currentDrawingState is DrawingState.NoCacheAvailable ) {
            drawingState = currentDrawingState.buildCache()
        }

        val drawingState = drawingState
        if( drawingState is DrawingState.CacheAvailable ){
            drawingState.draw(canvas, currentTime, muteMode, ambient, lowBitAmbient, burnInProtection, storage.isUserPremium())
        }
    }

    private fun DrawingState.NoCacheAvailable.buildCache(): DrawingState.CacheAvailable {
        val timeText = "22:13"
        val timeTextBounds = Rect().apply {
            timePaint.getTextBounds(timeText, 0, timeText.length, this)
        }
        val timeYOffset = centerY + (timeTextBounds.height() / 2.0f ) - 5f

        val complicationsDrawingCache = buildComplicationDrawingCache(timeYOffset - timeTextBounds.height() - 10f)

        val dateText = "May, 15"
        val dateTextBounds = Rect().apply {
            datePaint.getTextBounds(dateText, 0, dateText.length, this)
        }
        val dateYOffset = timeYOffset + (timeTextBounds.height() / 2) - (dateTextBounds.height() / 2.0f ) + 20f

        return DrawingState.CacheAvailable(
            screenWidth,
            screenHeight,
            centerX,
            centerY,
            timeYOffset,
            dateYOffset,
            complicationsDrawingCache
        )
    }

    private fun DrawingState.NoCacheAvailable.buildComplicationDrawingCache(bottomY: Float): ComplicationsDrawingCache {
        val wearOsImage = wearOSLogo

        val sizeOfComplication = (screenWidth / 4.5).toInt()
        val verticalOffset = bottomY.toInt() - sizeOfComplication

        val leftBounds = Rect(
            (centerX - (wearOsImage.width / 2) - 15f - sizeOfComplication).toInt(),
            verticalOffset,
            (centerX - (wearOsImage.width / 2)  - 15f).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationsDrawable[LEFT_COMPLICATION_ID]?.let { leftComplicationDrawable ->
            leftComplicationDrawable.bounds = leftBounds
        }

        val middleBounds = Rect(
            (centerX - (sizeOfComplication / 2)).toInt(),
            leftBounds.top,
            (centerX + (sizeOfComplication / 2)).toInt(),
            leftBounds.bottom
        )

        complicationsDrawable[MIDDLE_COMPLICATION_ID]?.let { middleComplicationDrawable ->
            middleComplicationDrawable.bounds = middleBounds
        }

        val rightBounds = Rect(
            (centerX + (wearOsImage.width / 2) + 15f).toInt(),
            verticalOffset,
            (centerX + (wearOsImage.width / 2)  + 15f + sizeOfComplication).toInt(),
            (verticalOffset + sizeOfComplication)
        )

        complicationsDrawable[RIGHT_COMPLICATION_ID]?.let { rightComplicationDrawable ->
            rightComplicationDrawable.bounds = rightBounds
        }

        val iconXOffset = centerX - (wearOsImage.width / 2.0f)
        val iconYOffset = leftBounds.top + (leftBounds.height() / 2) - (wearOsImage.height / 2)

        return ComplicationsDrawingCache(
            iconXOffset,
            iconYOffset.toFloat()
        )
    }

    private fun DrawingState.CacheAvailable.draw(canvas: Canvas,
                                                 currentTime: Date,
                                                 muteMode: Boolean,
                                                 ambient:Boolean,
                                                 lowBitAmbient: Boolean,
                                                 burnInProtection: Boolean,
                                                 isUserPremium: Boolean) {
        val timeText = if( storage.getUse24hTimeFormat()) {
            timeFormatter24H.format(currentTime)
        } else {
            timeFormatter12H.format(currentTime)
        }
        val timeXOffset = centerX - (timePaint.measureText(timeText) / 2f)
        canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint)

        complicationsDrawingCache.drawComplications(canvas, ambient, currentTime, isUserPremium)

        val dateText = formatDateTime(context, currentTime.time, FORMAT_SHOW_DATE or FORMAT_SHOW_WEEKDAY or FORMAT_ABBREV_WEEKDAY)
        val dateXOffset = centerX - (datePaint.measureText(dateText) / 2f)
        canvas.drawText(dateText, dateXOffset, dateYOffset, datePaint)
    }

    private fun ComplicationsDrawingCache.drawComplications(canvas: Canvas, ambient: Boolean, currentTime: Date, isUserPremium: Boolean) {
        if( !ambient && isUserPremium ) {
            complicationsDrawable.forEach { (complicationId, complicationDrawable) ->
                if( complicationId != MIDDLE_COMPLICATION_ID || !storage.shouldShowWearOSLogo() ) {
                    complicationDrawable.draw(canvas, currentTime.time)
                }
            }
        }

        if( storage.shouldShowWearOSLogo() ) {
            val wearOsImage = if( ambient ) { wearOSLogoAmbient } else { wearOSLogo }
            canvas.drawBitmap(wearOsImage, iconXOffset, iconYOffset, wearOSLogoPaint)
        }
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
    data class NoCacheAvailable(val screenWidth: Int,
                                val screenHeight: Int,
                                val centerX: Float,
                                val centerY: Float) : DrawingState()
    data class CacheAvailable(val screenWidth: Int,
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