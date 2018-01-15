package com.example.workouttracker;

import android.support.v7.widget.RecyclerView;

/**
 * Created by Gerald on 11/7/2017.
 */

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);

    void nextSet();

    void onItemDismiss(RecyclerView.ViewHolder viewHolder, int position);
}
