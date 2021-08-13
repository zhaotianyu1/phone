package com.example.tclphone;

import android.Manifest;
import android.annotation.SuppressLint;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SmsManager;

import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.broadcastreceiver.SimStateReceive;
import com.example.tclphone.callrecord.MyAdapter;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.MyRecordsHelper;
import com.example.tclphone.db.Person;

import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.db.Records;
import com.example.tclphone.phonebook.AddContactActivity;
import com.example.tclphone.phonebook.ContactsActivity;
import com.example.tclphone.phonebook.EditContactActivity;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.phonebook.util.SimpleItemDecoration;
import com.example.tclphone.utils.ContactDialog;
import com.example.tclphone.utils.L;

import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.rcs.call.module.JusCallDelegate;

import com.juphoon.rcs.login.sdk.listener.RcsLoginListener;

import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static android.Manifest.permission.RECORD_AUDIO;
import static android.os.Debug.isDebuggerConnected;
import static com.juphoon.rcs.login.module.JusLoginDelegate.logout;


public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, DialogInterface.OnCancelListener, RcsLoginListener {

    private static final String TAG = "ZTY_MainActivity";
    SimStateReceive simStateReceive;
    String key = "tcl.tv5g";//加密唯一标识c

    //PZR的控件
    //电话号码输入框
    private EditText editText;
    //语言通话按钮
    private AllCellsGlowLayout btnAudioCall;
    //视频通话按钮
    private AllCellsGlowLayout btnVideoCall;

    private TextView signal_data;
    private TextView signal;
    TextView sign_id;

    TCLNavigationItem call_record;
    LinearLayout tags;
    TCLNavigationItem call_contracts;
    private ImageView iv_sign;
    PhoneHelper phoneHelper = new PhoneHelper();
    private FrameLayout main_act;
    private MyAdapter recordAdapter;//通话记录
    private RecyclerView recyclerView_record;//通话记录
    private List<Records> mData_records = new ArrayList<>(); //通话记录
    private ImageView mask;
    private LinearLayout record_boolean;

    private ProgressDialog mLoginProgressDialog;
    private ProgressDialog mLicenseProgressDialog;

    MyRecordsHelper myRecordsHelper = new MyRecordsHelper();
    private int isReturn = 0;//判断是否回退
    TelephonyManager Tel;

    MyPhoneStateListener MyListener;
    private int count = 0;
    //发送短信操作
    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";


    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            String operator = Tel.getSimOperator();
            L.i(TAG, "网络为： " + operator);
            if (!operator.isEmpty()) {
                //中国移动
                if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")){
                    sign_id.setText("中国移动");
                } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010")) {//中国联通{//中国联通
                    sign_id.setText("中国联通");
                    List<Phone> phoneList = phoneHelper.loadPhones();
                    if (phoneList.get(phoneList.size() - 1).getPhone().equals("")) {
                        local_phonenumber();
                    }
                } else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//中国电信
                    sign_id.setText("中国电信");
                }
            } else {
                sign_id.setText("");
                phoneHelper.addPhones("");
                count = 0;
            }
            //监听网络状态
            internet();
            if (mData_records.isEmpty()){
                recyclerView_record.setFocusable(false);
            }else{
                recyclerView_record.setFocusable(true);
            }
            // 每5秒执行一次
            handler.postDelayed(runnable, 4000);
        }
    };

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        super.onResume();
        if (recordAdapter != null) {//如果这个adapter中不为null
            mData_records.clear();
            mData_records.addAll((List<Records>) myRecordsHelper.loadRecords().get("lists"));
            if (mData_records.isEmpty()) {
                record_boolean.setVisibility(View.VISIBLE);
            } else {
                record_boolean.setVisibility(View.GONE);
            }
            recordAdapter.notifyDataSetChanged();//刷新这个adapter
            call_record.requestFocus();
            call_record.setNeedBreath(true);
            editText.setText("");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);

        //判断sdk的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            //判断是否拥有权限
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                //需要一系列的权限
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.READ_EXTERNAL_STORAGE, RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_CONTACTS
                                , Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CHANGE_NETWORK_STATE,
                                Manifest.permission.MODIFY_PHONE_STATE, Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.SEND_SMS,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,},
                        100);
            } else {
                try {
                    initViews();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                initViews();
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        //监听SIM卡手机号
        MyListener = new MyPhoneStateListener();
        handler.post(runnable); //发送消息，启动线程监听运行
        //初始化图标
        iv_sign = findViewById(R.id.Iv_sign);
        signal = findViewById(R.id.signal);
        signal_data = findViewById(R.id.signal_data);
        sign_id = findViewById(R.id.sign_id);

        if (simStateReceive == null) {
            simStateReceive = new SimStateReceive();
        }
        L.i(TAG, "isRoot isRoot!---"+isRoot());
        if(isRoot()){
            TCLToast.makeText(getBaseContext(), "本机已获取Root权限", TCLToast.LENGTH_SHORT).show();
        }

        boolean is_debug = isDebuggerConnected();
        L.i(TAG, "is_debug is_debug!---"+is_debug);

    }

    /**
     * 获取本机号码方法
     *
     * @return
     */
    public void local_phonenumber() {
        //本机电话号码初始化
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
        String phoneNumber = tms.getLine1Number();
        phoneHelper.addPhones(phoneNumber);
        // local_phone.setText(phoneNumber);
    }

//    //初始化，包括初始化sdk和初始化视图
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void init() throws SocketException {
////        int initSDKResult = RscInitHelper.getInstance().initSDK(getApplicationContext(),getBaseContext());
////        if (initSDKResult != RESULT_SUCCESS) {
////            String resultTitle = titleWithSDKResult(initSDKResult);
////            showAlertDialog("", resultTitle);
////            return;
////        }
////        else {
////            MainCallDBManager.getInstance().init(getBaseContext());
////            AutoLogin.getInstance().autoLogin();
//        initViews();
////            MainCallDBManager.getInstance().setRtpPacket();//设置音视频加解密
////        }
//    }


    //初始化视图
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    private void initViews() throws SocketException {

        L.i("PZR", "state3");
        L.i("PZR", "Login success!");
        setContentView(R.layout.activity_main);

        call_record = findViewById(R.id.record_button);
        call_record.requestFocus();
        call_record.setTextContent("5G通话");
        call_record.setMiddleFocusStatus(true);
        call_record.setNeedBreath(true);

        //对联系人控件进行监听



        call_contracts = findViewById(R.id.contacts__button);
        call_contracts.setFocusable(false);
        call_contracts.setTextContent("联系人");
        call_contracts.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //跳转到联系人主界面
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(MainActivity.this, ContactsActivity.class);
                    Log.i(TAG, "onSignalStrengthsChanged: 跳转到联系人主界面，并清空栈顶界面");
                    startActivity(intent);

                }
            }
        });
        //通话记录 yjn
        String recordTag = "PZR_Record";
        recyclerView_record = findViewById(R.id.recycler_record);
       // mask = findViewById(R.id.record_mask);
        record_boolean = findViewById(R.id.record_boolean);


        //设置RecyclerView布局管理器
        LinearLayoutManager linearLayoutManager_record = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView_record.setLayoutManager(linearLayoutManager_record);


        //PZR 获取拨号部分的几个主控件
        editText = findViewById(R.id.phone_num_text);

        btnAudioCall = findViewById(R.id.audioCall);

        btnVideoCall = findViewById(R.id.videoCall);
        main_act = findViewById(R.id.main_act);

        int firstItemPosition = linearLayoutManager_record.findFirstVisibleItemPosition();
