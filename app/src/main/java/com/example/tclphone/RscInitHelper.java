package com.example.tclphone;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.juphoon.rcs.login.module.JusLoginDelegate;
import com.juphoon.rcs.login.sdk.common.RcsCommonUtils;

import java.net.SocketException;

public class RscInitHelper {
    static {
        System.loadLibrary("native-lib");
    }

    public static final String TAG = "PZR_init";

    private static boolean isInit = false;

    private static Context aContext;

    private static Context bConxtext;

    public static RscInitHelper rscInitHelper = new RscInitHelper();

    private RscInitHelper(){

    }

    public static RscInitHelper getInstance(){
        return rscInitHelper;
    }


    @RequiresApi(api= Build.VERSION_CODES.O)
    public int initSDK(Context appContext,Context baseContext) throws SocketException {
        Log.i(TAG,"isInit1 : " + isInit);
        if(!isInit){
            Log.i(TAG,"statt initSDK");
            isInit = true;
            Log.i(TAG,"isInit2 : " + isInit);
            aContext = appContext;
            bConxtext = baseContext;
           int initSDKResult = JusLoginDelegate.loadLibrary(aContext,true,true);
            Log.i(TAG,"initSDKResult : begin");
            if(initSDKResult== JusLoginDelegate.RESULT_SUCCESS){
                if(JusLoginDelegate.existsLicenseInFiles(bConxtext)){
                    initSDKResult = JusLoginDelegate.initSDKWithOfflineLicense(bConxtext);
                }else if (JusLoginDelegate.existsLicenseInAsset(bConxtext)){
                    String licensePath = bConxtext.getFilesDir().getAbsolutePath()+"/license.sign";
                    RcsCommonUtils.saveAssetFile(bConxtext,"license.sign",licensePath);
                    initSDKResult = JusLoginDelegate.initSDKWithOfflineLicense(bConxtext);
                }
            }
            Log.i(TAG,"initSDKResult : finished");
            return initSDKResult;
        }else {
            return JusLoginDelegate.RESULT_SUCCESS;
        }

    }

    public boolean getIsInit(){
        return isInit;
    }

}






