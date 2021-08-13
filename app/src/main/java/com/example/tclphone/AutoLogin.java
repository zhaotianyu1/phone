package com.example.tclphone;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;

import com.example.tclphone.broadcastreceiver.TrildCeckReceiver;
import com.example.tclphone.utils.L;
import com.example.tclphone.utils.NativeMethodList;
import com.juphoon.cmcc.lemon.MtcCliCfg;
import com.juphoon.rcs.login.module.JusLoginDelegate;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.juphoon.rcs.login.module.JusAccountDefine.RESOLUTION_1280_720;
import static com.juphoon.rcs.login.module.JusLoginDelegate.ACCOUNT_FEC;
import static com.juphoon.rcs.login.module.JusLoginDelegate.ACCOUNT_FRAMERATE_CONTROL;
import static com.juphoon.rcs.login.module.JusLoginDelegate.ACCOUNT_RESOLUTION_CONTROL;
import static com.juphoon.rcs.login.module.JusLoginDelegate.ACCOUNT_RESOLUTION_MAX;
import static com.juphoon.rcs.login.module.JusLoginDelegate.ACCOUNT_VIDEO_BITRATE_VALUE;
import static com.juphoon.rcs.login.module.JusLoginDelegate.setCallSetting;
import static com.juphoon.rcs.login.module.JusLoginDelegate.setVideoArsParm;
import static com.juphoon.rcs.login.module.JusLoginDelegate.tclLogin;

public class AutoLogin {

    private static final String TAG = "PZR_AL";

    private static final int RETURN_SUCCESS = 200;
    private static final  AutoLogin autoLogin = new AutoLogin();
    //与菊凤登录有关的三个参数
    String impi;
    String impu;
    String realm;
    String nInPCSCFAddrd;
    static volatile String localIp;
    String localIp1;
    String localIpPort;

    private AutoLogin(){
    }

    public static AutoLogin getInstance(){
            return autoLogin;
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(@NonNull Message msg) {
            if(msg.what==RETURN_SUCCESS){
                localIp =(String) msg.obj;
                HashMap<String,String> map = new HashMap();
                //开启订阅注册,0代表否
                map.put(JusLoginDelegate.ACCOUNT_OPEN_SUBS,"0");
                //开始注册前配置
                JusLoginDelegate.setCallSetting(impi,map);

                MtcCliCfg.Mtc_CliCfgSetTmrLenWaitReg(30);

                if (localIp.contains("%")) {
                    localIp1 = localIp.substring(0, localIp.indexOf("%"));
                    Log.w(TAG,"local1------>"+localIp);
                    if(!RcsLoginManager.isLogined()){
                        tclLogin(impu, impi, realm, nInPCSCFAddrd, localIp1);
                    }

                }else{
                    Log.w(TAG,"localIp tcl_Login---------->"+localIp);
                    if(!RcsLoginManager.isLogined())
                    {
                        tclLogin(impu,impi,realm,nInPCSCFAddrd,localIp);
                    }
                }
                HashMap<String, String> params = new HashMap<>();
                //自适应帧速率
                params.put(ACCOUNT_FRAMERATE_CONTROL, "1");
                //最大帧速率，因为设置了自适应帧速率，所以不需要此项
                //            params.put( ACCOUNT_FRAMERATE_MAX,"20");
                //自适应码率 0 false
                params.put(ACCOUNT_RESOLUTION_CONTROL, "0");
                //最大分辨率
                params.put(ACCOUNT_RESOLUTION_MAX, RESOLUTION_1280_720);
                //音量冗余，0，false
                params.put(ACCOUNT_FEC, "0");
                //视频冗余，1，true
//            params.put(ACCOUNT_VIDEO_FEC, "1");
                //丢包重传,1,true
//            params.put(ACCOUNT_NACK, "1");
                //发送端降噪等级
//            params.put( ACCOUNT_SEND_ANR_MODE, ANR_MID  );
                //码率
                params.put(ACCOUNT_VIDEO_BITRATE_VALUE, "500");
                //设置媒体参数
                setCallSetting(impi, params);
                //设置ars接口  impi:accoun账号 brHi:码率最大值  brLo:	码率最小值  frHi:帧速率最大值  frLo:帧速率最小值
                setVideoArsParm(impi, 1200 * 1000, 1000 * 1000, 15, 10);

                //0， 是软编软解;MEDIACODEC_NORMAL = 0;
                //1.   是硬编软解；MEDIACODEC_ENCODER = 1
                //2.  是软编硬解；MEDIACODEC_DECODER = 2
                //3.  是硬编硬解；MEDIACODEC_ENCODER_AND_DECODER = 3
                int mediaCodec = JusLoginDelegate.MEDIACODEC_NORMAL;
                //设置媒体编解码方式
                JusLoginDelegate.setMediaCodec(mediaCodec);
                Log.i("PZR","state6");

            }
            super.handleMessage(msg);
        }
    };