//        if (firstItemPosition > 0) {
//            mask.setVisibility(View.VISIBLE);
//        } else {
//            mask.setVisibility(View.GONE);
//        }

        try {
            load_record();
            recordAdapter = new MyAdapter(this, mData_records, recyclerView_record);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        recyclerView_record.setItemViewCacheSize(20);
        recyclerView_record.setDrawingCacheEnabled(true);
        recyclerView_record.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        L.i(recordTag, "onCreate: myAdapter " + recordAdapter);

        recyclerView_record.setHasFixedSize(true);
        recyclerView_record.setAdapter(recordAdapter);
        recyclerView_record.setItemAnimator(null);

        L.i(recordTag, "onCreate: mData " + mData_records.isEmpty());
        call_record.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_RIGHT){
                    call_contracts.setFocusable(true);
                }else{
                    call_contracts.setFocusable(false);
                    isReturn = 0;
                }
                if(code == KeyEvent.KEYCODE_BACK){
                    Log.i(TAG, "isReturn1-------:"+isReturn);
                    Log.i(TAG, "联系人回退-------");
                    if(isReturn ==1){
                        Log.i(TAG, "开始finish-------");
                        finish();
                    }else{
                        Log.i(TAG, "开始isReturn+1-------");
                        isReturn = 1;
                    }
                }
                return false;
            }
        });

        if (mData_records.isEmpty()) {
            record_boolean.setVisibility(View.VISIBLE);
        } else {
            record_boolean.setVisibility(View.GONE);
        }


