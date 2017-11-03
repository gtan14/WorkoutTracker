package com.example.workouttracker;

import android.graphics.Point;
import android.view.View;

/*Taken from Bandreid from StackOverflow*/

public class EventDragShadowBuilder extends View.DragShadowBuilder {

    private int touchPointXCoord, touchPointYCoord;

    public EventDragShadowBuilder() {
        super();
    }

    public EventDragShadowBuilder(View view, int touchPointXCoord,
                                  int touchPointYCoord) {

        super(view);
        this.touchPointXCoord = touchPointXCoord;
        this.touchPointYCoord = touchPointYCoord;
    }

    @Override
    public void onProvideShadowMetrics(Point shadowSize,
                                       Point shadowTouchPoint) {

        super.onProvideShadowMetrics(shadowSize, shadowTouchPoint);
        shadowTouchPoint.set(touchPointXCoord, touchPointYCoord);

    }
}