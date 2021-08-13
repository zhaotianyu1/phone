package com.example.tclphone.utils;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

import androidx.core.view.ViewCompat;

/**
 * 获得焦点时放大的ImageButton
 * Created by lxf on 2017/2/21.
 */
public class FocusImageButton extends androidx.appcompat.widget.AppCompatImageButton {
    public FocusImageButton(Context context) {
        super(context);
    }

    public FocusImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public FocusImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            scaleUp();
        } else {
            scaleDown();
        }
    }

    //1.08表示放大倍数,可以随便改
    private void scaleUp() {
        ViewCompat.animate(this)
                .setDuration(200)
                .scaleX(1.04f)
                .scaleY(1.04f)
                .start();
    }

    private void scaleDown() {
        ViewCompat.animate(this)
                .setDuration(200)
                .scaleX(1f)
                .scaleY(1f)
                .start();
    }
}
