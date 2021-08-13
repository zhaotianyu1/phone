package com.example.tclphone.broadcastreceiver;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.tcl.uicompat.TCLToast;

public class BlueToothReceiver extends BroadcastReceiver {

    private static final String TAG = "PZR_broadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG,"广播开始 action = " + action );
        if(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action))
        {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,-1);
            Log.i(TAG,"BluetoothAdapter state : "+state);
            switch (state){
                case BluetoothAdapter.STATE_CONNECTED:
                    Log.i(TAG,"STATE_CONNECTED");
                    TCLToast.makeText(context,"当前版本不支持蓝牙设备",TCLToast.LENGTH_SHORT).show();
                    break;
            }
        }

    }
}
