package com.example.tclphone;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.example.tclphone.broadcastreceiver.SimStateReceive;
import com.example.tclphone.broadcastreceiver.TrildCeckReceiver;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.db.SIM;
import com.example.tclphone.db.SimHelper;
import com.example.tclphone.utils.L;
import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.cmcc.lemon.MtcCommonConstants;
import com.juphoon.rcs.login.module.JusLoginDelegate;
import com.juphoon.rcs.login.sdk.listener.RcsLoginListener;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.areaconfiger.HardwareInfo;
import com.tcl.tvmanager.TTvFunctionManager;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


import static com.juphoon.rcs.login.module.JusLoginDelegate.RESULT_SUCCESS;


public class CallService extends Service implements JusLoginDelegate.JusLicenseListener {

    public static final String LOGIN_TIMES_ONE_HOURE = "login_times_one_hour";
    private static final String TAG = "PZR_Service";
    private ProgressDialog mLicenseProgressDialog;
    public static int initSDKResult = -1;
    PhoneHelper phoneHelper = new PhoneHelper();
    private NotificationManager notificationManager;
    private String notificationId = "channel_Id";
    private String notificationName = "channel_Name";
    private static boolean listenerFlag = false;

    SimHelper simHelper = new SimHelper();
    MyContactsHelper contactsHelper = new MyContactsHelper();
    SimStateReceive simStateReceive;

    MyApplication application = MyApplication.getInstance();

    private static String localIpPort;

    private static final String TRILD_CHECK_SERVICE = "com.tclphone.ims.network.up.SERVICE";

    private static final String UPGRADE = "android.intent.action.MY_PACKAGE_REPLACED";


