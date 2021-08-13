package com.example.tclphone.dialog;

import android.app.AlertDialog;
import android.content.Context;

public class CallDialog extends AlertDialog {
    protected CallDialog(Context context) {
        super(context);
    }

    protected CallDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    protected CallDialog(Context context, int themeResId) {
        super(context, themeResId);
    }
}
