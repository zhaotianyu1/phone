package com.example.tclphone.broadcastreceiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;


import androidx.core.app.ActivityCompat;

import com.example.tclphone.CallService;
import com.example.tclphone.MyApplication;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.db.SIM;
import com.example.tclphone.db.SimHelper;
import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.rcs.call.sdk.RcsCallDefines;
import com.juphoon.rcs.call.sdk.RcsCallSession;
import com.juphoon.rcs.call.sdk.manager.RcsCallManager;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.uicompat.TCLToast;

import java.util.ArrayList;
import java.util.List;

import static com.juphoon.rcs.login.module.JusLoginDelegate.RESULT_SUCCESS;

public class TrildCeckReceiver extends BroadcastReceiver {

    private static final String TAG = "PZR_TrildCeckReceiver";
    public static final String TRILD_CHECK = "com.tclphone.ims.network.up";
    public static final String TRILD_DOWN =  "com.tclphone.ims.network.down";
    public static final String TRILD_CHECK_SERVICE =  "com.tclphone.ims.network.up.SERVICE";


    public final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public final static String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
    private final static int SIM_VALID = 0;
    private final static int SIM_INVALID = 1;
    private int simState = SIM_INVALID;
    private static Integer count =0 ;

    public int getSimState() {
        return simState;
    }
    boolean is_Read = false;
    PhoneHelper phoneHelper = new PhoneHelper();
    MyContactsHelper contactsHelper = new MyContactsHelper();
    protected Cursor mCursor = null;
    SimHelper simHelper = new SimHelper();
    private Handler btHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 2:
                    Log.i(TAG, "??????SI---------------??????---");
                    break;
            }
        }
    };
    private List<Activity> list = new ArrayList<Activity>();


    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.i(TAG, "??????action------"+action);

        MyApplication application = MyApplication.getInstance();
        String ifname = intent.getStringExtra("ifname");
        Log.i(TAG,"ifname : "+ifname);

        Log.i(TAG, "??????????????????------");



        Log.i(TAG, "simState??????-----------??? ");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        int state=tm.getSimState();
        Log.i(TAG, "simState??????-----------state??? " +state);


        if(TRILD_CHECK.equals(action)) {
            //AutoLogin.getInstance().login();
            application.setIsNetworkUp(true);
            Log.i(TAG,"setIsNetworkUp : "+application.getIsNetworkUp());
            Intent intent1 = new Intent(context, CallService.class);
            intent1.setAction(TRILD_CHECK_SERVICE);
            intent1.putExtra("localIp",ifname);
            if(application.isSupport5G){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.i(TAG, "??????Trild??????------");
                    context.startForegroundService(intent1);
                    Log.i(TAG, "??????Trild??????------");
                } else {
                    Log.i(TAG, "??????Trild??????------");
                    context.startService(intent1);
                    Log.i(TAG, "??????Trild??????------");
                }

            }else {
                Log.i(TAG,"???????????????5G??????");
            }

            Log.i(TAG, "??????????????????------");
        }

        if(TRILD_DOWN.equals(action)){
            application.setIsNetworkUp(false);
            if(application.getInitSDKResult()==RESULT_SUCCESS && RcsLoginManager.isLogined()){
                Log.i(TAG,"Trild_down and close login start");
                RcsLoginManager.close();
                Log.i(TAG,"Trild_down and close login end");
            }
        }


        if(intent.getAction().equals(ACTION_SHUTDOWN)){
            Log.i(TAG, "ACTION_SHUTDOWN-----------state????????? ");
        }
        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED))
        {
            //?????????SIM????????????
            if(state != TelephonyManager.SIM_STATE_READY)
            {
                is_noready(tm,state,context);
                termCalling(context);
                Log.i(TAG, "???SIM_STATE_READY-----------??? ");

            }else {
                if(count%2==0)
                {
                    getPhoneNumber();
                    sim_state_message(tm,state,context);
                    count = 1;
                    Log.i(TAG, "read end-----------??? ");
                }else {
                    count = 0;
                }

            }
        }

    }

    //????????????sim????????????????????????
    public void termCalling(Context context){
        ArrayList<RcsCallSession> RcsCallSessionLists = RcsCallManager.getInstance().getListCallSessions();
        int size = RcsCallSessionLists.size();
        Log.i(TAG,"tonghuageshu : "+size);
        if(size!=0){
            for( RcsCallSession rcsList : RcsCallSessionLists)
            {
                Log.i(TAG,"??????TclActivity");
                rcsList.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
            }

        }

        MyApplication.getInstance().removeAllActivity();

        if(RcsLoginManager.isLogined()){
            Log.i(TAG,"??????close????????????----------");
            RcsLoginManager.close();
            Log.i(TAG,"??????close????????????----------");
        }
        TCLToast.makeText(context,"SIM?????????",TCLToast.LENGTH_SHORT).show();
    }

    public void exit(Context context) {
//        for (Activity activity : list) {
//            activity.finish();
//            Log.i(TAG, "activity end-----------??? ");
//        }
        System.exit(0);
    }

    /**
     * ???SIM??????????????????
     * @param tm
     * @param state
     * @param context
     */
    public void sim_state_message(TelephonyManager tm,int state,Context context){
        switch (state) {
            case TelephonyManager.SIM_STATE_READY:
                simState = SIM_VALID;

                String old_simiccid = "";
                List<SIM> simList = simHelper.loadSim();
                if (simList.size() > 0) {
                    old_simiccid = simList.get(simList.size() - 1).getIccid();
                }
                Log.w(TAG, "old_simiccid??? " + old_simiccid);
                //??????SIM???????????????
                String iccid = "N/A";
                iccid = tm.getSimSerialNumber();
                Log.w(TAG, "iccid??? " + iccid);
                if (iccid != null) {
                    simHelper.addSim(iccid);
                }

                //??????SIM???????????????????????????????????????????????????SIM???????????????
                if (!old_simiccid.equals(iccid)) {
                    is_Read = false;
                    List<Person> contacts = contactsHelper.loadAllSimContacts();
                    if (contacts.size() > 0) {
                        Log.w(TAG, "contacts??????????????? " + contacts.size());
                        for (int i = 0; i < contacts.size(); i++) {
                            contactsHelper.deleteContacts(contacts.get(i).getId());
                        }
                    }
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String local = tm.getLine1Number();
                    if(local!=null) {
                        phoneHelper.addPhones(local);
                    }
//                    send_message(tm);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            read_Sim();
                            Message msg = btHandler.obtainMessage();
                            msg.what = 2;
                            Log.i(TAG, "??????SIM?????????-----------------");
                            btHandler.sendMessage(msg);
                        }
                    }).start();
                } else {
                    List<Person> list = contactsHelper.loadAllSimContacts();
                    Log.w(TAG, "??????SI------??????" + list.size());
                    if (list.size() <= 0 && !is_Read) {
                        // waits();
                        Log.w(TAG, "??????SIM??????----------??? " + list.size());
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                //???SIM??????????????????
                                read_Sim();
                                Message msg = btHandler.obtainMessage();
                                msg.what = 2;
                                Log.i(TAG, "??????SI---------------------");
                                btHandler.sendMessage(msg);
                            }
                        }).start();
                        Log.i(TAG, "??????SI---------------???????????????---");
                    }
                    String phone =getPhoneNumber();
                    if("".equals(phone) || phone== null){
//                        send_message(tm);
                    }
                }
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                break;
            default:
                break;
        }
    }

    public void is_noready(TelephonyManager tm,int state,Context context){
        switch (state) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                Log.i(TAG, "??????-------------");
                exit(context);
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                Log.i(TAG, "???SIM???-------------");
                simState = SIM_INVALID;
                Log.i(TAG, "simState????????????1----------??? " + simState);
                List<Person> contacts = contactsHelper.loadAllSimContacts();
                if (contacts.size() > 0) {
                    Log.i(TAG, "simState????????????1----------??? ?????????????????????");
                    for (int i = 0; i < contacts.size(); i++) {
                        contactsHelper.deleteContacts(contacts.get(i).getId());
                    }
                    Log.i(TAG, "simState????????????1----------??? ????????????");
                }
                List<Phone> phoneList = phoneHelper.loadPhones();
                if (phoneList.size() > 0) {
                    Log.i(TAG, "simState????????????1----------??? ????????????????????????");
                    for (int i = 0; i < phoneList.size(); i++) {
                        phoneHelper.delete(phoneList.get(i).getId());
                    }
                    Log.i(TAG, "simState????????????1----------??? ????????????");
                }
                phoneHelper.addPhones("");
                simHelper.addSim("");
                getPhoneNumber();
                PropertyUtils.set("com.tcl.android.phone", "");
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                break;
            default:
                break;
        }
    }


    /**
     * ???SIM??????????????????
     */
    public void read_Sim() {
        long startTime1 = System.currentTimeMillis();//??????????????????
        Log.i(TAG, "?????????SIM?????????-----------------------------3");
        //????????????SIM????????????url
        String str = "content://icc/adn";
        Intent intent = new Intent();
        intent.setData(Uri.parse(str));
        Uri uri = intent.getData();
        //??????SIM??????????????????
        try {
            mCursor = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.i(TAG, "SIM???????????????-------");
            e.printStackTrace();
        }
        long endTime1 = System.currentTimeMillis();    //??????????????????
        Log.w(TAG, "???SIM????????????????????? " + (endTime1 - startTime1) + "ms");
        Log.w(TAG, "-----------" + mCursor.getCount());
        //SIM??????????????????
        if (mCursor == null) {
            Log.i(TAG, "SIM???????????????");
            is_Read = true;
            return;
        }
        //SIM??????????????????
        if (mCursor != null) {
            Log.i(TAG, "SIM???????????????--------------");
            while (mCursor.moveToNext()) {
                // ?????????????????????
                int nameFieldColumnIndex = mCursor.getColumnIndex("name");
                // ??????????????????
                int numberFieldColumnIndex = mCursor
                        .getColumnIndex("number");

                contactsHelper.addContacts(mCursor.getString(nameFieldColumnIndex), mCursor.getString(numberFieldColumnIndex), 1, 2);
            }
            is_Read = true;
        }
        //???????????????????????????
        mCursor.close();
    }

    /**
     * ???????????????????????????
     */
    public void send_message(TelephonyManager tm) {
        String operator = tm.getSimOperator();
        SmsManager sms = SmsManager.getDefault();
        if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")){
            //??????????????????????????????
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10086";
                String message = "Bj";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }
        } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010")) {//????????????
            //??????????????????
        }  else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//????????????
            Log.i(TAG, "?????????????????????--------------");
            //??????????????????????????????
            Log.i(TAG, "getPhoneNumber--------------:"+getPhoneNumber());
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10001";
                String message = "701";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
                Log.i(TAG, "????????????--------------");
            }
        }
    }

    /**
     * ???????????????????????????????????????
     * @return
     */
    public String getPhoneNumber(){
        List<Phone> phoneList=phoneHelper.loadPhones();
        String phone="";
        if(phoneList.isEmpty()||phoneList.get(phoneList.size() - 1).getPhone().equals("")){
            phone="";
        }else {
            phone=phoneList.get(phoneList.size() - 1).getPhone();
            Log.i(TAG, "???????????????--------------???"+phone);
        }
        return phone;
    }





}
