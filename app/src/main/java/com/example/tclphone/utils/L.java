package com.example.tclphone.utils;

import android.util.Log;

import com.example.tclphone.BuildConfig;


public class L {
    private static final String TAG = "tv5gLog";
    private static boolean isDebug = BuildConfig.DEBUG;


    public static void d(String msg,String args){
        if (!isDebug){
            return;
        }
        Log.d(TAG,msg+"  "+args);
    }

    public static void i(String msg,String args){
        if (!isDebug){
            return;
        }
        Log.i(TAG,msg+"  "+args);
    }

}
