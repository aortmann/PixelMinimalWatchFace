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
package android.support.wearable.complications.rendering;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.rendering.utils.IconLayoutHelper;
import android.support.wearable.complications.rendering.utils.LargeImageLayoutHelper;
import android.support.wearable.complications.rendering.utils.LayoutHelper;
import android.support.wearable.complications.rendering.utils.LayoutUtils;
import android.support.wearable.complications.rendering.utils.LongTextLayoutHelper;
import android.support.wearable.complications.rendering.utils.RangedValueLayoutHelper;
import android.support.wearable.complications.rendering.utils.ShortTextLayoutHelper;
import android.support.wearable.complications.rendering.utils.SmallImageLayoutHelper;
import android.text.Layout;
import android.text.TextPaint;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.benoitletondor.pixelminimalwatchface.helper.ContextExtensionKt;

import java.util.Objects;

import static android.text.Layout.Alignment.ALIGN_CENTER;

/**
 * This is a copy of ComplicationRenderer code with bad hacks, don't look at it, it's horrible!
 */
public class CustomComplicationRenderer extends ComplicationRenderer {
    private final Context mContext;
    private ComplicationData mComplicationData;
    private final Rect mBounds = new Rect();
    private CharSequence mNoDataText = "";
    private boolean mRangedValueProgressHidden;
    private boolean mHasNoData;
    @Nullable
    private Drawable mIcon;
    @Nullable
    private Drawable mBurnInProtectionIcon;
    @Nullable
    private Drawable mSmallImage;
    @Nullable
    private Drawable mBurnInProtectionSmallImage;
    @Nullable
    private Drawable mLargeImage;
    private final RoundedDrawable mRoundedBackgroundDrawable = new RoundedDrawable();
    private final RoundedDrawable mRoundedLargeImage = new RoundedDrawable();
    private final RoundedDrawable mRoundedSmallImage = new RoundedDrawable();
    private final TextRenderer mMainTextRenderer = new CustomTextRenderer();
    private final TextRenderer mSubTextRenderer = new CustomTextRenderer();
    private final Rect mBackgroundBounds = new Rect();
    private final RectF mBackgroundBoundsF = new RectF();
    private final Rect mIconBounds = new Rect();
    private final Rect mSmallImageBounds = new Rect();
    private final Rect mLargeImageBounds = new Rect();
    private final Rect mMainTextBounds = new Rect();
    private final Rect mSubTextBounds = new Rect();
    private final Rect mRangedValueBounds = new Rect();
    private final RectF mRangedValueBoundsF = new RectF();
    @VisibleForTesting
    ComplicationRenderer.PaintSet mActivePaintSet = null;
    @VisibleForTesting
    ComplicationRenderer.PaintSet mAmbientPaintSet = null;
    @Nullable
    private TextPaint mMainTextPaint = null;
    @Nullable
    private TextPaint mSubTextPaint = null;
    private ComplicationStyle mActiveStyle;
    private ComplicationStyle mAmbientStyle;
    @Nullable
    private Paint mDebugPaint;
    @Nullable
    private ComplicationRenderer.OnInvalidateListener mInvalidateListener;
    private final Rect mMainTextAdjustedBounds = new Rect();
    private final Rect mSubTextAdjustedBounds = new Rect();
    private int mTextPadding;
    private boolean mIsWide;
    private int mMinHeightFor2LinesTextOnWideComplication;

    public CustomComplicationRenderer(Context context, ComplicationStyle activeStyle, ComplicationStyle ambientStyle, boolean isWide) {
        super(context, activeStyle, ambientStyle);
        this.mContext = context;
        this.updateStyle(activeStyle, ambientStyle);
        this.mIsWide = isWide;
        this.mTextPadding = ContextExtensionKt.dpToPx(context, 5);
        this.mMinHeightFor2LinesTextOnWideComplication = ContextExtensionKt.dpToPx(context, 25);
    }

    public void updateStyle(ComplicationStyle activeStyle, ComplicationStyle ambientStyle) {
        this.mActiveStyle = activeStyle;
        this.mAmbientStyle = ambientStyle;
        this.mActivePaintSet = new ComplicationRenderer.PaintSet(activeStyle, false, false, false);
        this.mAmbientPaintSet = new ComplicationRenderer.PaintSet(ambientStyle, true, false, false);
        this.calculateBounds();
    }