    private Handler btHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                PropertyUtils.set("com.tcl.android.phone", getPhoneNumber());
                L.i(TAG, "??????SIM????????? ");
            }
            super.handleMessage(msg);
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {

        super.onCreate();

        MyApplication myApplication = (MyApplication) getApplicationContext();

        myApplication.setIsServiceRunning(true);

        Log.i(TAG, "isCallServiceRunning ???" + myApplication.getIsServiceRunning());

        Log.i(TAG, "start call service");
        PropertyUtils.set("com.tcl.android.phoneState", "CALL_STATE_IDLE");
        PropertyUtils.set("com.tcl.phoneState", "0");

        try {
            initSDKResult = RscInitHelper.getInstance().initSDK(getApplicationContext(), getBaseContext());
            application.setInitSDKResult(initSDKResult);
            if (initSDKResult != RESULT_SUCCESS) {
                String resultTitle = titleWithSDKResult(initSDKResult);
                Log.i(TAG, resultTitle);
                return;
            } else {
                Log.i(TAG, "sdk load success");
            }
        } catch (SocketException e) {
            Log.i(TAG, "sdk load failed");
            e.printStackTrace();
        }

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        //??????NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = getNotification();

        startForeground(1, notification);

        simStateReceive = new SimStateReceive();
        IntentFilter filter = new IntentFilter();
        filter.addAction(SimStateReceive.ACTION_SIM_STATE_CHANGED);
        filter.addAction(SimStateReceive.ACTION_SHUTDOWN);
//        filter.addAction(SimStateReceive.SOURCE_CHANGE);
//        filter.addAction(TrildCeckReceiver.TRILD_CHECK);
//        filter.addAction(TrildCeckReceiver.TRILD_DOWN);
        registerReceiver(simStateReceive, filter);
        Log.i(TAG, "??????SIM?????????-----------------??? ");

        registerTSA();


        if (initSDKResult == RESULT_SUCCESS) {
            MainCallDBManager.getInstance().init(this);

        }

        try {
            Log.i(TAG, "upgrade autoLogin");
            AutoLogin.getInstance().autoLogin(this);
        } catch (SocketException | InterruptedException e) {
            e.printStackTrace();
        }



    }

    private void registerTSA() {
        boolean bool = TTvFunctionManager.getInstance(getApplicationContext()).systemAuthorized(RecordConstants.TSA_KEY);
        if (bool) {
            Log.i(TAG, "??????TSA??????");
        } else {
            Log.i(TAG, "??????TSA??????");
        }
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("5g??????")
                .setContentText("");

        //??????Notification???ChannelID,????????????????????????

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(notificationId);
        }
        Notification notification = builder.build();
        return notification;
    }


    @Override
    public void onDestroy() {

        Log.i(TAG, " CallService onDestroy!");
        unregisterReceiver(simStateReceive);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startedId) {
        if (intent != null) {
            String action = intent.getAction();
            if(action == null){
                //????????????--SIM?????????/??????
                MyApplication.getInstance().setReady(false);
            }
            Log.i(TAG, "??????action=======" + action);

            register_Login(intent, action);
            registerUpgrade(this, action);

        }
        return Service.START_STICKY;

    }


    private void registerUpgrade(Context context, String action) {
        if (UPGRADE.equals(action)) {
            Log.i(TAG, "upgrade begin");
            try {
                Log.i(TAG, "upgrade autoLogin");
                AutoLogin.getInstance().autoLogin(context);
            } catch (SocketException | InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "upgrade finished");

        }
    }

    private void register_Login(Intent intent, String action) {
        if (TRILD_CHECK_SERVICE.equals(action)) {
            Log.i(TAG, "????????????");
            localIpPort = intent.getStringExtra("localIp");
            Log.i(TAG, "localIp(service) : " + localIpPort);
            AutoLogin.getInstance().login(localIpPort);
            Log.i(TAG, "????????????");
            if (!listenerFlag) {
                listenerFlag = true;
                setLoginTimesOneHour(20);
                RcsLoginManager.addRegisterListener(new RcsLoginListener() {
                    @Override
                    public void onBeforeRegister() {

                    }

                    @Override
                    public void onRegisterStateChanged(int state, int i1, String s) {
                        Log.i(TAG, " onRegisterStateChanged");
                        switch (state) {
                            case RcsLoginManager.MTC_REG_STATE_REGED://????????????
                                Log.i(TAG, "????????????");
                                //??????????????????
                                MyApplication.getInstance().setReady(true);
                                //??????????????????
                                String is_phone = getPhoneNumber();
                                if (is_phone.equals("")) {
                                    //??????????????????????????????
                                    L.i(TAG, "???????????????---------------??? ");
                                    send_message();
                                } else {
                                    L.i(TAG, "???????????????---------------??? " + is_phone);
                                }
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        read_Sims();
                                    }
                                }).start();
                                final Timer t = new Timer();
                                t.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        t.cancel();
                                    }
                                }, 3000);
                                Message msg = new Message();
                                msg.what = 1;
                                btHandler.sendMessage(msg);
                                break;
                            case RcsLoginManager.MTC_REG_STATE_IDLE://????????????
                                Log.i(TAG, " ????????????");

                                if (application.getIsNetworkUp()) {
                                    if (getLoginTimesOneHour() <= 20) {
                                        AutoLogin.getInstance().login(localIpPort);
                                    }
                                }

                                break;
                            case RcsLoginManager.MTC_REG_STATE_REGING://????????????
                                Log.i(TAG, " MTC_REG_STATE_REGING 3");
                                break;
                            case RcsLoginManager.MTC_REG_STATE_UNREGING://?????????
                                Log.i(TAG, " MTC_REG_STATE_UNREGING 4");
                                break;
                        }

                    }
                });
            }

        }

    }

    public static int getLoginTimesOneHour() {
        long nowHour = System.currentTimeMillis() / (3600 * 1000);
        String times = PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).getString(LOGIN_TIMES_ONE_HOURE, "");
        if (!TextUtils.isEmpty(times)) {
            String[] values = times.split("-");
            if (values.length == 2) {
                if (TextUtils.equals(String.valueOf(nowHour), values[0])) {
                    return Integer.valueOf(values[1]);
                }
            }
        }
        return 0;
    }

    public static void setLoginTimesOneHour(int times) {
        PreferenceManager.getDefaultSharedPreferences(MyApplication.getContext()).edit().putString(LOGIN_TIMES_ONE_HOURE, String.format("%d-%d", System.currentTimeMillis() / (3600 * 1000), times)).apply();
    }

    private static String titleWithSDKResult(int result) {

        String resultTitle = "";
        switch (result) {
            case JusLoginDelegate.RESULT_GET_DEVICEID_ERROR:
                resultTitle = "??????Device ID??????";
                break;
            case JusLoginDelegate.RESULT_CPU_NOT_SUPPORTED:
                resultTitle = "??????CPU?????????";
                break;
            case JusLoginDelegate.RESULT_SDK_FLODER_CREATE_ERROR:
                resultTitle = "SDK?????????????????????";
                break;
            case JusLoginDelegate.RESULT_UNSATISFIED_LINK_ERROR:
                resultTitle = "SO???????????????";
                break;
            case JusLoginDelegate.RESULT_LICENSE_EXPIRED:
                resultTitle = "license??????";
                break;
            case JusLoginDelegate.RESULT_INIT_ERROR:
                resultTitle = "SDK???????????????";
                break;
            case JusLoginDelegate.RESULT_SUCCESS:
                resultTitle = "SDK???????????????";
                break;
            case JusLoginDelegate.RESULT_DOWNLOAD_LICENSE_ERROR:
                resultTitle = "license???????????????license??????";
                break;
            case MtcCommonConstants.ZFAILED:
                resultTitle = "??????????????????";
                break;
        }
        return resultTitle;
    }


    private void dissmissLicenseProgressDialogIfNeed() {

        if (mLicenseProgressDialog != null) {
            mLicenseProgressDialog.dismiss();
            mLicenseProgressDialog = null;
        }
    }

    @Override
    public void didDownloadLicense(int result) {
        dissmissLicenseProgressDialogIfNeed();
        if (result == RESULT_SUCCESS) {
            MainCallDBManager.getInstance().init(getBaseContext());
        } else {
            String resultTitle = titleWithSDKResult(result);
            Log.i(TAG, resultTitle + " didDownloadLicense resultTitle ");
        }
    }

