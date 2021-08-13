package com.example.tclphone.broadcastreceiver;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.view.WindowManager;
import android.app.AlertDialog;

import androidx.core.app.ActivityCompat;

import com.example.tclphone.MainActivity;
import com.example.tclphone.MyApplication;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.db.SIM;
import com.example.tclphone.db.SimHelper;
import com.example.tclphone.utils.NativeMethodList;
import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.rcs.call.sdk.RcsCallDefines;
import com.juphoon.rcs.call.sdk.RcsCallSession;
import com.juphoon.rcs.call.sdk.manager.RcsCallManager;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.tosapi.atv.TvFunctionApi;
import com.tcl.tvmanager.TTvCommonManager;
import com.tcl.tvmanager.TvManager;
import com.tcl.tvmanager.vo.EnTCLInputSource;
import com.tcl.uicompat.TCLToast;


import java.util.ArrayList;
import java.util.List;


/**
 * SIM卡拔插监听
 */
public class SimStateReceive extends BroadcastReceiver {

    private final String TAG = "PZR_SimStateReceive";
    public final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
    public final static String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

//    public static final String  SOURCE_CHANGE= "com.tcl.tv.action.changesource";

    private final static int SIM_VALID = 0;
    private final static int SIM_INVALID = 1;
    private int simState = SIM_INVALID;
    private static Integer count =0 ;

    private static Integer is_end = 0;

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
                case 1:
                    handler.removeCallbacks(runnable);
                    Log.i(TAG, "handler广播---------------结束---");
                    break;
                case 2:
                    Log.i(TAG, "读取SI---------------结束---");
                   // MyApplication.getInstance().setRead(true);
                    break;
            }
        }
    };
    private static Handler handler =new Handler();
    Runnable runnable=new Runnable() {
        @Override
        public void run() {

            TelephonyManager tm = (TelephonyManager) MyApplication.getContext().getSystemService(Service.TELEPHONY_SERVICE);
            int state=tm.getSimState();
            getPhoneNumber();
            sim_state_message(tm,state,MyApplication.getContext());
            count = 1;
            Log.i(TAG, "read end-----------： ");
            if(is_Read){
                handler.removeCallbacks(runnable);
                is_Read = false;
                Message message = new Message();
                message.what = 1;
                btHandler.sendMessage(message);
            }
            handler.postDelayed(runnable,4000);
        }
    };

    private List<Activity> list = new ArrayList<Activity>();
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.i(TAG, "simState监听-----------： ");
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        int state=tm.getSimState();
        Log.i(TAG, "simState监听-----------state： " +state);
        if(intent.getAction().equals(ACTION_SHUTDOWN)){
            Log.i(TAG, "ACTION_SHUTDOWN-----------state：关机 ");
        }