    public void setComplicationData(@Nullable ComplicationData data) {
        if (!Objects.equals(this.mComplicationData, data)) {
            if (data == null) {
                this.mComplicationData = null;
            } else {
                if (data.getType() == 10) {
                    if (this.mHasNoData) {
                        return;
                    }

                    this.mHasNoData = true;
                    this.mComplicationData = (new ComplicationData.Builder(3)).setShortText(ComplicationText.plainText(this.mNoDataText)).build();
                } else {
                    this.mComplicationData = data;
                    this.mHasNoData = false;
                }

                if (!this.loadDrawableIconAndImages()) {
                    this.invalidate();
                }

                this.calculateBounds();
            }
        }
    }

    public boolean setBounds(Rect bounds) {
        boolean shouldCalculateBounds = true;
        if (this.mBounds.width() == bounds.width() && this.mBounds.height() == bounds.height()) {
            shouldCalculateBounds = false;
        }

        this.mBounds.set(bounds);
        if (shouldCalculateBounds) {
            this.calculateBounds();
        }

        return shouldCalculateBounds;
    }

    public void setNoDataText(@Nullable CharSequence noDataText) {
        if (noDataText == null) {
            noDataText = "";
        }

        this.mNoDataText = ((CharSequence)noDataText).subSequence(0, ((CharSequence)noDataText).length());
        if (this.mHasNoData) {
            this.mHasNoData = false;
            this.setComplicationData((new ComplicationData.Builder(10)).build());
        }

    }

    public void setRangedValueProgressHidden(boolean hidden) {
        if (this.mRangedValueProgressHidden != hidden) {
            this.mRangedValueProgressHidden = hidden;
            this.calculateBounds();
        }

    }

    boolean isRangedValueProgressHidden() {
        return this.mRangedValueProgressHidden;
    }

    public void draw(Canvas canvas, long currentTimeMillis, boolean inAmbientMode, boolean lowBitAmbient, boolean burnInProtection, boolean showTapHighlight) {
        if (this.mComplicationData != null && this.mComplicationData.getType() != 2 && this.mComplicationData.getType() != 1 && this.mComplicationData.isActive(currentTimeMillis)) {
            if (!this.mBounds.isEmpty()) {
                if (inAmbientMode && (this.mAmbientPaintSet.lowBitAmbient != lowBitAmbient || this.mAmbientPaintSet.burnInProtection != burnInProtection)) {
                    this.mAmbientPaintSet = new ComplicationRenderer.PaintSet(this.mAmbientStyle, true, lowBitAmbient, burnInProtection);
                }

                ComplicationRenderer.PaintSet currentPaintSet = inAmbientMode ? this.mAmbientPaintSet : this.mActivePaintSet;
                this.updateComplicationTexts(currentTimeMillis);
                canvas.save();
                canvas.translate((float)this.mBounds.left, (float)this.mBounds.top);
                this.drawBackground(canvas, currentPaintSet);
                this.drawIcon(canvas, currentPaintSet);
                this.drawSmallImage(canvas, currentPaintSet);
                this.drawLargeImage(canvas, currentPaintSet);
                this.drawRangedValue(canvas, currentPaintSet);
                this.drawMainText(canvas, currentPaintSet);
                this.drawSubText(canvas, currentPaintSet);
                if (showTapHighlight) {
                    this.drawHighlight(canvas, currentPaintSet);
                }

                this.drawBorders(canvas, currentPaintSet);
                canvas.restore();
            }
        }
    }

    public void setOnInvalidateListener(ComplicationRenderer.OnInvalidateListener listener) {
        this.mInvalidateListener = listener;
    }

    private void invalidate() {
        if (this.mInvalidateListener != null) {
            this.mInvalidateListener.onInvalidate();
        }

    }

    private void updateComplicationTexts(long currentTimeMillis) {
        if (this.mComplicationData.getShortText() != null) {
            this.mMainTextRenderer.setMaxLines(1);
            this.mMainTextRenderer.setText(this.mComplicationData.getShortText().getText(this.mContext, currentTimeMillis));
            if (this.mComplicationData.getShortTitle() != null) {
                this.mSubTextRenderer.setText(this.mComplicationData.getShortTitle().getText(this.mContext, currentTimeMillis));
            } else {
                this.mSubTextRenderer.setText("");
            }
        }

        if (this.mComplicationData.getLongText() != null) {
            this.mMainTextRenderer.setText(this.mComplicationData.getLongText().getText(this.mContext, currentTimeMillis));
            if (this.mComplicationData.getLongTitle() != null) {
                this.mSubTextRenderer.setText(this.mComplicationData.getLongTitle().getText(this.mContext, currentTimeMillis));
                this.mMainTextRenderer.setMaxLines(1);
            } else {
                this.mSubTextRenderer.setText("");
                this.mMainTextRenderer.setMaxLines(2);
            }
        }

    }

