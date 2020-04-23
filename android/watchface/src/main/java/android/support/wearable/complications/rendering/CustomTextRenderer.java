package android.support.wearable.complications.rendering;

import android.annotation.SuppressLint;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.LocaleSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.Objects;

public class CustomTextRenderer extends TextRenderer {
    private static final Class<?>[] SPAN_WHITELIST = new Class[]{ForegroundColorSpan.class, LocaleSpan.class, SubscriptSpan.class, SuperscriptSpan.class, StrikethroughSpan.class, StyleSpan.class, TypefaceSpan.class, UnderlineSpan.class};
    private final Rect mBounds = new Rect();
    private TextPaint mPaint;
    @Nullable
    private String mAmbientModeText;
    @Nullable
    private CharSequence mOriginalText;
    @Nullable
    private CharSequence mText;
    private float mRelativePaddingStart;
    private float mRelativePaddingEnd;
    private float mRelativePaddingTop;
    private float mRelativePaddingBottom;
    private StaticLayout mStaticLayout;
    private int mGravity = 17;
    private int mMaxLines = 1;
    private int mMinCharactersShown = 7;
    private TextUtils.TruncateAt mEllipsize;
    private Layout.Alignment mAlignment;
    private final Rect mWorkingRect;
    private final Rect mOutputRect;
    private boolean mInAmbientMode;
    private boolean mNeedUpdateLayout;
    private boolean mNeedCalculateBounds;

    public CustomTextRenderer() {
        this.mEllipsize = TextUtils.TruncateAt.END;
        this.mAlignment = Layout.Alignment.ALIGN_CENTER;
        this.mWorkingRect = new Rect();
        this.mOutputRect = new Rect();
        this.mInAmbientMode = false;
    }

    public void draw(Canvas canvas, Rect bounds) {
        if (!TextUtils.isEmpty(this.mText)) {
            if (this.mNeedUpdateLayout || this.mBounds.width() != bounds.width() || this.mBounds.height() != bounds.height()) {
                this.updateLayout(bounds.width(), bounds.height());
                this.mNeedUpdateLayout = false;
                this.mNeedCalculateBounds = true;
            }

            if (this.mNeedCalculateBounds || !this.mBounds.equals(bounds)) {
                this.mBounds.set(bounds);
                this.calculateBounds();
                this.mNeedCalculateBounds = false;
            }

            canvas.save();
            canvas.translate((float)this.mOutputRect.left, (float)this.mOutputRect.top);
            this.mStaticLayout.draw(canvas);
            canvas.restore();
        }
    }

    public void requestUpdateLayout() {
        this.mNeedUpdateLayout = true;
    }

    public void setText(@Nullable CharSequence text) {
        if (!Objects.equals(this.mOriginalText, text)) {
            this.mOriginalText = text;
            this.mText = this.applySpanWhitelist(this.mOriginalText);
            this.mNeedUpdateLayout = true;
        }
    }

    @VisibleForTesting
    CharSequence applySpanWhitelist(CharSequence text) {
        if (text instanceof Spanned) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            Object[] spans = builder.getSpans(0, text.length(), Object.class);
            Object[] var4 = spans;
            int var5 = spans.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Object span = var4[var6];
                if (!this.isSpanAllowed(span)) {
                    builder.removeSpan(span);
                }
            }

