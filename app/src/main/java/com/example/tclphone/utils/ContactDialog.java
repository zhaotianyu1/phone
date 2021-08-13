package com.example.tclphone.utils;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.tclphone.R;
import com.example.tclphone.constants.RecordConstants;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;


public class ContactDialog extends Dialog {

    public ContactDialog(@NonNull Context context) {
        super(context);
    }

    public ContactDialog(Context context, int theme) {
        super(context, theme);
    }
    public static class Builder {
        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;
        private OnClickListener positiveButtonClickListener;
        private OnClickListener negativeButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
        /**         * Set the Dialog message from resource
         * *
         * * @param
         * * @return
         * */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }
        /**
         * * Set the Dialog title from resource
         * *
         * * @param title
         * * @return
         * */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }
        /**
         * * Set the Dialog title from String
         * *
         * * @param title
         * * @return
         * */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }
        /**
         * * Set the positive button resource and it's listener
         * *
         * * @param positiveButtonText
         * * @return
         * */

        public Builder setPositiveButton(String positiveButtonText, OnClickListener listener)
        {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, OnClickListener listener)
        {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }
        @SuppressLint("WrongViewCast")
        public ContactDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final ContactDialog dialog = new ContactDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.select_call_mode, null);
            dialog.addContentView(layout, new LinearLayout.LayoutParams( LinearLayout.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.WRAP_CONTENT));
            // set the dialog title
            //((TextView) layout.findViewById(R.id.title)).setText(title);
            // set the confirm button
            if (positiveButtonText != null) {
                //((Button) layout.findViewById(R.id.buttVideo)).setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    ((AllCellsGlowLayout) layout.findViewById(R.id.buttVideo)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                    layout.findViewById(R.id.buttVideo).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if(hasFocus){
                                ((TextView)layout.findViewById(R.id.video_call_text)).setTextColor(RecordConstants.WHITE_E6);
                            }else {
                                ((TextView)layout.findViewById(R.id.video_call_text)).setTextColor(RecordConstants.WHITE_4D);
                            }
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.buttVideo).setVisibility(
                        View.GONE
                );
            }
            //set the cancel button
            if (negativeButtonText != null) {
                //((Button) layout.findViewById(R.id.buttAudio)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((AllCellsGlowLayout) layout.findViewById(R.id.buttAudio)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                    layout.findViewById(R.id.buttAudio).setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if(hasFocus){
                                ((TextView)layout.findViewById(R.id.audio_call_text)).setTextColor(RecordConstants.WHITE_E6);
                            }else {
                                ((TextView)layout.findViewById(R.id.audio_call_text)).setTextColor(RecordConstants.WHITE_4D);
                            }
                        }
                    });
                }


            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.buttAudio).setVisibility(
                        View.GONE
                );
            }
            // set the content message
            if (message != null) {
                // ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
            }
            dialog.setContentView(layout);
            return dialog;
        }
    }


}
