package com.example.tclphone;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.SIM;
import com.example.tclphone.db.SimHelper;
import com.example.tclphone.utils.L;

import com.example.tclphone.litepal.LitePal;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 读取SIM卡的服务
 */
public class SimService extends Service {

    private final String TAG = "ZTY_SimService";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //定时器每隔2秒发送一次广播检测
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Intent timeIntent = new Intent();
                timeIntent.setAction("SIM_CHANGE");//自定义Action
                sendBroadcast(timeIntent); //发送广播
            }
        }, 0, 2000);
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
}