            return builder;
        } else {
            return text;
        }
    }

    private boolean isSpanAllowed(Object span) {
        Class[] var2 = SPAN_WHITELIST;
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Class<?> spanClass = var2[var4];
            if (spanClass.isInstance(span)) {
                return true;
            }
        }

        return false;
    }

    public void setPaint(TextPaint paint) {
        this.mPaint = paint;
        this.mNeedUpdateLayout = true;
    }

    public void setRelativePadding(float start, float top, float end, float bottom) {
        if (this.mRelativePaddingStart != start || this.mRelativePaddingTop != top || this.mRelativePaddingEnd != end || this.mRelativePaddingBottom != bottom) {
            this.mRelativePaddingStart = start;
            this.mRelativePaddingTop = top;
            this.mRelativePaddingEnd = end;
            this.mRelativePaddingBottom = bottom;
            this.mNeedUpdateLayout = true;
        }
    }

    public void setGravity(int gravity) {
        if (this.mGravity != gravity) {
            this.mGravity = gravity;
            this.mNeedCalculateBounds = true;
        }
    }

    public void setMaxLines(int maxLines) {
        if (this.mMaxLines != maxLines && maxLines > 0) {
            this.mMaxLines = maxLines;
            this.mNeedUpdateLayout = true;
        }
    }

    public void setMinimumCharactersShown(int minCharactersShown) {
        if (this.mMinCharactersShown != minCharactersShown) {
            this.mMinCharactersShown = minCharactersShown;
            this.mNeedUpdateLayout = true;
        }
    }

    public void setEllipsize(@Nullable TextUtils.TruncateAt ellipsize) {
        if (this.mEllipsize != ellipsize) {
            this.mEllipsize = ellipsize;
            this.mNeedUpdateLayout = true;
        }
    }

    public void setAlignment(Layout.Alignment alignment) {
        if (this.mAlignment != alignment) {
            this.mAlignment = alignment;
            this.mNeedUpdateLayout = true;
        }
    }

    public boolean hasText() {
        return !TextUtils.isEmpty(this.mText);
    }

    public boolean isLtr() {
        return this.mStaticLayout.getParagraphDirection(0) == 1;
    }

    public void setInAmbientMode(boolean inAmbientMode) {
        if (this.mInAmbientMode != inAmbientMode) {
            this.mInAmbientMode = inAmbientMode;
            if (!TextUtils.equals(this.mAmbientModeText, this.mText)) {
                this.mNeedUpdateLayout = true;
            }

        }
    }

    @SuppressLint("WrongConstant")
    private void updateLayout(int width, int height) {
        if (this.mPaint == null) {
            this.setPaint(new TextPaint());
        }
        int availableWidth = (int)((float)width * (1.0F - this.mRelativePaddingStart - this.mRelativePaddingEnd));
        TextPaint paint = new TextPaint(this.mPaint);
        paint.setTextSize(mPaint.getTextSize());
        float textWidth = paint.measureText(this.mText, 0, this.mText.length());
        if (textWidth > (float)availableWidth) {
            int charactersShown = this.mMinCharactersShown;
            if (this.mEllipsize != null && this.mEllipsize != TextUtils.TruncateAt.MARQUEE) {
                ++charactersShown;
            }

            charactersShown = Math.min(charactersShown, this.mText.length());
            CharSequence textToFit = this.mText.subSequence(0, charactersShown);

            for(textWidth = paint.measureText(textToFit, 0, textToFit.length()); textWidth > (float)availableWidth; textWidth = paint.measureText(textToFit, 0, textToFit.length())) {
                paint.setTextSize(paint.getTextSize() - 1.0F);
            }
        }

        CharSequence text = this.mText;
        if (this.mInAmbientMode) {
            this.mAmbientModeText = EmojiHelper.replaceEmoji(this.mText, 32);
            text = this.mAmbientModeText;
        }

        StaticLayout.Builder builder = StaticLayout.Builder.obtain((CharSequence)text, 0, ((CharSequence)text).length(), paint, availableWidth);
        builder.setBreakStrategy(1);
        builder.setEllipsize(this.mEllipsize);
        builder.setHyphenationFrequency(Layout.HYPHENATION_FREQUENCY_FULL);
        builder.setMaxLines(this.mMaxLines);
        builder.setAlignment(this.mAlignment);
        this.mStaticLayout = builder.build();
    }

    private void calculateBounds() {
        int layoutDirection = this.isLtr() ? 0 : 1;
        int leftPadding = (int)((float)this.mBounds.width() * (this.isLtr() ? this.mRelativePaddingStart : this.mRelativePaddingEnd));
        int rightPadding = (int)((float)this.mBounds.width() * (this.isLtr() ? this.mRelativePaddingEnd : this.mRelativePaddingStart));
        int topPadding = (int)((float)this.mBounds.height() * this.mRelativePaddingTop);
        int bottomPadding = (int)((float)this.mBounds.height() * this.mRelativePaddingBottom);
        this.mWorkingRect.set(this.mBounds.left + leftPadding, this.mBounds.top + topPadding, this.mBounds.right - rightPadding, this.mBounds.bottom - bottomPadding);
        Gravity.apply(this.mGravity, this.mStaticLayout.getWidth(), this.mStaticLayout.getHeight(), this.mWorkingRect, this.mOutputRect, layoutDirection);
    }
}
