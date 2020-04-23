package android.support.wearable.complications.rendering;

import android.graphics.Rect;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.utils.LayoutHelper;
import android.support.wearable.complications.rendering.utils.LayoutUtils;

public class CustomTextLayoutHelper extends LayoutHelper {

    private boolean shouldShowTextOnly(Rect bounds) {
        ComplicationData data = this.getComplicationData();
        return data.getIcon() == null && data.getSmallImage() == null;
    }

    public void getTextBounds(Rect outRect) {
        this.getBounds(outRect);
        if (!this.shouldShowTextOnly(outRect)) {
            LayoutUtils.getRightPart(outRect, outRect);
        }
    }
}
