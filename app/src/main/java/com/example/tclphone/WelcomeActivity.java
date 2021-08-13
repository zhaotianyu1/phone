package com.example.tclphone;

import android.app.ActivityManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Contacts;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.example.tclphone.broadcastreceiver.SimStateReceive;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.MyRecordsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.utils.L;
import com.example.tclphone.utils.PropertyUtils;

import com.example.tclphone.litepal.LitePal;
import com.example.tclphone.utils.RandomValueUtil;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 初始化欢迎界面
 */
public class WelcomeActivity extends BaseActivity {
    private static final String TAG = "PZR_WelcomeActivity";
    private Timer mTimer;
    private static Context mContext;

    //输入地址
    private static final String INPUT_FILE_PATH = "src/main/assets/litepal.xml";

    PhoneHelper phoneHelper = new PhoneHelper();
    SimStateReceive simStateReceive;
    //联系人的数据库操作类

    MyContactsHelper contactsHelper = new MyContactsHelper();
    RandomValueUtil randomValueUtil = new RandomValueUtil();
    MyRecordsHelper myRecordsHelper = new MyRecordsHelper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        mContext =this;

        if(!isTaskRoot())
        {
            Log.i(TAG,"isTaskRoot() : ");
            finish();
            return;

        }
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_welcome);

        LitePal.getDatabase();//若数据库没有创建，该语句将创建数据库

        MyContactsHelper myContactsHelper = new MyContactsHelper();

//        //初始化本机手机号码
//        init_books();

        init();

        if (simStateReceive == null) {
            simStateReceive = new SimStateReceive();
        }
        Log.w(TAG, "----------------查看系统默认短信1---------------------------------- ");
        try {
            componentName();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
       // isDefaultSmsApp();
        Log.w(TAG, "----------------修改默认短信名---------------------------------- ");
        convert_network();
        Log.w(TAG, "----------------查看系统默认短信2---------------------------------- ");
        isDefaultSmsApp();
       // SimDelete();
//        try {
//            componentName();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
////        }
//        for (int i = 0; i < 100; i++) {
//               String phone = randomValueUtil.getTelephone();
//             //int sex = randomValueUtil.getSex();
//            myRecordsHelper.communicate("你是什么都可以可以可以",
//                    "2020-09-10 13:02:33", "2020-09-10 13:22:33", 1, 0);
//              myRecordsHelper.communicate("你是什么都可以可以可以",
//                 "2020-09-10 13:02:33", "2020-09-10 13:22:33", 2, 1);
//            myRecordsHelper.communicate("你是什么都可以可以可以",
//                    "2020-09-10 13:02:33", "2020-09-10 13:22:33", 2, 0);
//            // myContactsHelper.addContacts("随意","123213123",0,0);
//        }

//        try {
//            componentName();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//        convert_network();
//        try {
//            componentName();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
    }



    public static boolean isClsRunning(String pkg, String cls, Context context) {

        ActivityManager am =(ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);

        ActivityManager.RunningTaskInfo task = tasks.get(0);

        if (task != null) {

            return TextUtils.equals(task.topActivity.getPackageName(), pkg) && TextUtils.equals(task.topActivity.getClassName(), cls);

        }

        return false;

    }
    @Override
    protected void onResume() {
        super.onResume();
    }


    public static Context getContext() {

        return mContext;

    }
    //初始化数据
    private void initData() {

        LitePal.getDatabase().beginTransaction();
        try {
//            RandomValueUtil randomValueUtil = new RandomValueUtil();
//            MyRecordsHelper myRecordsHelper = new MyRecordsHelper();

            LitePal.getDatabase().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            LitePal.getDatabase().endTransaction();
        }



    }
//    public void SimDelete() {
//        Log.i(TAG, "准备删除");
//        Uri uri = Uri.parse("content://icc/adn");
//        Cursor cursor = this.getContentResolver().query(uri, null, null,
//                null, null);
//        Log.i(TAG, ">>>>>> " + cursor.getCount());
//        String name = "   ";
////        while (cursor.moveToNext()){
//            String where = "tag='" + name+ "'";
//            where += " AND number='" +  "13403314525" + "'";
//            int i =this.getContentResolver().delete(uri, where, null);
////        }
//        Log.i(TAG, "删除--->>>>>> " + i);
//
//    }
    private void init() {

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                toMain();
            }
        }, 0);
    }

    private void toMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

//    /**
//     * 初始化本地手机号码
//     */
//    public void init_books() {
//        TelephonyManager manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        PropertyUtils.set("com.tcl.android.phone", getPhoneNumber());
//        //如果没有SIM卡的情况下，清空数据库中SIM卡的联系人
//        if (manager.getSimState() != TelephonyManager.SIM_STATE_READY) {
//            List<Person> contacts = contactsHelper.loadAllSimContacts();
//            if (contacts.size() > 0) {
//                Log.w(TAG, "contacts：开始删除 " + contacts.size());
//                for (int i = 0; i < contacts.size(); i++) {
//                    contactsHelper.deleteContacts(contacts.get(i).getId());
//                }
//            }
//        }
//        String iccid = manager.getSimSerialNumber();
//        Log.w(TAG, "iccid： " + iccid);
//    }
    public String getPhoneNumber(){
        List<Phone> phoneList=phoneHelper.loadPhones();
        String phone="";
        if(phoneList.isEmpty()||phoneList.get(phoneList.size() - 1).getPhone().equals("")){
            phone="";
            Log.i(TAG, "local_phone--------------------未读取到:  ");
        }else {
            phone=phoneList.get(phoneList.size() - 1).getPhone();
            Log.i(TAG, "local_phone--------------------读取到:  ");
        }
        return phone;
    }

    public boolean isDefaultSmsApp() {

        final String configuredApplication = Telephony.Sms.getDefaultSmsPackage(mContext);
        Log.i("zty"," configuredApplication:"+ configuredApplication);
//            return true;
        return  mContext.getPackageName().equals(configuredApplication);
    }

    public void componentName() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?> c = null;
        c = Class.forName("com.android.internal.telephony.SmsApplication");
        if(c ==null){
            Log.i("zty","componentName是空----------:");
        }
        Method set = c.getDeclaredMethod("getDefaultSmsApplication", Context.class, boolean.class);

        Log.i("zty","componentName111----------:");
        set.setAccessible(true);

        Log.i("zty","componentName222----------:");

        ComponentName componentName = (ComponentName) set.invoke(null,getContext(), true);

        Log.i("zty", "getContext--------------------读取到:  "+ getContext().toString());

        if(componentName != null){
            Log.i("zty", "getPackageName--------------------读取到:  "+componentName.getPackageName());
        }


        Log.i("zty","读取完成--------------------------------:");

    }
    public void convert_network(){

        Class<?> c = null;
        try {
            c = Class.forName("com.android.internal.telephony.SmsApplication");
            if(c == null){
                Log.i("zty","c是空----------:");
                return ;
            }
            Log.i("zty","c----------:"+c);
            Log.i("zty","c不是空----------:");
            Method set = c.getDeclaredMethod("setDefaultApplication", String.class, Context.class);
            Log.i("zty","111111----------:");

            String str = this.getPackageName();

            Log.i("zty","str "+str);
            set.invoke(null, str, getContext());
            Log.i("zty","完成修改----------:");
//            String DefaultApplication = Telephony.Sms.getDefaultSmsPackage(this);
//            Log.i("zty","DefaultApplication----------:"+DefaultApplication);

        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }


}