//        if(intent.getAction().equals(SOURCE_CHANGE)){
//            Log.i(TAG,"信源切换");
//            termCalling(context);
//        }


        if (intent.getAction().equals(ACTION_SIM_STATE_CHANGED)) {
            //若当前SIM卡不正常
            if(state != TelephonyManager.SIM_STATE_READY)
            {
                //SIM卡异常标识
                MyApplication.getInstance().setReady(false);
                termCallingAndClose(context);
                is_noready(tm,state,context);
                Log.i(TAG, "！SIM_STATE_READY-----------： ");

            }else {

                if(!MyApplication.getInstance().getIsNetworkUp())
                {
                    Log.i(TAG,"sim卡准备好，发起TrildCheck");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            NativeMethodList.TrildCheck();
                        }
                    }).start();
                }else {
                    Log.i(TAG,"sim卡没准备好，不发起TrildCheck");
                }
                    if(count%2==0){
                        handler.post(runnable); //发送消息，启动线程监听SIM卡运行
                        is_end = 1;
                    }else {
                        count = 0;
                    }
            }
        }
    }

    //切换信源，退出通话和应用
    public void termCallingAndClose(Context context){
        ArrayList<RcsCallSession> RcsCallSessionLists = RcsCallManager.getInstance().getListCallSessions();
        int size = RcsCallSessionLists.size();
        Log.i(TAG,"tonghuageshu : "+size);
        if(size!=0){
            for(RcsCallSession rcsList : RcsCallSessionLists)
            {
                Log.i(TAG,"断开通话");
                rcsList.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
            }
        }
        Log.i(TAG,"销毁Activity");
        MyApplication.getInstance().removeAllActivity();
        if(RcsLoginManager.isLogined()){
            Log.i(TAG,"调用close函数开始----------");
            RcsLoginManager.close();
            Log.i(TAG,"调用close函数结束----------");
        }
    }

    //如果电视sim卡异常，退出通话
    public void termCalling(Context context){
        ArrayList<RcsCallSession> RcsCallSessionLists = RcsCallManager.getInstance().getListCallSessions();
        int size = RcsCallSessionLists.size();
        Log.i(TAG,"tonghuageshu : "+size);
        if(size!=0){
            for(RcsCallSession rcsList : RcsCallSessionLists)
            {
                Log.i(TAG,"断开通话");
                rcsList.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
            }
        }
        Log.i(TAG,"销毁Activity");
        MyApplication.getInstance().removeAllActivity();
    }

    public void exit(Context context) {
//        for (Activity activity : list) {
//            activity.finish();
//            Log.i(TAG, "activity end-----------： ");
//        }
        System.exit(0);
    }

    /**
     * 对SIM卡的信息操作
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
                Log.w(TAG, "old_simiccid： " + old_simiccid);
                //获取SIM卡的身份证
                String iccid = "N/A";
                iccid = tm.getSimSerialNumber();
                Log.w(TAG, "iccid： " + iccid);
                if (iccid != null) {
                    simHelper.addSim(iccid);
                    is_Read =true;
                    Log.w(TAG, "关闭广播---------:"+is_Read);
                }

                //判断SIM卡是否切换，若切换清空数据库中过去SIM卡的联系人
                if (!old_simiccid.equals(iccid) && iccid !=null) {

                    List<Person> contacts = contactsHelper.loadAllSimContacts();
                    if (contacts.size() > 0) {
                        Log.w(TAG, "contacts：开始删除 " + contacts.size());
                        for (int i = 0; i < contacts.size(); i++) {
                            contactsHelper.deleteContacts(contacts.get(i).getId());
                        }
                    }
                    List<Phone> phoneList = phoneHelper.loadPhones();
                    if (phoneList.size() > 0) {
                        Log.i(TAG, "simState状态异常1----------： 开始删除本机号码");
                        for (int i = 0; i < phoneList.size(); i++) {
                            phoneHelper.delete(phoneList.get(i).getId());
                        }
                        Log.i(TAG, "simState状态异常1----------： 删除成功");
                    }
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    String local = tm.getLine1Number();
                    if(local!=null) {
                        phoneHelper.addPhones(local);
                    }
                    //send_message(tm);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            read_Sim();
                            Message msg = btHandler.obtainMessage();
                            msg.what = 2;
                            Log.i(TAG, "读取SIM卡完成-----------------");
                            btHandler.sendMessage(msg);
                        }
                    }).start();
                } else {
                    if(iccid != null) {
                        List<Person> list = contactsHelper.loadAllSimContacts();
                        Log.w(TAG, "读取SI------有卡" + list.size());
//                        new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                read_Sims();
//                            }
//                        }).start();
                        if (list.size() <= 0) {
                            // waits();
                            Log.w(TAG, "读取SIM卡中----------： " + list.size());
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //读SIM卡，耗时操作
                                    read_Sim();
                                    Message msg = btHandler.obtainMessage();
                                    msg.what = 2;
                                    Log.i(TAG, "读取SI---------------------");
                                    btHandler.sendMessage(msg);
                                }
                            }).start();
                            Log.i(TAG, "读取SI---------------主线程继续---");
                        }
//                        String phone = getPhoneNumber();
//                        if ("".equals(phone) || phone == null) {
//                            //send_message(tm);
//                        }
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
                Log.i(TAG, "SIM_STATE_UNKNOWN");
//                exit(context);
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                if(is_end != 0) {
                    Log.i(TAG, "无SIM卡-------------");
                    simState = SIM_INVALID;
                    Log.i(TAG, "simState状态异常1----------： " + simState);
                    List<Person> contacts = contactsHelper.loadAllSimContacts();
                    if (contacts.size() > 0) {
                        Log.i(TAG, "simState状态异常1----------： 开始删除联系人");
                        for (int i = 0; i < contacts.size(); i++) {
                            contactsHelper.deleteContacts(contacts.get(i).getId());
                        }
                        Log.i(TAG, "simState状态异常1----------： 删除成功");
                        MyApplication.getInstance().setRead(true);
                    }
                    List<Phone> phoneList = phoneHelper.loadPhones();
                    if (phoneList.size() > 0) {
                        Log.i(TAG, "simState状态异常1----------： 开始删除本机号码");
                        for (int i = 0; i < phoneList.size(); i++) {
                            phoneHelper.delete(phoneList.get(i).getId());
                        }
                        Log.i(TAG, "simState状态异常1----------： 删除成功");
                    }
                    // phoneHelper.addPhones("");
                    //simHelper.addSim("");
                    getPhoneNumber();
                    PropertyUtils.set("com.tcl.android.phone", "");
                    TCLToast.makeText(context,"SIM卡未插入",TCLToast.LENGTH_SHORT).show();
//                    exit(context);
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


    /**
     * 读SIM卡中的联系人
     */
    public void read_Sim() {
        long startTime1 = System.currentTimeMillis();//获取开始时间
        Log.i(TAG, "read_Sim开始读SIM卡数据-----------------------------");
        //系统获取SIM卡存储的url
        String str = "content://icc/adn";
        Intent intent = new Intent();
        intent.setData(Uri.parse(str));
        Uri uri = intent.getData();
        //搜索SIM卡中的联系人
        try {
            mCursor = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.i(TAG, "SIM卡读取失败-------");
            e.printStackTrace();
        }
        long endTime1 = System.currentTimeMillis();    //获取结束时间
        Log.w(TAG, "读SIM卡的运行时间： " + (endTime1 - startTime1) + "ms");

        if(mCursor.getCount() == 0){
            MyApplication.getInstance().setCursor(true);
        }
        //SIM卡中无联系人
        if (mCursor == null) {
            Log.i(TAG, "SIM卡读取失败");
            return;
        }
        //SIM卡中有联系人
        if (mCursor != null) {
            Log.i(TAG, "SIM卡读取成功--------------"+mCursor.getCount());
            while (mCursor.moveToNext()) {
                // 取得联系人名字
                int nameFieldColumnIndex = mCursor.getColumnIndex("name");
                // 取得电话号码
                int numberFieldColumnIndex = mCursor
                        .getColumnIndex("number");
                if(mCursor.getString(nameFieldColumnIndex).equals(mCursor.getString(numberFieldColumnIndex))){
                    contactsHelper.addContacts("", mCursor.getString(numberFieldColumnIndex), 1, 2);
                }else {
                    contactsHelper.addContacts(mCursor.getString(nameFieldColumnIndex), mCursor.getString(numberFieldColumnIndex), 1, 2);
                }

            }
            MyApplication.getInstance().setRead(true);
        }

        //关闭游标，释放资源
        mCursor.close();
    }

    /**
     * 发送短信获取手机号
     */
    public void send_message(TelephonyManager tm) {
        String operator = tm.getSimOperator();
        SmsManager sms = SmsManager.getDefault();
        if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")){
            //发送短信获取本机号码
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10086";
                String message = "Bj";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
            }
        } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010")) {//中国联通
            //获取本机号码
        }  else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//中国电信
            Log.i(TAG, "电信卡发送短信--------------");
            //发送短信获取本机号码
            Log.i(TAG, "getPhoneNumber--------------:"+getPhoneNumber());
            if (getPhoneNumber().equals("")) {
                String phoneNumber = "10001";
                String message = "701";
                sms.sendTextMessage(phoneNumber, null, message, null, null);
                Log.i(TAG, "发送成功--------------");
            }
        }
    }

    /**
     * 获取当前手机号（从数据库）
     * @return
     */
    public String getPhoneNumber(){
        List<Phone> phoneList=phoneHelper.loadPhones();
        String phone="";
        if(phoneList.isEmpty()||phoneList.get(phoneList.size() - 1).getPhone().equals("")){
            phone="";
        }else {
            phone=phoneList.get(phoneList.size() - 1).getPhone();
            Log.i(TAG, "当前手机号--------------："+phone);
        }
        return phone;
    }


    public void read_Sims() {
        long startTime1 = System.currentTimeMillis();//获取开始时间
        Log.i(TAG, "read_Sims开始读SIM卡数据-----------------------------3");
        //系统获取SIM卡存储的url
        String str = "content://icc/adn";
        Intent intent = new Intent();
        intent.setData(Uri.parse(str));
        Uri uri = intent.getData();
        //搜索SIM卡中的联系人
        try {
            mCursor = MyApplication.getContext().getContentResolver().query(uri, null, null, null, null);
        } catch (Exception e) {
            Log.i(TAG, "read_SimsSIM卡读取失败-------");
            e.printStackTrace();
        }
        long endTime1 = System.currentTimeMillis();    //获取结束时间
        Log.w(TAG, "read_Sims读SIM卡的运行时间： " + (endTime1 - startTime1) + "ms");
        Log.w(TAG, "read_Sims-mCursor.size-----------" + mCursor.getCount());
        //SIM卡中无联系人
        if (mCursor == null) {
            Log.i(TAG, "read_SimsSIM卡读取失败");

            return;
        }
        //SIM卡中有联系人
        if (mCursor != null) {
            Log.i(TAG, "read_SimsSIM卡自动读取成功--------------");
        }

        //关闭游标，释放资源
        mCursor.close();
    }

}