//    /**
//     * ??????SIM?????????
//     */
//    public void read_Sims() {
//        //????????????
//        String old_simiccid = "";
//        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        List<SIM> simList = simHelper.loadSim();
//        if (simList.size() > 0) {
//            old_simiccid = simList.get(simList.size() - 1).getIccid();
//        }
//        Log.w(TAG, "old_simiccid??? " + old_simiccid);
//        //??????SIM???????????????
//        String iccid = "N/A";
//        iccid = manager.getSimSerialNumber();
//        if (iccid != null) {
//            simHelper.addSim(iccid);
//        }
//        Log.w(TAG, "iccid??? " + iccid);
//    }

    /**
     * ???????????????????????????
     */
    public void send_message() {
        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        int networkType = manager.getNetworkType();
        if (networkType == TelephonyManager.NETWORK_TYPE_NR) {//5G??????
            Log.i(TAG, "??????5G??????");
            MyApplication.getInstance().setType( TelephonyManager.NETWORK_TYPE_NR);
            convert_network();
            MyApplication.getInstance().setConvert(true);
            sendMessage(manager);
            Log.i(TAG, "5G????????????4G??????---------");
        } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {//4G??????
            Log.i(TAG, "??????4G??????");
            MyApplication.getInstance().setType( TelephonyManager.NETWORK_TYPE_LTE);
            sendMessage(manager);
        }
}

    public void sendMessage(TelephonyManager manager){
        String operator = manager.getSimOperator();
        SmsManager sms = SmsManager.getDefault();
        if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")) {
            local_phonenumber();
            L.i(TAG, "?????????????????????---------------??? ");
            //??????????????????????????????
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10086";
                String message = "Bj";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
                L.i(TAG, "?????????????????????---------------??? ");
            }
        } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010")) {//????????????
            //??????????????????
            local_phonenumber();
        } else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//????????????
            local_phonenumber();
            L.i(TAG, "?????????????????????---------------??? ");
            //??????????????????????????????
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10001";
                String message = "701";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
                L.i(TAG, "?????????????????????---------------??? ");
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(50000);
                            if(MyApplication.getInstance().isConvert()){
                                L.i(TAG, "50s no receive---------------??? ");
                                 convert_network4G_5G();
                                 MyApplication.getInstance().setConvert(false);
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }
    }

    public void convert_network(){
        Class<?> c = null;
        int NETWORK_MODE_LTE_ONLY = 11;
        try {
            L.i(TAG, "begin---");
            c = Class.forName("android.telephony.TelephonyManager");
            if(c == null){
                L.i(TAG, "c==null--");
            }
            Constructor cons = c.getDeclaredConstructor();
            cons.setAccessible(true);
            TelephonyManager telephonyManager = (TelephonyManager) cons.newInstance();
            Method set = c.getMethod("setPreferredNetworkType", int.class, int.class);
            set.invoke(telephonyManager,NETWORK_MODE_LTE_ONLY,11);
            L.i(TAG, "NETWORK_MODE_LTE_ONLY---"+NETWORK_MODE_LTE_ONLY);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    /**
     * 4G???5G??????
     */
    public void convert_network4G_5G(){
        Class<?> c = null;
        int NETWORK_MODE_LTE_ONLY = 23;
        try {
            L.i(TAG, "begin---");
            c = Class.forName("android.telephony.TelephonyManager");
            if(c == null){
                L.i(TAG, "c==null--");
            }
            L.i(TAG, "begin---1");
            Constructor cons = c.getDeclaredConstructor();
            cons.setAccessible(true);
            TelephonyManager telephonyManager = (TelephonyManager) cons.newInstance();
            L.i(TAG, "begin---2");
            Method set = c.getMethod("setPreferredNetworkType", int.class, int.class);
            L.i(TAG, "begin---3");
            set.invoke(telephonyManager,23,23);
            L.i(TAG, "NETWORK_MODE_LTE_ONLY---"+NETWORK_MODE_LTE_ONLY);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void local_phonenumber() {
        //???????????????????????????
        // local_phone = findViewById(R.id.local_phone);

        TelephonyManager tms = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        @SuppressLint("MissingPermission")
        String phoneNumber = tms.getLine1Number();
        if(phoneNumber != null || "".equals(phoneNumber)) {
            phoneHelper.addPhones(phoneNumber);
        }
    }


    public String getPhoneNumber(){
        List<Phone> phoneList=phoneHelper.loadPhones();
        String phone="";
        if(phoneList.isEmpty()||phoneList.get(phoneList.size() - 1).getPhone().equals("")){
            phone="";
        }else {
            phone=phoneList.get(phoneList.size() - 1).getPhone();
        }
        return phone;
    }
    protected Cursor mCursor = null;
    
    public void read_Sims() {
        long startTime1 = System.currentTimeMillis();//??????????????????
        Log.i(TAG, "read_Sims?????????SIM?????????-----------------------------3");
        //????????????SIM????????????url
        String str = "content://icc/adn";
        Intent intent = new Intent();
        intent.setData(Uri.parse(str));
        Uri uri = intent.getData();
        //??????SIM??????????????????
        try {
            mCursor = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.i(TAG, "read_SimsSIM???????????????-------");
            e.printStackTrace();
        }
        long endTime1 = System.currentTimeMillis();    //??????????????????
        Log.w(TAG, "read_Sims???SIM????????????????????? " + (endTime1 - startTime1) + "ms");
        Log.w(TAG, "read_Sims-mCursor.size-----------" + mCursor.getCount());
        //SIM??????????????????
        if (mCursor == null) {
            Log.i(TAG, "read_SimsSIM???????????????");

            return;
        }
        //SIM??????????????????
        if (mCursor != null) {
            Log.i(TAG, "MyApplication.getInstance().isCursor()--------------"+MyApplication.getInstance().isCursor());
            if(MyApplication.getInstance().isCursor()) {
                List<Person> contacts = contactsHelper.loadAllSimContacts();
                Log.i(TAG, "?????????????????????--------------");
                Log.i(TAG, "contacts.size()"+contacts.size());
                if (contacts.size() != mCursor.getCount()) {
                    while (mCursor.moveToNext()) {
                        // ?????????????????????
                        int nameFieldColumnIndex = mCursor.getColumnIndex("name");
                        // ??????????????????
                        int numberFieldColumnIndex = mCursor
                                .getColumnIndex("number");
                        if(mCursor.getString(nameFieldColumnIndex).equals(mCursor.getString(numberFieldColumnIndex))){
                            contactsHelper.addContacts("", mCursor.getString(numberFieldColumnIndex), 1, 2);
                        }else {
                            contactsHelper.addContacts(mCursor.getString(nameFieldColumnIndex), mCursor.getString(numberFieldColumnIndex), 1, 2);
                        }
                    }
                }
                MyApplication.getInstance().setCursor(false);
            }
            Log.i(TAG, "????????????SIM??????????????????--------------");
            while (mCursor.moveToNext()) {
                // ?????????????????????
                int nameFieldColumnIndex = mCursor.getColumnIndex("name");
                // ??????????????????
                int numberFieldColumnIndex = mCursor
                        .getColumnIndex("number");
                Log.i(TAG, "name: "+mCursor.getString(nameFieldColumnIndex)+"phoneNumber: "+mCursor.getString(numberFieldColumnIndex));
            }

            Log.i(TAG, "read_SimsSIM?????????????????????--------------");
        }

        //???????????????????????????
        mCursor.close();
    }

}



