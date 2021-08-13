package com.example.tclphone;


import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "PZR_BaseActivity";

    private MyApplication application;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        application = MyApplication.getInstance();
        addActivity();
    }

    public void addActivity(){
        Log.i(TAG,"添加Activity");
        application.addActivity(this);
    }

    public void removeActivity(){
        application.removeActivity(this);
    }

    public void removeALLActivity() {
        application.removeAllActivity();// 调用myApplication的销毁所有Activity方法
    }

    @Override
    protected void onDestroy() {
        application.removeActivity(this);
        super.onDestroy();
    }
}
