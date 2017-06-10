package com.maxtower.testzoomlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewTreeObserver;

public class MainActivity extends AppCompatActivity
{

    ZoomLayout zoomLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        zoomLayout = (ZoomLayout) findViewById(R.id.zoom_layout);
        ViewTreeObserver vto = zoomLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener (new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                zoomLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width  = zoomLayout.getMeasuredWidth();
                int height = zoomLayout.getMeasuredHeight();
                zoomLayout.setContentSize(width, height);
            }
        });
    }


}
