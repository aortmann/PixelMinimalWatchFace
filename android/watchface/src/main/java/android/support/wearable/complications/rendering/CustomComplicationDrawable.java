package android.support.wearable.complications.rendering;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.watchface.WatchFaceService;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class CustomComplicationDrawable extends ComplicationDrawable {
    class NamelessClass_2 implements Runnable {
        NamelessClass_2() {
        }

        public void run() {
            CustomComplicationDrawable.this.setIsHighlighted(false);
            CustomComplicationDrawable.this.invalidateSelf();
        }
    }

    class NamelessClass_1 implements ComplicationRenderer.OnInvalidateListener {
        NamelessClass_1() {
        }

        public void onInvalidate() {
            CustomComplicationDrawable.this.invalidateSelf();
        }
    }

    public static final Creator<CustomComplicationDrawable> CREATOR = new Creator<CustomComplicationDrawable>() {
        public CustomComplicationDrawable createFromParcel(Parcel source) {
            return new CustomComplicationDrawable(source);
        }

        public CustomComplicationDrawable[] newArray(int size) {
            return new CustomComplicationDrawable[size];
        }
    };
    private boolean mIsWide;
    private Context mContext;
    private ComplicationRenderer mComplicationRenderer;
    private final ComplicationStyle.Builder mActiveStyleBuilder;
    private final ComplicationStyle.Builder mAmbientStyleBuilder;
    private final Handler mMainThreadHandler;
    private final Runnable mUnhighlightRunnable;
    private final ComplicationRenderer.OnInvalidateListener mRendererInvalidateListener;
    private CharSequence mNoDataText;
    private long mHighlightDuration;
    private long mCurrentTimeMillis;
    private boolean mInAmbientMode;
    private boolean mLowBitAmbient;
    private boolean mBurnInProtection;
    private boolean mIsHighlighted;
    private boolean mIsStyleUpToDate;
    private boolean mRangedValueProgressHidden;
    private boolean mIsInflatedFromXml;
    private boolean mAlreadyStyled;

    public CustomComplicationDrawable() {
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
        this.mUnhighlightRunnable = new NamelessClass_2();
        this.mRendererInvalidateListener = new NamelessClass_1();
        this.mActiveStyleBuilder = new ComplicationStyle.Builder();
        this.mAmbientStyleBuilder = new ComplicationStyle.Builder();
    }

    public CustomComplicationDrawable(Context context, boolean isWide) {
        this();
        this.mIsWide = isWide;
        this.setContext(context);
    }

    public CustomComplicationDrawable(CustomComplicationDrawable drawable) {
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());
        this.mUnhighlightRunnable = new NamelessClass_2();
        this.mRendererInvalidateListener = new NamelessClass_1();
        this.mActiveStyleBuilder = new ComplicationStyle.Builder(drawable.mActiveStyleBuilder);
        this.mAmbientStyleBuilder = new ComplicationStyle.Builder(drawable.mAmbientStyleBuilder);
        this.mNoDataText = drawable.mNoDataText.subSequence(0, drawable.mNoDataText.length());
        this.mHighlightDuration = drawable.mHighlightDuration;
        this.mRangedValueProgressHidden = drawable.mRangedValueProgressHidden;
        this.setBounds(drawable.getBounds());
        this.mAlreadyStyled = true;
    }

    private CustomComplicationDrawable(Parcel in) {
        this.mMainThreadHandler = new Handler(Looper.getMainLooper());

        class NamelessClass_2 implements Runnable {
            NamelessClass_2() {
            }

            public void run() {
                CustomComplicationDrawable.this.setIsHighlighted(false);
                CustomComplicationDrawable.this.invalidateSelf();
            }
        }

        this.mUnhighlightRunnable = new NamelessClass_2();

        class NamelessClass_1 implements ComplicationRenderer.OnInvalidateListener {
            NamelessClass_1() {
            }

            public void onInvalidate() {
                CustomComplicationDrawable.this.invalidateSelf();
            }
        }

        this.mRendererInvalidateListener = new NamelessClass_1();
        Bundle bundle = in.readBundle(this.getClass().getClassLoader());
        this.mActiveStyleBuilder = (ComplicationStyle.Builder)bundle.getParcelable("active_style_builder");
        this.mAmbientStyleBuilder = (ComplicationStyle.Builder)bundle.getParcelable("ambient_style_builder");
        this.mNoDataText = bundle.getCharSequence("no_data_text");
        this.mHighlightDuration = bundle.getLong("highlight_duration");
        this.mRangedValueProgressHidden = bundle.getBoolean("ranged_value_progress_hidden");
        this.setBounds((Rect)bundle.getParcelable("bounds"));
        this.mAlreadyStyled = true;
    }

    public void writeToParcel(Parcel dest, int flags) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("active_style_builder", this.mActiveStyleBuilder);
        bundle.putParcelable("ambient_style_builder", this.mAmbientStyleBuilder);
        bundle.putCharSequence("no_data_text", this.mNoDataText);
        bundle.putLong("highlight_duration", this.mHighlightDuration);
        bundle.putBoolean("ranged_value_progress_hidden", this.mRangedValueProgressHidden);
        bundle.putParcelable("bounds", this.getBounds());
        dest.writeBundle(bundle);
    }

    public int describeContents() {
        return 0;
    }

    @SuppressLint("WrongConstant")
    private static void setStyleToDefaultValues(ComplicationStyle.Builder styleBuilder, Resources r) {
        styleBuilder.setBackgroundColor(r.getColor(android.support.wearable.R.color.complicationDrawable_backgroundColor, (Resources.Theme)null));
        styleBuilder.setTextColor(r.getColor(android.support.wearable.R.color.complicationDrawable_textColor, (Resources.Theme)null));
        styleBuilder.setTitleColor(r.getColor(android.support.wearable.R.color.complicationDrawable_titleColor, (Resources.Theme)null));
        styleBuilder.setTextTypeface(Typeface.create(r.getString(android.support.wearable.R.string.complicationDrawable_textTypeface), 0));
        styleBuilder.setTitleTypeface(Typeface.create(r.getString(android.support.wearable.R.string.complicationDrawable_titleTypeface), 0));
        styleBuilder.setTextSize(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_textSize));
        styleBuilder.setTitleSize(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_titleSize));
        styleBuilder.setIconColor(r.getColor(android.support.wearable.R.color.complicationDrawable_iconColor, (Resources.Theme)null));
        styleBuilder.setBorderColor(r.getColor(android.support.wearable.R.color.complicationDrawable_borderColor, (Resources.Theme)null));
        styleBuilder.setBorderWidth(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderWidth));
        styleBuilder.setBorderRadius(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderRadius));
        styleBuilder.setBorderStyle(r.getInteger(android.support.wearable.R.integer.complicationDrawable_borderStyle));
        styleBuilder.setBorderDashWidth(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderDashWidth));
        styleBuilder.setBorderDashGap(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderDashGap));
        styleBuilder.setRangedValueRingWidth(r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_rangedValueRingWidth));
        styleBuilder.setRangedValuePrimaryColor(r.getColor(android.support.wearable.R.color.complicationDrawable_rangedValuePrimaryColor, (Resources.Theme)null));
        styleBuilder.setRangedValueSecondaryColor(r.getColor(android.support.wearable.R.color.complicationDrawable_rangedValueSecondaryColor, (Resources.Theme)null));
        styleBuilder.setHighlightColor(r.getColor(android.support.wearable.R.color.complicationDrawable_highlightColor, (Resources.Theme)null));
    }

    public void setContext(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Argument \"context\" should not be null.");
        } else if (!Objects.equals(context, this.mContext)) {
            this.mContext = context;
            if (!this.mIsInflatedFromXml && !this.mAlreadyStyled) {
                setStyleToDefaultValues(this.mActiveStyleBuilder, context.getResources());
                setStyleToDefaultValues(this.mAmbientStyleBuilder, context.getResources());
            }

            if (!this.mAlreadyStyled) {
                this.mHighlightDuration = (long)context.getResources().getInteger(android.support.wearable.R.integer.complicationDrawable_highlightDurationMs);
            }

            this.mComplicationRenderer = new CustomComplicationRenderer(this.mContext, this.mActiveStyleBuilder.build(), this.mAmbientStyleBuilder.build(), this.mIsWide);
            this.mComplicationRenderer.setOnInvalidateListener(this.mRendererInvalidateListener);
            if (this.mNoDataText == null) {
                this.setNoDataText(context.getString(android.support.wearable.R.string.complicationDrawable_noDataText));
            } else {
                this.mComplicationRenderer.setNoDataText(this.mNoDataText);
            }

            this.mComplicationRenderer.setRangedValueProgressHidden(this.mRangedValueProgressHidden);
            this.mComplicationRenderer.setBounds(this.getBounds());
            this.mIsStyleUpToDate = true;
        }
    }

    private void inflateAttributes(Resources r, XmlPullParser parser) {
        TypedArray a = r.obtainAttributes(Xml.asAttributeSet(parser), android.support.wearable.R.styleable.ComplicationDrawable);
        this.setRangedValueProgressHidden(a.getBoolean(android.support.wearable.R.styleable.ComplicationDrawable_rangedValueProgressHidden, false));
        a.recycle();
    }

    @SuppressLint("WrongConstant")
    private void inflateStyle(boolean isAmbient, Resources r, XmlPullParser parser) {
        TypedArray a = r.obtainAttributes(Xml.asAttributeSet(parser), android.support.wearable.R.styleable.ComplicationDrawable);
        ComplicationStyle.Builder currentBuilder = this.getComplicationStyleBuilder(isAmbient);
        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_backgroundColor)) {
            currentBuilder.setBackgroundColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_backgroundColor, r.getColor(android.support.wearable.R.color.complicationDrawable_backgroundColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_backgroundDrawable)) {
            currentBuilder.setBackgroundDrawable(a.getDrawable(android.support.wearable.R.styleable.ComplicationDrawable_backgroundDrawable));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_textColor)) {
            currentBuilder.setTextColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_textColor, r.getColor(android.support.wearable.R.color.complicationDrawable_textColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_titleColor)) {
            currentBuilder.setTitleColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_titleColor, r.getColor(android.support.wearable.R.color.complicationDrawable_titleColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_textTypeface)) {
            currentBuilder.setTextTypeface(Typeface.create(a.getString(android.support.wearable.R.styleable.ComplicationDrawable_textTypeface), 0));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_titleTypeface)) {
            currentBuilder.setTitleTypeface(Typeface.create(a.getString(android.support.wearable.R.styleable.ComplicationDrawable_titleTypeface), 0));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_textSize)) {
            currentBuilder.setTextSize(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_textSize, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_textSize)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_titleSize)) {
            currentBuilder.setTitleSize(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_titleSize, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_titleSize)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_iconColor)) {
            currentBuilder.setIconColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_iconColor, r.getColor(android.support.wearable.R.color.complicationDrawable_iconColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderColor)) {
            currentBuilder.setBorderColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_borderColor, r.getColor(android.support.wearable.R.color.complicationDrawable_borderColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderRadius)) {
            currentBuilder.setBorderRadius(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_borderRadius, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderRadius)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderStyle)) {
            currentBuilder.setBorderStyle(a.getInt(android.support.wearable.R.styleable.ComplicationDrawable_borderStyle, r.getInteger(android.support.wearable.R.integer.complicationDrawable_borderStyle)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderDashWidth)) {
            currentBuilder.setBorderDashWidth(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_borderDashWidth, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderDashWidth)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderDashGap)) {
            currentBuilder.setBorderDashGap(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_borderDashGap, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderDashGap)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_borderWidth)) {
            currentBuilder.setBorderWidth(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_borderWidth, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_borderWidth)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_rangedValueRingWidth)) {
            currentBuilder.setRangedValueRingWidth(a.getDimensionPixelSize(android.support.wearable.R.styleable.ComplicationDrawable_rangedValueRingWidth, r.getDimensionPixelSize(android.support.wearable.R.dimen.complicationDrawable_rangedValueRingWidth)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_rangedValuePrimaryColor)) {
            currentBuilder.setRangedValuePrimaryColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_rangedValuePrimaryColor, r.getColor(android.support.wearable.R.color.complicationDrawable_rangedValuePrimaryColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_rangedValueSecondaryColor)) {
            currentBuilder.setRangedValueSecondaryColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_rangedValueSecondaryColor, r.getColor(android.support.wearable.R.color.complicationDrawable_rangedValueSecondaryColor, (Resources.Theme)null)));
        }

        if (a.hasValue(android.support.wearable.R.styleable.ComplicationDrawable_highlightColor)) {
            currentBuilder.setHighlightColor(a.getColor(android.support.wearable.R.styleable.ComplicationDrawable_highlightColor, r.getColor(android.support.wearable.R.color.complicationDrawable_highlightColor, (Resources.Theme)null)));
        }

        a.recycle();
    }

    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Resources.Theme theme) throws XmlPullParserException, IOException {
        this.mIsInflatedFromXml = true;
        int outerDepth = parser.getDepth();
        this.inflateAttributes(r, parser);
        setStyleToDefaultValues(this.mActiveStyleBuilder, r);
        setStyleToDefaultValues(this.mAmbientStyleBuilder, r);
        this.inflateStyle(false, r, parser);
        this.inflateStyle(true, r, parser);

        int type;
        while((type = parser.next()) != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
            if (type == 2) {
                String name = parser.getName();
                if (TextUtils.equals(name, "ambient")) {
                    this.inflateStyle(true, r, parser);
                } else {
                    String var8 = String.valueOf(this);
                    Log.w("ComplicationDrawable", (new StringBuilder(43 + String.valueOf(name).length() + String.valueOf(var8).length())).append("Unknown element: ").append(name).append(" for ComplicationDrawable ").append(var8).toString());
                }
            }
        }

        this.mIsStyleUpToDate = false;
    }

    public void draw(Canvas canvas, long currentTimeMillis) {
        this.assertInitialized();
        this.setCurrentTimeMillis(currentTimeMillis);
        this.draw(canvas);
    }

    public void draw(Canvas canvas) {
        this.assertInitialized();
        this.updateStyleIfRequired();
        this.mComplicationRenderer.draw(canvas, this.mCurrentTimeMillis, this.mInAmbientMode, this.mLowBitAmbient, this.mBurnInProtection, this.mIsHighlighted);
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    @SuppressLint("WrongConstant")
    public int getOpacity() {
        return -3;
    }

    protected void onBoundsChange(Rect bounds) {
        if (this.mComplicationRenderer != null) {
            this.mComplicationRenderer.setBounds(bounds);
        }

    }

    public void setNoDataText(@Nullable CharSequence noDataText) {
        if (noDataText == null) {
            this.mNoDataText = "";
        } else {
            this.mNoDataText = noDataText.subSequence(0, noDataText.length());
        }

        if (this.mComplicationRenderer != null) {
            this.mComplicationRenderer.setNoDataText(this.mNoDataText);
        }

    }

    public void setRangedValueProgressHidden(boolean rangedValueProgressHidden) {
        this.mRangedValueProgressHidden = rangedValueProgressHidden;
        if (this.mComplicationRenderer != null) {
            this.mComplicationRenderer.setRangedValueProgressHidden(rangedValueProgressHidden);
        }

    }

    public boolean isRangedValueProgressHidden() {
        return this.mRangedValueProgressHidden;
    }

    public void setComplicationData(@Nullable ComplicationData complicationData) {
        this.assertInitialized();
        this.mComplicationRenderer.setComplicationData(complicationData);
    }

    public void setInAmbientMode(boolean inAmbientMode) {
        this.mInAmbientMode = inAmbientMode;
    }

    public void setLowBitAmbient(boolean lowBitAmbient) {
        this.mLowBitAmbient = lowBitAmbient;
    }

    public void setBurnInProtection(boolean burnInProtection) {
        this.mBurnInProtection = burnInProtection;
    }

    public void setCurrentTimeMillis(long currentTimeMillis) {
        this.mCurrentTimeMillis = currentTimeMillis;
    }

    public void setIsHighlighted(boolean isHighlighted) {
        this.mIsHighlighted = isHighlighted;
    }

    public void setBackgroundColorActive(int backgroundColor) {
        this.getComplicationStyleBuilder(false).setBackgroundColor(backgroundColor);
        this.mIsStyleUpToDate = false;
    }

    public void setBackgroundDrawableActive(Drawable drawable) {
        this.getComplicationStyleBuilder(false).setBackgroundDrawable(drawable);
        this.mIsStyleUpToDate = false;
    }

    public void setTextColorActive(int textColor) {
        this.getComplicationStyleBuilder(false).setTextColor(textColor);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleColorActive(int titleColor) {
        this.getComplicationStyleBuilder(false).setTitleColor(titleColor);
        this.mIsStyleUpToDate = false;
    }

    public void setImageColorFilterActive(ColorFilter colorFilter) {
        this.getComplicationStyleBuilder(false).setColorFilter(colorFilter);
        this.mIsStyleUpToDate = false;
    }

    public void setIconColorActive(int iconColor) {
        this.getComplicationStyleBuilder(false).setIconColor(iconColor);
        this.mIsStyleUpToDate = false;
    }

    public void setTextTypefaceActive(Typeface textTypeface) {
        this.getComplicationStyleBuilder(false).setTextTypeface(textTypeface);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleTypefaceActive(Typeface titleTypeface) {
        this.getComplicationStyleBuilder(false).setTitleTypeface(titleTypeface);
        this.mIsStyleUpToDate = false;
    }

    public void setTextSizeActive(int textSize) {
        this.getComplicationStyleBuilder(false).setTextSize(textSize);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleSizeActive(int titleSize) {
        this.getComplicationStyleBuilder(false).setTitleSize(titleSize);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderColorActive(int borderColor) {
        this.getComplicationStyleBuilder(false).setBorderColor(borderColor);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderStyleActive(int borderStyle) {
        this.getComplicationStyleBuilder(false).setBorderStyle(borderStyle);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderDashWidthActive(int borderDashWidth) {
        this.getComplicationStyleBuilder(false).setBorderDashWidth(borderDashWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderDashGapActive(int borderDashGap) {
        this.getComplicationStyleBuilder(false).setBorderDashGap(borderDashGap);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderRadiusActive(int borderRadius) {
        this.getComplicationStyleBuilder(false).setBorderRadius(borderRadius);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderWidthActive(int borderWidth) {
        this.getComplicationStyleBuilder(false).setBorderWidth(borderWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValueRingWidthActive(int rangedValueRingWidth) {
        this.getComplicationStyleBuilder(false).setRangedValueRingWidth(rangedValueRingWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValuePrimaryColorActive(int rangedValuePrimaryColor) {
        this.getComplicationStyleBuilder(false).setRangedValuePrimaryColor(rangedValuePrimaryColor);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValueSecondaryColorActive(int rangedValueSecondaryColor) {
        this.getComplicationStyleBuilder(false).setRangedValueSecondaryColor(rangedValueSecondaryColor);
        this.mIsStyleUpToDate = false;
    }

    public void setHighlightColorActive(int highlightColor) {
        this.getComplicationStyleBuilder(false).setHighlightColor(highlightColor);
        this.mIsStyleUpToDate = false;
    }

    public void setBackgroundColorAmbient(int backgroundColor) {
        this.getComplicationStyleBuilder(true).setBackgroundColor(backgroundColor);
        this.mIsStyleUpToDate = false;
    }

    public void setBackgroundDrawableAmbient(Drawable drawable) {
        this.getComplicationStyleBuilder(true).setBackgroundDrawable(drawable);
        this.mIsStyleUpToDate = false;
    }

    public void setTextColorAmbient(int textColor) {
        this.getComplicationStyleBuilder(true).setTextColor(textColor);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleColorAmbient(int titleColor) {
        this.getComplicationStyleBuilder(true).setTitleColor(titleColor);
        this.mIsStyleUpToDate = false;
    }

    public void setImageColorFilterAmbient(ColorFilter colorFilter) {
        this.getComplicationStyleBuilder(true).setColorFilter(colorFilter);
        this.mIsStyleUpToDate = false;
    }

    public void setIconColorAmbient(int iconColor) {
        this.getComplicationStyleBuilder(true).setIconColor(iconColor);
        this.mIsStyleUpToDate = false;
    }

    public void setTextTypefaceAmbient(Typeface textTypeface) {
        this.getComplicationStyleBuilder(true).setTextTypeface(textTypeface);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleTypefaceAmbient(Typeface titleTypeface) {
        this.getComplicationStyleBuilder(true).setTitleTypeface(titleTypeface);
        this.mIsStyleUpToDate = false;
    }

    public void setTextSizeAmbient(int textSize) {
        this.getComplicationStyleBuilder(true).setTextSize(textSize);
        this.mIsStyleUpToDate = false;
    }

    public void setTitleSizeAmbient(int titleSize) {
        this.getComplicationStyleBuilder(true).setTitleSize(titleSize);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderColorAmbient(int borderColor) {
        this.getComplicationStyleBuilder(true).setBorderColor(borderColor);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderStyleAmbient(int borderStyle) {
        this.getComplicationStyleBuilder(true).setBorderStyle(borderStyle);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderDashWidthAmbient(int borderDashWidth) {
        this.getComplicationStyleBuilder(true).setBorderDashWidth(borderDashWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderDashGapAmbient(int borderDashGap) {
        this.getComplicationStyleBuilder(true).setBorderDashGap(borderDashGap);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderRadiusAmbient(int borderRadius) {
        this.getComplicationStyleBuilder(true).setBorderRadius(borderRadius);
        this.mIsStyleUpToDate = false;
    }

    public void setBorderWidthAmbient(int borderWidth) {
        this.getComplicationStyleBuilder(true).setBorderWidth(borderWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValueRingWidthAmbient(int rangedValueRingWidth) {
        this.getComplicationStyleBuilder(true).setRangedValueRingWidth(rangedValueRingWidth);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValuePrimaryColorAmbient(int rangedValuePrimaryColor) {
        this.getComplicationStyleBuilder(true).setRangedValuePrimaryColor(rangedValuePrimaryColor);
        this.mIsStyleUpToDate = false;
    }

    public void setRangedValueSecondaryColorAmbient(int rangedValueSecondaryColor) {
        this.getComplicationStyleBuilder(true).setRangedValueSecondaryColor(rangedValueSecondaryColor);
        this.mIsStyleUpToDate = false;
    }

    public void setHighlightColorAmbient(int highlightColor) {
        this.getComplicationStyleBuilder(true).setHighlightColor(highlightColor);
        this.mIsStyleUpToDate = false;
    }

    /** @deprecated */
    @Deprecated
    public boolean onTap(int x, int y, long tapTimeMillis) {
        return this.onTap(x, y);
    }

    @SuppressLint("WrongConstant")
    public boolean onTap(int x, int y) {
        if (this.mComplicationRenderer == null) {
            return false;
        } else {
            ComplicationData data = this.mComplicationRenderer.getComplicationData();
            if (data != null && (data.getTapAction() != null || data.getType() == 9) && this.getBounds().contains(x, y)) {
                if (data.getType() == 9) {
                    if (!(this.mContext instanceof WatchFaceService)) {
                        return false;
                    }

                    this.mContext.startActivity(ComplicationHelperActivity.createPermissionRequestHelperIntent(this.mContext, new ComponentName(this.mContext, this.mContext.getClass())).addFlags(268435456));
                } else {
                    try {
                        data.getTapAction().send();
                    } catch (PendingIntent.CanceledException var5) {
                        return false;
                    }
                }

                if (this.getHighlightDuration() > 0L) {
                    this.setIsHighlighted(true);
                    this.invalidateSelf();
                    this.mMainThreadHandler.removeCallbacks(this.mUnhighlightRunnable);
                    this.mMainThreadHandler.postDelayed(this.mUnhighlightRunnable, this.getHighlightDuration());
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean isHighlighted() {
        return this.mIsHighlighted;
    }

    public void setHighlightDuration(long highlightDurationMillis) {
        if (highlightDurationMillis < 0L) {
            throw new IllegalArgumentException("Highlight duration should be non-negative.");
        } else {
            this.mHighlightDuration = highlightDurationMillis;
        }
    }

    public long getHighlightDuration() {
        return this.mHighlightDuration;
    }

    private ComplicationStyle.Builder getComplicationStyleBuilder(boolean isAmbient) {
        return isAmbient ? this.mAmbientStyleBuilder : this.mActiveStyleBuilder;
    }

    private void updateStyleIfRequired() {
        if (!this.mIsStyleUpToDate) {
            this.mComplicationRenderer.updateStyle(this.mActiveStyleBuilder.build(), this.mAmbientStyleBuilder.build());
            this.mIsStyleUpToDate = true;
        }

    }

    private void assertInitialized() {
        if (this.mContext == null) {
            throw new IllegalStateException("ComplicationDrawable does not have a context. Use setContext(Context) to set it first.");
        }
    }

    @VisibleForTesting
    ComplicationStyle getActiveStyle() {
        return this.mActiveStyleBuilder.build();
    }

    @VisibleForTesting
    ComplicationStyle getAmbientStyle() {
        return this.mAmbientStyleBuilder.build();
    }

    @VisibleForTesting
    ComplicationRenderer getComplicationRenderer() {
        return this.mComplicationRenderer;
    }

    @VisibleForTesting
    CharSequence getNoDataText() {
        return this.mNoDataText;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface BorderStyle {
    }
}
