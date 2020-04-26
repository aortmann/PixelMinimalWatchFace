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

import android.graphics.Rect;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.rendering.utils.LayoutHelper;
import android.support.wearable.complications.rendering.utils.LayoutUtils;

/**
 * A custom Layout helper that gives text the full width
 */
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