    private void drawBackground(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        int radius = this.getBorderRadius(paintSet.style);
        canvas.drawRoundRect(this.mBackgroundBoundsF, (float)radius, (float)radius, paintSet.backgroundPaint);
        if (paintSet.style.getBackgroundDrawable() != null && !paintSet.isInBurnInProtectionMode()) {
            this.mRoundedBackgroundDrawable.setDrawable(paintSet.style.getBackgroundDrawable());
            this.mRoundedBackgroundDrawable.setRadius(radius);
            this.mRoundedBackgroundDrawable.setBounds(this.mBackgroundBounds);
            this.mRoundedBackgroundDrawable.draw(canvas);
        }

    }

    private void drawBorders(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (paintSet.style.getBorderStyle() != 0) {
            int radius = this.getBorderRadius(paintSet.style);
            canvas.drawRoundRect(this.mBackgroundBoundsF, (float)radius, (float)radius, paintSet.borderPaint);
        }

    }

    private void drawHighlight(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!paintSet.isAmbientStyle) {
            int radius = this.getBorderRadius(paintSet.style);
            canvas.drawRoundRect(this.mBackgroundBoundsF, (float)radius, (float)radius, paintSet.highlightPaint);
        }

    }

    void drawMainText(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mMainTextBounds.isEmpty()) {
            if (this.mMainTextPaint != paintSet.primaryTextPaint) {
                this.mMainTextPaint = paintSet.primaryTextPaint;
                this.mMainTextRenderer.setPaint(this.mMainTextPaint);
                this.mMainTextRenderer.setInAmbientMode(paintSet.isAmbientStyle);
            }

            if( !mIsWide ) {
                mMainTextAdjustedBounds.set(mTextPadding, this.mMainTextBounds.top, this.mMainTextBounds.right + this.mMainTextBounds.left - mTextPadding, this.mMainTextBounds.bottom);
            } else {
                mMainTextAdjustedBounds.set(mMainTextBounds);
            }

            this.mMainTextRenderer.draw(canvas, mMainTextAdjustedBounds);
        }
    }

    private void drawSubText(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mSubTextBounds.isEmpty()) {
            if (this.mSubTextPaint != paintSet.secondaryTextPaint) {
                this.mSubTextPaint = paintSet.secondaryTextPaint;
                this.mSubTextRenderer.setPaint(this.mSubTextPaint);
                this.mSubTextRenderer.setInAmbientMode(paintSet.isAmbientStyle);
            }

            if( !mIsWide ) {
                mSubTextAdjustedBounds.set(mTextPadding, this.mSubTextBounds.top, this.mSubTextBounds.right + this.mSubTextBounds.left - mTextPadding, this.mSubTextBounds.bottom);
            } else {
                mSubTextAdjustedBounds.set(mSubTextBounds);
            }

            this.mSubTextRenderer.draw(canvas, mSubTextAdjustedBounds);
        }
    }

    private void drawRangedValue(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mRangedValueBoundsF.isEmpty()) {
            float interval = this.mComplicationData.getMaxValue() - this.mComplicationData.getMinValue();
            float progress = interval > 0.0F ? this.mComplicationData.getValue() / interval : 0.0F;
            float total = 352.0F;
            float inProgressAngle = total * progress;
            float remainderAngle = total - inProgressAngle;
            int insetAmount = (int)Math.ceil((double)paintSet.inProgressPaint.getStrokeWidth());
            this.mRangedValueBoundsF.inset((float)insetAmount, (float)insetAmount);
            canvas.drawArc(this.mRangedValueBoundsF, -88.0F, inProgressAngle, false, paintSet.inProgressPaint);
            canvas.drawArc(this.mRangedValueBoundsF, -88.0F + inProgressAngle + 4.0F, remainderAngle, false, paintSet.remainingPaint);
            this.mRangedValueBoundsF.inset((float)(-insetAmount), (float)(-insetAmount));
        }
    }

    private void drawIcon(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mIconBounds.isEmpty()) {
            Drawable icon = this.mIcon;
            if (icon != null) {
                if (paintSet.isInBurnInProtectionMode() && this.mBurnInProtectionIcon != null) {
                    icon = this.mBurnInProtectionIcon;
                }

                icon.setColorFilter(paintSet.iconColorFilter);

                drawIconOnCanvas(canvas, this.mIconBounds, icon);
            }

        }
    }

    private void drawSmallImage(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mSmallImageBounds.isEmpty()) {
            if (!paintSet.isInBurnInProtectionMode()) {
                this.mRoundedSmallImage.setDrawable(this.mSmallImage);
                if (this.mSmallImage == null) {
                    return;
                }
            } else {
                this.mRoundedSmallImage.setDrawable(this.mBurnInProtectionSmallImage);
                if (this.mBurnInProtectionSmallImage == null) {
                    return;
                }
            }

            if (this.mComplicationData.getImageStyle() == 2) {
                this.mRoundedSmallImage.setColorFilter((ColorFilter)null);
                this.mRoundedSmallImage.setRadius(0);
            } else {
                this.mRoundedSmallImage.setColorFilter(paintSet.style.getColorFilter());
                this.mRoundedSmallImage.setRadius(this.getImageBorderRadius(paintSet.style, this.mSmallImageBounds));
            }

            this.mRoundedSmallImage.setBounds(this.mSmallImageBounds);
            this.mRoundedSmallImage.draw(canvas);
        }
    }

    private void drawLargeImage(Canvas canvas, ComplicationRenderer.PaintSet paintSet) {
        if (!this.mLargeImageBounds.isEmpty()) {
            if (!paintSet.isInBurnInProtectionMode()) {
                this.mRoundedLargeImage.setDrawable(this.mLargeImage);
                this.mRoundedLargeImage.setRadius(this.getImageBorderRadius(paintSet.style, this.mLargeImageBounds));
                this.mRoundedLargeImage.setBounds(this.mLargeImageBounds);
                this.mRoundedLargeImage.setColorFilter(paintSet.style.getColorFilter());
                this.mRoundedLargeImage.draw(canvas);
            }

        }
    }

    private static void drawIconOnCanvas(Canvas canvas, Rect bounds, Drawable icon) {
        icon.setBounds(0, 0, bounds.width(), bounds.height());
        canvas.save();
        canvas.translate((float)bounds.left, (float)bounds.top);
        icon.draw(canvas);
        canvas.restore();
    }

    private int getBorderRadius(ComplicationStyle currentStyle) {
        return this.mBounds.isEmpty() ? 0 : Math.min(Math.min(this.mBounds.height(), this.mBounds.width()) / 2, currentStyle.getBorderRadius());
    }

    @VisibleForTesting
    int getImageBorderRadius(ComplicationStyle currentStyle, Rect imageBounds) {
        return this.mBounds.isEmpty() ? 0 : Math.max(this.getBorderRadius(currentStyle) - Math.min(Math.min(imageBounds.left, this.mBounds.width() - imageBounds.right), Math.min(imageBounds.top, this.mBounds.height() - imageBounds.bottom)), 0);
    }

    private void calculateBounds() {
        if (this.mComplicationData != null && !this.mBounds.isEmpty()) {
            this.mBackgroundBounds.set(0, 0, this.mBounds.width(), this.mBounds.height());
            this.mBackgroundBoundsF.set(0.0F, 0.0F, (float)this.mBounds.width(), (float)this.mBounds.height());
            LayoutHelper currentLayoutHelper;
            switch(this.mComplicationData.getType()) {
                case 3:
                case 9:
                    currentLayoutHelper = new ShortTextLayoutHelper();
                    break;
                case 4:
                    currentLayoutHelper = new LongTextLayoutHelper();
                    break;
                case 5:
                    if (this.mRangedValueProgressHidden) {
                        if (this.mComplicationData.getShortText() == null) {
                            currentLayoutHelper = new IconLayoutHelper();
                        } else {
                            currentLayoutHelper = new ShortTextLayoutHelper();
                        }
                    } else {
                        currentLayoutHelper = new RangedValueLayoutHelper();
                    }
                    break;
                case 6:
                    currentLayoutHelper = new IconLayoutHelper();
                    break;
                case 7:
                    currentLayoutHelper = new SmallImageLayoutHelper();
                    break;
                case 8:
                    currentLayoutHelper = new LargeImageLayoutHelper();
                    break;
                default:
                    currentLayoutHelper = new LayoutHelper();
            }

            currentLayoutHelper.update(this.mBounds.width(), this.mBounds.height(), this.mComplicationData);
            currentLayoutHelper.getRangedValueBounds(this.mRangedValueBounds);
            this.mRangedValueBoundsF.set(this.mRangedValueBounds);
            currentLayoutHelper.getIconBounds(this.mIconBounds);
            currentLayoutHelper.getSmallImageBounds(this.mSmallImageBounds);
            currentLayoutHelper.getLargeImageBounds(this.mLargeImageBounds);
            Layout.Alignment alignment;
            if (this.mComplicationData.getType() == 4) {
                alignment = currentLayoutHelper.getLongTextAlignment();
                currentLayoutHelper.getLongTextBounds(this.mMainTextBounds);
                this.mMainTextRenderer.setAlignment(ALIGN_CENTER);
                this.mMainTextRenderer.setGravity(currentLayoutHelper.getLongTextGravity());
                currentLayoutHelper.getLongTitleBounds(this.mSubTextBounds);
                this.mSubTextRenderer.setAlignment(ALIGN_CENTER);
                this.mSubTextRenderer.setGravity(currentLayoutHelper.getLongTitleGravity());
            } else {
                alignment = currentLayoutHelper.getShortTextAlignment();
                currentLayoutHelper.getShortTextBounds(this.mMainTextBounds);
                this.mMainTextRenderer.setAlignment(mIsWide ? ALIGN_CENTER : alignment);
                this.mMainTextRenderer.setGravity(currentLayoutHelper.getShortTextGravity());
                currentLayoutHelper.getShortTitleBounds(this.mSubTextBounds);
                this.mSubTextRenderer.setAlignment(mIsWide ? ALIGN_CENTER : currentLayoutHelper.getShortTitleAlignment());
                this.mSubTextRenderer.setGravity(16);
            }

            if( mIsWide && mBackgroundBounds.height() < mMinHeightFor2LinesTextOnWideComplication ) {
                this.mSubTextBounds.setEmpty();

                CustomTextLayoutHelper helper = new CustomTextLayoutHelper();
                helper.update(this.mBounds.width(), this.mBounds.height(), this.mComplicationData);
                helper.getTextBounds(this.mMainTextBounds);
                mMainTextRenderer.setGravity(Gravity.CENTER);
            }

            if (alignment != ALIGN_CENTER) {
                float paddingAmount = 0.1F * (float)this.mBounds.height();
                this.mMainTextRenderer.setRelativePadding(paddingAmount / (float)this.mMainTextBounds.width(), 0.0F, 0.0F, 0.0F);
                this.mSubTextRenderer.setRelativePadding(paddingAmount / (float)this.mMainTextBounds.width(), 0.0F, 0.0F, 0.0F);
            } else {
                this.mMainTextRenderer.setRelativePadding(0.0F, 0.0F, 0.0F, 0.0F);
                this.mSubTextRenderer.setRelativePadding(0.0F, 0.0F, 0.0F, 0.0F);
            }

            Rect innerBounds = new Rect();
            LayoutUtils.getInnerBounds(innerBounds, this.mBackgroundBounds, (float)Math.max(this.getBorderRadius(this.mActiveStyle), this.getBorderRadius(this.mAmbientStyle)));
            if (!this.mMainTextBounds.intersect(innerBounds)) {
                this.mMainTextBounds.setEmpty();
            }

            if (!this.mSubTextBounds.intersect(innerBounds)) {
                this.mSubTextBounds.setEmpty();
            }

            if (!this.mIconBounds.isEmpty()) {
                LayoutUtils.scaledAroundCenter(this.mIconBounds, this.mIconBounds, 0.80F);
            }

            if (!this.mSmallImageBounds.isEmpty()) {
                LayoutUtils.scaledAroundCenter(this.mSmallImageBounds, this.mSmallImageBounds, 0.95F);
                if (this.mComplicationData.getImageStyle() == 2) {
                    LayoutUtils.fitSquareToBounds(this.mSmallImageBounds, innerBounds);
                }
            }

            if (!this.mLargeImageBounds.isEmpty()) {
                LayoutUtils.scaledAroundCenter(this.mLargeImageBounds, this.mLargeImageBounds, 1.0F);
            }

        }
    }

    private boolean loadDrawableIconAndImages() {
        Handler handler = new Handler(Looper.getMainLooper());
        Icon icon = null;
        Icon smallImage = null;
        Icon burnInProtectionSmallImage = null;
        Icon largeImage = null;
        Icon burnInProtectionIcon = null;
        this.mIcon = null;
        this.mSmallImage = null;
        this.mBurnInProtectionSmallImage = null;
        this.mLargeImage = null;
        this.mBurnInProtectionIcon = null;
        if (this.mComplicationData != null) {
            icon = this.mComplicationData.getIcon();
            burnInProtectionIcon = this.mComplicationData.getBurnInProtectionIcon();
            burnInProtectionSmallImage = this.mComplicationData.getBurnInProtectionSmallImage();
            smallImage = this.mComplicationData.getSmallImage();
            largeImage = this.mComplicationData.getLargeImage();
        }

        boolean hasImage = false;
        if (icon != null) {
            hasImage = true;
            icon.loadDrawableAsync(this.mContext, new Icon.OnDrawableLoadedListener() {
                public void onDrawableLoaded(Drawable d) {
                    if (d != null) {
                        CustomComplicationRenderer.this.mIcon = d;
                        CustomComplicationRenderer.this.mIcon.mutate();
                        CustomComplicationRenderer.this.invalidate();
                    }
                }
            }, handler);
        }

        if (burnInProtectionIcon != null) {
            hasImage = true;
            burnInProtectionIcon.loadDrawableAsync(this.mContext, new Icon.OnDrawableLoadedListener() {
                public void onDrawableLoaded(Drawable d) {
                    if (d != null) {
                        CustomComplicationRenderer.this.mBurnInProtectionIcon = d;
                        CustomComplicationRenderer.this.mBurnInProtectionIcon.mutate();
                        CustomComplicationRenderer.this.invalidate();
                    }
                }
            }, handler);
        }

        if (smallImage != null) {
            hasImage = true;
            smallImage.loadDrawableAsync(this.mContext, new Icon.OnDrawableLoadedListener() {
                public void onDrawableLoaded(Drawable d) {
                    if (d != null) {
                        CustomComplicationRenderer.this.mSmallImage = d;
                        CustomComplicationRenderer.this.invalidate();
                    }
                }
            }, handler);
        }

        if (burnInProtectionSmallImage != null) {
            hasImage = true;
            burnInProtectionSmallImage.loadDrawableAsync(this.mContext, new Icon.OnDrawableLoadedListener() {
                public void onDrawableLoaded(Drawable d) {
                    if (d != null) {
                        CustomComplicationRenderer.this.mBurnInProtectionSmallImage = d;
                        CustomComplicationRenderer.this.invalidate();
                    }
                }
            }, handler);
        }

        if (largeImage != null) {
            hasImage = true;
            largeImage.loadDrawableAsync(this.mContext, new Icon.OnDrawableLoadedListener() {
                public void onDrawableLoaded(Drawable d) {
                    if (d != null) {
                        CustomComplicationRenderer.this.mLargeImage = d;
                        CustomComplicationRenderer.this.invalidate();
                    }
                }
            }, handler);
        }

        return hasImage;
    }

    @VisibleForTesting
    Rect getBounds() {
        return this.mBounds;
    }

    @VisibleForTesting
    Rect getIconBounds() {
        return this.mIconBounds;
    }

    @VisibleForTesting
    Drawable getIcon() {
        return this.mIcon;
    }

    @VisibleForTesting
    Drawable getSmallImage() {
        return this.mSmallImage;
    }

    @VisibleForTesting
    Drawable getBurnInProtectionIcon() {
        return this.mBurnInProtectionIcon;
    }

    @VisibleForTesting
    Drawable getBurnInProtectionSmallImage() {
        return this.mBurnInProtectionSmallImage;
    }

    @VisibleForTesting
    RoundedDrawable getRoundedSmallImage() {
        return this.mRoundedSmallImage;
    }

    @VisibleForTesting
    Rect getMainTextBounds() {
        return this.mMainTextBounds;
    }

    @VisibleForTesting
    Rect getSubTextBounds() {
        return this.mSubTextBounds;
    }

    @VisibleForTesting
    void getComplicationInnerBounds(Rect outRect) {
        LayoutUtils.getInnerBounds(outRect, this.mBounds, (float)Math.max(this.getBorderRadius(this.mActiveStyle), this.getBorderRadius(this.mAmbientStyle)));
    }

    ComplicationData getComplicationData() {
        return this.mComplicationData;
    }
}
