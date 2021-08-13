package com.example.tclphone.broadcastreceiver;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.tclphone.AutoLogin;
import com.example.tclphone.CallService;
import com.example.tclphone.MyApplication;



public class BootBroadcastReceiver extends BroadcastReceiver {

    private static  final String TAG = "PZR_BBR";

    private MyApplication application = MyApplication.getInstance();
//    private static final String ACTION_BOOT = "android.intent.action.BOOT_P0";

    private static final String TIME_UPGRADE = "com.tcl.TOTUpdate";


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(TIME_UPGRADE.equals(action)){
            Log.i(TAG,"");
            Log.i(TAG,"");
        }


    }


}
