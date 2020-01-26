package com.benoitletondor.pixelminimalwatchface;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ComplicationConfigData {


    /**
     * Interface all ConfigItems must implement so the {@link RecyclerView}'s Adapter associated
     * with the configuration activity knows what type of ViewHolder to inflate.
     */
    public interface ConfigItemType {
        int getConfigType();
    }

    /**
     * Returns Watch Face Service class associated with configuration Activity.
     */
    public static Class getWatchFaceServiceClass() {
        return PixelMinimalWatchFace.class;
    }

    public static ComplicationColors getDefaultComplicationColors(@NonNull Context context) {
        int leftColor = ContextCompat.getColor(context, R.color.complication_default_left_color);
        int rightColor = ContextCompat.getColor(context, R.color.complication_default_right_color);
        return new ComplicationColors(leftColor, rightColor, true);
    }

    /**
     * Returns Material Design color options.
     */
    public static List<ComplicationColors> getColorOptionsDataSet(@NonNull Context context) {
        ArrayList<ComplicationColors> colorOptionsDataSet = new ArrayList<>();

        colorOptionsDataSet.add(getDefaultComplicationColors(context));

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#FFFFFF"))); // White

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#FFEB3B"))); // Yellow
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#FFC107"))); // Amber
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#FF9800"))); // Orange
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#FF5722"))); // Deep Orange

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#F44336"))); // Red
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#E91E63"))); // Pink

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#9C27B0"))); // Purple
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#673AB7"))); // Deep Purple
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#3F51B5"))); // Indigo
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#2196F3"))); // Blue
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#03A9F4"))); // Light Blue

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#00BCD4"))); // Cyan
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#009688"))); // Teal
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#4CAF50"))); // Green
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#8BC34A"))); // Lime Green
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#CDDC39"))); // Lime

        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#607D8B"))); // Blue Grey
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#9E9E9E"))); // Grey
        colorOptionsDataSet.add(new ComplicationColors(Color.parseColor("#795548"))); // Brown

        return colorOptionsDataSet;
    }

    public static ArrayList<ConfigItemType> getDataToPopulateAdapter(Context context) {

        ArrayList<ConfigItemType> settingsConfigData = new ArrayList<>();

        // Data for watch face preview and complications UX in settings Activity.
        ConfigItemType complicationConfigItem =
                new PreviewAndComplicationsConfigItem(R.drawable.add_complication);
        settingsConfigData.add(complicationConfigItem);

        ConfigItemType markerColorConfigItem =
                new ColorConfigItem(
                        context.getString(R.string.config_complications_color_label),
                        R.drawable.ic_add_white_24dp,
                        ColorSelectionActivity.class);
        settingsConfigData.add(markerColorConfigItem);

        return settingsConfigData;
    }

    /**
     * Data for Watch Face Preview with Complications Preview item in RecyclerView.
     */
    public static class PreviewAndComplicationsConfigItem implements ConfigItemType {

        private int defaultComplicationResourceId;

        PreviewAndComplicationsConfigItem(int defaultComplicationResourceId) {
            this.defaultComplicationResourceId = defaultComplicationResourceId;
        }

        public int getDefaultComplicationResourceId() {
            return defaultComplicationResourceId;
        }

        @Override
        public int getConfigType() {
            return ComplicationConfigRecyclerViewAdapter.TYPE_PREVIEW_AND_COMPLICATIONS_CONFIG;
        }
    }

    /**
     * Data for color picker item in RecyclerView.
     */
    public static class ColorConfigItem implements ConfigItemType {

        private String name;
        private int iconResourceId;
        private Class<ColorSelectionActivity> activityToChoosePreference;

        ColorConfigItem(
                String name,
                int iconResourceId,
                Class<ColorSelectionActivity> activity) {
            this.name = name;
            this.iconResourceId = iconResourceId;
            this.activityToChoosePreference = activity;
        }

        public String getName() {
            return name;
        }

        public int getIconResourceId() {
            return iconResourceId;
        }


        public Class<ColorSelectionActivity> getActivityToChoosePreference() {
            return activityToChoosePreference;
        }

        @Override
        public int getConfigType() {
            return ComplicationConfigRecyclerViewAdapter.TYPE_COLOR_CONFIG;
        }
    }
}