package com.benoitletondor.pixelminimalwatchface;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ColorSelectionRecyclerViewAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ComplicationColors> mColorOptionsDataSet;

    public ColorSelectionRecyclerViewAdapter(List<ComplicationColors> colorSettingsDataSet) {
        mColorOptionsDataSet = colorSettingsDataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ColorViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.color_config_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ComplicationColors colors = mColorOptionsDataSet.get(position);
        ColorViewHolder colorViewHolder = (ColorViewHolder) viewHolder;
        colorViewHolder.setColors(colors);
    }

    @Override
    public int getItemCount() {
        return mColorOptionsDataSet.size();
    }

    /**
     * Displays color options for an item on the watch face and saves value to the
     * SharedPreference associated with it.
     */
    public class ColorViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private View mLeftColorView;
        private View mRightColorView;

        public ColorViewHolder(final View view) {
            super(view);
            mLeftColorView = view.findViewById(R.id.colorLeft);
            mRightColorView = view.findViewById(R.id.colorRight);
            view.setOnClickListener(this);
        }

        public void setColors(@NonNull ComplicationColors colors) {
            mLeftColorView.setBackgroundColor(colors.getLeftColor());
            mRightColorView.setBackgroundColor(colors.getRightColor());
        }

        @Override
        public void onClick (View view) {
            int position = getAdapterPosition();
            ComplicationColors colors = mColorOptionsDataSet.get(position);

            Storage.INSTANCE.setComplicationColors(colors);

            Activity activity = (Activity) view.getContext();
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }
    }
}