//        //设置分分割线
//        recyclerView_record.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        recyclerView_record.addItemDecoration(new SimpleItemDecoration());

        MyAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                final boolean isVideo = false;
                final ContactDialog.Builder builder = new ContactDialog.Builder(MainActivity.this);
                builder.setPositiveButton("button", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String peerNumber = mData_records.get(position).getPhoneNumber();

                        if (peerNumber.isEmpty() || peerNumber.length() == 0) {
                            TCLToast.makeText(getBaseContext(), "拨打失败，请输入正确的电话号码", TCLToast.LENGTH_SHORT).show();
                        } else {
                            //视频通话
                            tclCall(peerNumber, true);
                        }

                    }
                });
                builder.setNegativeButton("button", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        String peerNumber = mData_records.get(position).getPhoneNumber();
                        if (peerNumber.isEmpty() || peerNumber.length() == 0) {
                            TCLToast.makeText(getBaseContext(), "拨打失败，请输入正确的电话号码", TCLToast.LENGTH_SHORT).show();
                        } else {
                            //语音通话
                            tclCall(peerNumber, false);
                        }
                    }
                });
                builder.create().show();
            }

            @Override
            public void onItemLongClick(View view, int position) {

                if (position == 0) {
                    call_record.requestFocus();
                    call_record.setNeedBreath(true);
                }

            }

            /**
             * 通话记录界面左键操作
             * @param keyCode
             * @param view
             * @param position
             */
            boolean flag = true;

            @Override
            public void onItemLeftClick(int keyCode, View view, int position) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && flag) {
                    Log.i("ZTY", "position success!--------：" + position);
                    //获取单条数据的信息
                    String phoneNumber = mData_records.get(position).getPhoneNumber();
                    MyRecordsHelper recordsHelper = new MyRecordsHelper();
                    Intent intent;
//                    从数据库向前端传送数据信息供操作
//                    if (recordsHelper.hasContacts(phoneNumber)) {
//                        intent = new Intent(MainActivity.this, EditContactActivity.class);
//                        Person c = recordsHelper.loadContactsByPhoneNumber(phoneNumber);
//                        intent.putExtra("type", 2);
//                        intent.putExtra("id", c.getId());
//                        intent.putExtra("name", c.getName());
//                        intent.putExtra("phone_number", c.getPhoneNumber());
//                        intent.putExtra("storage", c.getStorage());
//                        intent.putExtra("photo", c.getPhoto());
//                    } else {
//                        intent = new Intent(MainActivity.this, AddContactActivity.class);
//                        intent.putExtra("phoneNumber", phoneNumber);
//                        intent.putExtra("type", "0");
//                    }
//                    startActivity(intent);
//                    finish();
                    flag = false;
                }
                flag = true;

                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    flag = false;
                }
            }

            @Override
            public void onFocusLiterner(View view, boolean b, int position) {

            }

        });

        //输入框架加入输入监听器
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }


            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //只保留11位数字号码
                String str = editText.getText().toString();
                if (str.length() > 0) {
                    char[] strToChar = str.toCharArray();
                    if (strToChar[0] == '0') {
                        if (editText.getText().length() == 12) {
                            btnVideoCall.setFocusable(true);
                            btnVideoCall.requestFocus();
                        }
                    } else if (strToChar[0] == '1') {
                        if (editText.getText().length() == 11) {
                            btnVideoCall.setFocusable(true);
                            btnVideoCall.requestFocus();
                        }
                    }
                }
                if(str.length()>20){
                    TCLToast.makeText(getBaseContext(),"最多支持20位号码",TCLToast.LENGTH_SHORT).show();
                }


            }
        });


    }

    //数字键1的编辑框的onclick函数
    public void onNum1(View v) {
        Editable editable = editText.getText();
        editable.append("1");
    }

    public void onNum2(View v) {
        Editable editable = editText.getText();
        editable.append("2");
    }

    public void onNum3(View v) {
        Editable editable = editText.getText();
        editable.append("3");
    }

    public void onNum4(View v) {
        Editable editable = editText.getText();
        editable.append("4");
    }

    public void onNum5(View v) {
        Editable editable = editText.getText();
        editable.append("5");
    }

    public void onNum6(View v) {
        Editable editable = editText.getText();
        editable.append("6");
    }

    public void onNum7(View v) {
        Editable editable = editText.getText();
        editable.append("7");
    }

    public void onNum8(View v) {
        Editable editable = editText.getText();
        editable.append("8");
    }

    public void onNum9(View v) {
        Editable editable = editText.getText();
        editable.append("9");
    }

    public void onNum0(View v) {
        Editable editable = editText.getText();
        editable.append("0");
    }

    private void deleText() {
        if (editText.getText().length() == 0) {
            return;
        }
        int index = editText.getSelectionStart();
        Editable editable = editText.getText();
        editable.delete(index - 1, index);
    }

    public void onNumdele(View v) {
        deleText();
    }

    //清空
    public void onNumrubis(View v) {
        editText.setText("");
    }

    public void onAudioCall(View v) {
        String number = editText.getText().toString();
        if (number.isEmpty() || number.length() == 0) {
            TCLToast.makeText(getBaseContext(), "拨打失败，请输入正确的电话号码", TCLToast.LENGTH_SHORT).show();
        } else {
            //语音通话
            tclCall(number, false);
        }
    }

    public void onVideoCall(View v) {
        String number = editText.getText().toString();
        if (number.isEmpty() || number.length() == 0) {
            TCLToast.makeText(getBaseContext(), "拨打失败，请输入正确的电话号码", TCLToast.LENGTH_SHORT).show();
        } else {
            //视频通话
            tclCall(number, true);
        }
    }

    public void load_record() {
        MyRecordsHelper myRecordsHelper = new MyRecordsHelper();
        Map<String, Object> map = myRecordsHelper.loadRecords();
        List<Records> recordsList = (List<Records>) map.get("lists");
        mData_records.addAll(recordsList);
        if(mData_records.size()>=6) {
            mData_records.add(new Records(5001,"ztytyz","123",
                 "2020-09-10 13:02:33", "2020-09-10 13:22:33", 2, 1,0));
            L.i(TAG, "已存空白数据");
        }
    }


    //第一阶段不支持紧急通话


    //是否是紧急通话
    public boolean checkEmergencyCall(String number) {
        for (String s : RecordConstants.EMERGENCY_CALL) {
            if (s.equals(number)) {
                Log.i(TAG, number + "是紧急通话");
                return true;
            }
        }
        Log.i(TAG, number + "不是紧急通话");
        return false;
    }


    public void tclCall(String number, Boolean b) {
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Service.TELEPHONY_SERVICE);
        int state = tm.getSimState();
        if (RscInitHelper.getInstance().getIsInit() && RcsLoginManager.isLogined()) {
            Log.i(TAG, "RcsLoginManager.isLogined() : true");
            if (checkEmergencyCall(number)) {
                TCLToast.makeText(getBaseContext(), "当前版本不支持紧急通话", TCLToast.LENGTH_SHORT).show();
            } else {
                String peerNumber = "tel:" + number;
                Log.i(TAG, "number-----------: " + number);
                JusCallDelegate.call(peerNumber, b);
            }

        } else if (state != TelephonyManager.SIM_STATE_READY) {
            Log.i(TAG, "未插入SIM卡，无法发起呼叫");
            TCLToast.makeText(getBaseContext(), "未插入SIM卡，无法发起呼叫", TCLToast.LENGTH_SHORT).show();
        } else if (!RscInitHelper.getInstance().getIsInit()) {
            Log.i(TAG, "sdk加载失败");
            TCLToast.makeText(getBaseContext(), "加载sdk失败，无法发起呼叫", TCLToast.LENGTH_SHORT).show();
        } else if (!MyApplication.getInstance().isReady()) {
            Log.i(TAG, "登陆中/SIM卡异常");
            TCLToast.makeText(getBaseContext(), "登陆中/SIM卡异常", TCLToast.LENGTH_SHORT).show();
        } else  {
            Log.i(TAG, "登录失败");
            TCLToast.makeText(getBaseContext(), "登录失败，无法发起呼叫", TCLToast.LENGTH_SHORT).show();
        }

    }



    //监听权限获取的结果，当权限获取返回码为100时，执行初始化函数，否则弹框反馈授权失败
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        initViews();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                } else {
                    TCLToast.makeText(this, "授权失败,请检查权限", TCLToast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    /**
     * 返回键
     */
    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (dialog == mLicenseProgressDialog) {
            System.exit(0);
        } else if (dialog == mLoginProgressDialog) {
            logout();
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    boolean is_menu = false ;
    /**
     * 页面的按键监听
     *
     * @param keyCode
     * @param event
     * @return true:代表屏蔽当前的按键，false代表解除被屏蔽的按键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU ) {
//            Log.i(TAG, "-----开启设置界面-----------");
//            if(!is_menu) {
//                Intent OtherIntent = new Intent();
//                OtherIntent.setClassName("com.tcl.settings", "com.tcl.settings.ShowWindowService");
//                OtherIntent.putExtra("flag", 1);
//                OtherIntent.putExtra("Type", "Settings");
//                OtherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                OtherIntent.setAction("com.tcl.settings.SHOW_WINDOW");
//                startService(OtherIntent);
//                is_menu = true;
//                Log.i(TAG, "-----打开设置-----------");
//            }else{
//                finish();
//                is_menu = false;
//                Log.i(TAG, "-----关闭设置-----------");
//            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(isReturn == 0) {
                Intent intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }

            Log.i(TAG, "-----回退-----------");
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.i("PZR"+TAG, "onDestroy");
        //将线程销毁掉

        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    public void onBeforeRegister() {

    }

    @Override
    public void onRegisterStateChanged(int i, int i1, String s) {

    }

    /**
     * 监听SIM卡的状态
     */
    class MyPhoneStateListener extends PhoneStateListener {


        private static final String TAG = "ZTY_MyPhoneStateListener";
        //判断当前网络是否有wifi
        int disConnect;

        /**
         * 监听当前SIM卡信号强度
         *
         * @param signalStrength
         */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);


            L.i(TAG, " ---------------------------:" + signalStrength.getGsmSignalStrength());
            //获取基站的信号强度asu值
            if (signalStrength.getGsmSignalStrength() >= 30) {
                iv_sign.setBackgroundResource(R.drawable.ic_status_bar_signal4);
            } else if (signalStrength.getGsmSignalStrength() < 30 && signalStrength.getGsmSignalStrength() >= 20) {
                iv_sign.setBackgroundResource(R.drawable.ic_status_bar_signal3);
            } else if (signalStrength.getGsmSignalStrength() < 20 && signalStrength.getGsmSignalStrength() >= 10) {
                iv_sign.setBackgroundResource(R.drawable.ic_status_bar_signal2);
            } else if (signalStrength.getGsmSignalStrength() < 10) {
                iv_sign.setBackgroundResource(R.drawable.ic_status_bar_signal1);

            }


        }

        /**
         * 监听网络是否断开
         *
         * @param state
         */
        @Override
        public void onDataConnectionStateChanged(int state) {

            switch (state) {
                case TelephonyManager.DATA_DISCONNECTED://网络断开
                    signal.setBackgroundResource(0);
                    signal_data.setBackgroundResource(0);
                    disConnect = 0;
                    break;
                case TelephonyManager.DATA_CONNECTING://网络正在连接
                    break;
                case TelephonyManager.DATA_CONNECTED://网络连接上
                    disConnect = 2;
                    //判断当前网络是5G还是4G
                    TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    //获取网络类型
                    int networkType = telephonyManager.getNetworkType();
                    if (networkType == TelephonyManager.NETWORK_TYPE_NR) {//5G网络
                        Log.i(TAG, "5G");
                        signal.setBackgroundResource(R.drawable.ic_status_bar_5g);

                    } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {//4G网络
                        Log.i(TAG, "4G");
                        signal.setBackgroundResource(R.drawable.ic_status_bar_4g);
                    }
                    break;
            }

//                }

        }

        /**
         * 监听上下行网络
         *
         * @param direction
         */
        @Override
        public void onDataActivity(int direction) {

            //数据网络的状态
            switch (direction) {
                case TelephonyManager.DATA_ACTIVITY_NONE:
                    if (disConnect == 0) {
                        signal.setBackgroundResource(0);
                        signal_data.setBackgroundResource(0);
                    } else {
                        signal_data.setBackgroundResource(R.drawable.ic_status_bar_none);
                    }
                    break;
                case TelephonyManager.DATA_ACTIVITY_IN:
                    signal_data.setBackgroundResource(R.drawable.ic_status_bar_up);
                    break;
                case TelephonyManager.DATA_ACTIVITY_OUT:
                    signal_data.setBackgroundResource(R.drawable.ic_status_bar_down);
                    break;
                case TelephonyManager.DATA_ACTIVITY_INOUT:
                    signal_data.setBackgroundResource(R.drawable.ic_status_bar_all);
                    break;
                case TelephonyManager.DATA_ACTIVITY_DORMANT:
                    break;
            }
        }
//        }
    }

    private void internet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Service.TELEPHONY_SERVICE);
        int state=tm.getSimState();
        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            boolean is = isPluggedIn();
            Log.i(TAG, "--------is----"+is);
            if(is){
                Log.i(TAG, "--------仅有线网络可用----");
                iv_sign.setBackgroundResource(R.drawable.ic_network);
                signal_data.setBackgroundResource(0);
                signal.setBackgroundResource(0);
            }else if(Tel.getSimState() == TelephonyManager.SIM_STATE_READY){
                Log.i(TAG, "--------无数据网络可用----");
                Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            }else if(Tel.getSimState() != TelephonyManager.SIM_STATE_READY){
                Log.i(TAG, "-------无SIM卡---");
                iv_sign.setBackgroundResource(0);
                signal_data.setBackgroundResource(0);
                signal.setBackgroundResource(0);
            }
        } else if (wifiNetInfo.isConnected()) {
            Log.i(TAG, "-------仅Wifi网络可用---");
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
            checkWifiState();
        }else if(mobNetInfo.isConnected()) {
            L.i(TAG, "-----仅数据网络可用： ---------");
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                    PhoneStateListener.LISTEN_DATA_ACTIVITY |
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        }else {
            Log.i(TAG, "-------无SIM卡---");
            iv_sign.setBackgroundResource(0);
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
        }
        L.i(TAG, "-----internet：finish ---------");
    }


    /**
     * 检查无线信号强度
     */
    public void checkWifiState() {

        WifiManager mWifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int wifi = mWifiInfo.getRssi();//获取wifi信号强度
        if (wifi > -50 && wifi < 0) {//最强

            iv_sign.setBackgroundResource(R.drawable.ic_wifi_3);
        } else if (wifi > -70 && wifi < -50) {//较强

            iv_sign.setBackgroundResource(R.drawable.ic_wifi_2);
        } else if (wifi > -80 && wifi < -70) {//较弱

            iv_sign.setBackgroundResource(R.drawable.ic_wifi_1);
        } else if (wifi > -100 && wifi < -80) {//微弱
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_0);

        }

    }

    /**
     * 判断是否有网线插入
     *
     * @param command
     * @return
     */
    public String execCommand(String command) {
        Runtime runtime;
        Process proc = null;
        StringBuffer stringBuffer = null;
        try {
            runtime = Runtime.getRuntime();
            proc = runtime.exec(command);
            stringBuffer = new StringBuffer();
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    proc.getInputStream()));

            String line = null;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line + " ");
            }

        } catch (Exception e) {
            System.err.println(e);
        } finally {
            try {
                proc.destroy();
            } catch (Exception e2) {
            }
        }
        return stringBuffer.toString();
    }

    //判断移动数据是否打开
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     * 网线的返回
     *
     * @return
     */
    public boolean isPluggedIn() {
        Context context = getBaseContext();

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < networkInfo.length; i++) {
            if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
                Log.i(TAG, "有网络-------------");
                return true;
            }
        }

//        String state = execCommand("cat /sys/class/net/eth0/carrier");
//        if (state.trim().equals("1")) {  //有网线插入时返回1，拔出时返回0
//            Log.i(TAG, "有网线-------------");
//            return true;
//        }
//        Log.i(TAG, "无网线-------------");
        return false;
    }

    /**
     * 判断是否获取root权限
     * @return true为获取，false为没有获取
     */
    public boolean isRoot() {
        boolean root = false;
        try {
            if ((!new File("/system/bin/su").exists()) && (!new File("/system/xbin/su").exists()))
            {
                root = false;
            } else {
                root = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }
}