    class MyThread extends Thread{
        @Override
        public void run() {

            ArrayList al = Trild_GetThreeParameters();
            impi = (String) al.get(0);
            impu = (String) al.get(1);
            realm = (String) al.get(2);


            Log.w("PZR","impu:"+impu);
            Log.w("PZR","realm:"+realm);
            Log.w("PZR","impi:"+impi);

            String nInPCSCFAddrc = "1";
            String PrefixLengthIPAddrc = "1";
            nInPCSCFAddrd = Trild_GetnInPCSCFAddr(nInPCSCFAddrc, PrefixLengthIPAddrc);
            //getRelevantPra();
            do{
                Log.i(TAG,"localIp is ready-------");
                try {
                    Log.w(TAG,"localIpPort"+localIpPort);
                    localIp = getIpAddress(localIpPort);
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (localIp==null);
            Log.w(TAG,"localIp is : "+localIp);
            Message msg = new Message();
            msg.what =RETURN_SUCCESS;
            msg.obj = localIp;
            handler.sendMessage(msg);

        }
    };
    private IntentFilter intentFilter;

    public void autoLogin(Context context) throws SocketException, InterruptedException {

        Log.i(TAG, "开始发起广播邀请-----------------");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "开始Trild_Check-----------------");
                TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
                int state=tm.getSimState();
                if(state == TelephonyManager.SIM_STATE_READY){
                    Log.i(TAG, "有SIM卡发起Trild_Check----------------");
                    NativeMethodList.TrildCheck();
                }else{
                    Log.i(TAG, "没有SIM卡-----------------");
                }
            }
        }).start();


}
    public  void login(String str){

        if (RcsLoginManager.isLogined()){
            Log.w(TAG, "onLogin: RcsLoginManager.isLogined()"+RcsLoginManager.isLogined());
        }
        else{
            Log.w(TAG, "onLogin: RcsLoginManager.isLogined()"+RcsLoginManager.isLogined());
//            impu = JusLoginDelegate.getImpu();
//            realm = JusLoginDelegate.getRealmAddr();
//            impi = JusLoginDelegate.getImpi();

            localIpPort = str;
            Log.i(TAG,"localIpPort : "+localIpPort);

            Log.i(TAG,"localIpPort : "+localIpPort);

            MyThread thread = new MyThread();
            thread.start();


//            getRelevantPra();
        }
    }
    /**
     * Get Ip address 自动获取IP地址
     *
     * @throws SocketException
     */
    public static String getIpAddress(String ipType) throws SocketException {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                if (ni.getName().equals(ipType)) {
                    Enumeration<InetAddress> ias = ni.getInetAddresses();
                    while (ias.hasMoreElements()) {
                        ia = ias.nextElement();
                        if (ia instanceof Inet4Address) {
                            continue;// skip ipv4
                        }
                        if(ia.isMCGlobal()){
                            String ip = ia.getHostAddress();

                            break;
                        }else{
                            String ip = ia.getHostAddress();

                            if(ip.length()>32){
                                hostIp = ia.getHostAddress();
                                break;
                            }
                            // 过滤掉127段的ip地址
                            else if (!"127.0.0.1".equals(ip)) {
                                hostIp = ia.getHostAddress();
                                //break;
                            }
                        }

                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        Log.w(TAG, "get the IpAddress--> " + hostIp + "");
        return hostIp;
    }
    public native String Trild_GetnInPCSCFAddr(String nInPCSCFAddrc ,String PrefixLengthIPAddrc);


    public native String Trild_1GetnImsIpAddr(String nInPCSCFAddrc, String nInPCSCFAddrcLen);


    public native ArrayList<String> Trild_GetThreeParameters();


}

