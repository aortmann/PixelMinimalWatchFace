package com.benoitletondor.pixelminimalwatchface;

import android.app.Activity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.wear.widget.WearableLinearLayoutManager;
import androidx.wear.widget.WearableRecyclerView;

/**
 * Allows user to select color for something on the watch face (background, highlight,etc.) and
 * saves it to {@link android.content.SharedPreferences} in RecyclerView.Adapter.
 */
public class ColorSelectionActivity extends Activity {

    private WearableRecyclerView mConfigAppearanceWearableRecyclerView;

    private ColorSelectionRecyclerViewAdapter mColorSelectionRecyclerViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_selection_config);

        mColorSelectionRecyclerViewAdapter = new ColorSelectionRecyclerViewAdapter(
            ComplicationConfigData.getColorOptionsDataSet(this)
        );

        mConfigAppearanceWearableRecyclerView = findViewById(R.id.colors_recycler_view);

        // Aligns the first and last items on the list vertically centered on the screen.
        mConfigAppearanceWearableRecyclerView.setEdgeItemsCenteringEnabled(true);

        mConfigAppearanceWearableRecyclerView.setLayoutManager(new WearableLinearLayoutManager(this));

        // Improves performance because we know changes in content do not change the layout size of
        // the RecyclerView.
        mConfigAppearanceWearableRecyclerView.setHasFixedSize(true);

        mConfigAppearanceWearableRecyclerView.setAdapter(mColorSelectionRecyclerViewAdapter);
    }
}