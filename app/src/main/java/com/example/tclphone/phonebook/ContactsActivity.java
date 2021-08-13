package com.example.tclphone.phonebook;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import android.os.Bundle;
import android.os.Handler;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.MainActivity;
import com.example.tclphone.MyApplication;
import com.example.tclphone.R;
import com.example.tclphone.SimService;
import com.example.tclphone.broadcastreceiver.SimStateReceive;
import com.example.tclphone.db.Contacts;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.db.SIM;
import com.example.tclphone.db.SimHelper;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.phonebook.contactadapter.ContactAdaptere;
import com.example.tclphone.phonebook.contactadapter.SWRecyclerView;
import com.example.tclphone.phonebook.fragment.FirstFragment;
import com.example.tclphone.phonebook.util.CenterLayoutManager;
import com.example.tclphone.phonebook.util.SimpleItemDecoration;
import com.example.tclphone.utils.L;
import com.example.tclphone.utils.LogUtils;
import com.example.tclphone.utils.PropertyUtils;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLButton;
import com.tcl.uicompat.TCLEditText;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tcl.tosapi.listener.EventNotify.mContext;

/**
 * 联系人主界面Acitvity
 */
public class ContactsActivity extends FragmentActivity implements View.OnClickListener, Filterable {

    //打印日志标识
    private static final String TAG = "ContacsActivity";
    //联系人的数据库操作类
    MyContactsHelper contactsHelper = new MyContactsHelper();
    //联系人列表布局
    private SWRecyclerView recyclerView_contact;
    //联系人列表
    public List<Contact> mData_contacts = new ArrayList<Contact>();
    public List<Contact> alist = new ArrayList<>();
    //联系人列表适配器
    public ContactAdaptere myAdapter;
    private List<Contacts> contacts_search;
    //数据库中的字段
    private int id;
    private String name;
    private String phone_number;
    private int photo;
    private int storage;

    //头像
    private AllCellsGlowLayout editContact;
    //搜索框
    private TCLEditText etInput;
    //搜索过滤器
    private ArrayFilter mFilter;
    //联系人按钮
    private Button contacts__button;
    //语音按钮
    private AllCellsGlowLayout buttVideo;
    //单击某条联系人的位置
    private int positions;
    //列表遮罩
    private ImageView mask;

    TCLNavigationItem call_record;

    TCLNavigationItem call_contracts;
    //本机电话号码
    private TextView local_phone;

    List<Person> contacts;

    static Activity instance;

    //RecyclerView布局管理器
    private CenterLayoutManager linearLayoutManager;
    //FragmentManager布局管理器
    private FragmentManager fragmentManager;
    //判断fragment页面是否显示（=0代表不显示，>0代表显示）
    private int isFragment = 0;
    //是否在fragment界面按下键操作标识（1代表在fragment中按下键，2代表不在fragment中按下键）
    private int isdown;
    private Context context;
    private ImageView iv_sign;
    //声明fragment页面
    private Fragment fragment;

    private LinearLayout benji;
    //声明fragment事务处理
    private FragmentTransaction transaction;
    SimStateReceive simStateReceive;
    PhoneHelper phoneHelper = new PhoneHelper();
    TextView sign_id;
    private TextView signal_data;
    private TextView signal;
    int ss = 0;//全局判断返回
    TelephonyManager Tel;
    private int isReturn = 0;//判断是否回退
    TCLButton add1;
    TCLButton copy;
    private int count=0;
    SimHelper simHelper = new SimHelper();
    //发送短信操作
    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";

    MyPhoneStateListener MyListener;
//    public static ContactsActivity instance =null;

    private Handler handler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {

            Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String operator = Tel.getSimOperator();
//            L.i(TAG, "网络为： " + operator);
            if (!operator.isEmpty()) {
                //中国移动
                if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")){
                    sign_id.setText("中国移动");
                    List<Phone> phoneList=phoneHelper.loadPhones();
                    if(phoneList.size()>0) {
                        if (phoneList.get(phoneList.size() - 1).getPhone().equals("") || is_change()) {
                            local_phonenumber();
//                        while (count < 1) {
//                            Intent sentIntent = new Intent(SENT_SMS_ACTION);
//                            PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, sentIntent, 0);
//                            SmsManager sms = SmsManager.getDefault();
//                            String phoneNumber = "10086";
//                            String message = "Bj";
//                            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
//                            L.i(TAG, "中国移动---------： sendTextMessage");
//                            count+=1;
//                        }
                        } else {
                            getPhoneNumber();
                        }
                    }

                } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010"))  {//中国联通
                    sign_id.setText("中国联通");
                    List<Phone> phoneList=phoneHelper.loadPhones();
                    if(phoneList.get(phoneList.size() - 1).getPhone().equals("")){
                        local_phonenumber();
                        getPhoneNumber();
                    }
                }  else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//中国电信

                    sign_id.setText("中国电信");
                    List<Phone> phoneList=phoneHelper.loadPhones();
//                    L.i(TAG, "phoneList.size-----123----："+phoneList.size());
                    if(phoneList.size()>0) {
                        if (phoneList.get(phoneList.size() - 1).getPhone().equals("") || is_change()) {
                            local_phonenumber();
//                        while (count<1) {
//                            Intent sentIntent = new Intent(SENT_SMS_ACTION);
//                            PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, sentIntent, 0);
//                            L.i(TAG, "中国电信---------： sendTextMessage");
//                            SmsManager sms = SmsManager.getDefault();
//                            String phoneNumber = "10001";
//                            String message = "701";
//                            sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
//                            count+=1;
//                        }
                        } else {
                            getPhoneNumber();
                        }
                    }
                }
            }else{
                sign_id.setText("");
                phoneHelper.addPhones("");
                getPhoneNumber();
                count=0;

            }

            getPhoneNumber();
            //监听网络状态
            internet();
            // 实时更新SIM卡中的联系人信息
            try {
                if(MyApplication.getInstance().isRead()) {
                    updateTime();
                    MyApplication.getInstance().setRead(false);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 每3秒执行一次
            handler.postDelayed(runnable,4000);
        }
    };

    private void internet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!mobNetInfo.isConnected() && !wifiNetInfo.isConnected()) {
            boolean is = isPluggedIn();
            if(is){
         //       Log.i(TAG, "--------仅有线网络可用----");
                iv_sign.setBackgroundResource(R.drawable.ic_network);
                signal_data.setBackgroundResource(0);
                signal.setBackgroundResource(0);
            }else if(Tel.getSimState() == TelephonyManager.SIM_STATE_READY){
           //     Log.i(TAG, "--------无数据网络可用----");
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
          //  Log.i(TAG, "-------仅Wifi网络可用---");
            checkWifiState();
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
        }else if(mobNetInfo.isConnected()) {
        //    L.i(TAG, "-----仅数据网络可用： ---------");
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                    PhoneStateListener.LISTEN_DATA_ACTIVITY |
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        }else {
            Log.i(TAG, "-------无SIM卡---");
            iv_sign.setBackgroundResource(0);
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
        }
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * 实时更新SIM卡中的联系人信息
     * @throws IOException
     * @throws JSONException
     */
    private void updateTime() throws IOException, JSONException {
        L.i(TAG, "updateTime： ---------");
        L.i(TAG, "contacts。size： ---------"+mData_contacts.size());

        List<Person> personList= contactsHelper.loadContacts();
        L.i(TAG, "personList。size： ---------"+personList.size());
        List<Contact> contactList = new ArrayList<>(); //短信
        if(personList.size()+1>mData_contacts.size() || personList.size()+1<mData_contacts.size()){
            L.i(TAG, "updateTime： --------- start");
            for(int i=0;i<personList.size();i++){
                String name =personList.get(i).getName();
                String phoneNumber = personList.get(i).getPhoneNumber();
                int gender=personList.get(i).getPhoto();
                int id=personList.get(i).getId();
                int storage=personList.get(i).getStorage();
                contactList.add(new Contact(id,name,phoneNumber,storage,gender));
            }

            myAdapter = new ContactAdaptere(this, contactList,recyclerView_contact);
            recyclerView_contact.setAdapter(myAdapter);
            myAdapter.notifyDataSetChanged();
            mData_contacts = contactList;
            L.i(TAG, "updateTime： --------- end");
        }
    }

    //枚举类fragment页面是否显示（0代表不显示，1代表显示）
    public enum AuditState {
        UNVISIABLE(0),
        VISIABLE(1);
        private final int statenum;

        AuditState(int statenum) {
            this.statenum = statenum;
        }

        public int getStatenum() {
            return statenum;
        }

    }

    /**
     * 判断是否换卡
     * @return
     */
    public boolean is_change() {
        String old_simiccid = "";
        List<SIM> simList = simHelper.loadSim();
        if (simList.size() > 0) {
            old_simiccid = simList.get(simList.size() - 1).getIccid();
        }
    //    Log.w(TAG, "old_simiccid： " + old_simiccid);
        //获取SIM卡的身份证
        String iccid = "N/A";
        iccid = Tel.getSimSerialNumber();
       // Log.w(TAG, "iccid： " + iccid);
        if (iccid != null) {
            simHelper.addSim(iccid);
        }
        //判断SIM卡是否切换，若切换清空数据库中过去SIM卡的联系人
        if (!old_simiccid.equals(iccid)) {
            return true;
        }
        return false;
    }

    public ContactsActivity() {
    }

    //枚举类fragment按下键操作（1代表在fragment中按下键,2代表不在fragment中按下键）
    public enum downState {
        down(1),
        nodown(2);
        private final int statenum;

        downState(int statenum) {
            this.statenum = statenum;
        }

        public int getStatenum() {
            return statenum;
        }
    }

    /**
     * 获取本机号码方法
     *
     * @return
     */
    public void local_phonenumber() {

        TelephonyManager tms = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        String phoneNumber = tms.getLine1Number();
        if(phoneNumber!=null) {
            phoneHelper.addPhones(phoneNumber);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_detail_layout);
        instance =this;
        initView();//初始化参数
        init_status();//初始化状态栏
        //监听SIM卡手机号实例化
        MyListener = new MyPhoneStateListener();
        handler.post(runnable); //发送消息，启动线程监听运行
//        if (BuildConfig.LOG_DEBUG) {
//            LogUtils.isShowLog = true;
//        } else {
//            LogUtils.isShowLog = false;
//        }

        if(isRoot()){
            TCLToast.makeText(getBaseContext(), "本机已获取Root权限", TCLToast.LENGTH_SHORT).show();
        }
    }

   // String key = "tcl.tv5g";//加密唯一标识

    /**
     * 初始化图标
     */
    public void init_status() {
        iv_sign = findViewById(R.id.Iv_sign);
        signal = findViewById(R.id.signal);
        signal_data = findViewById(R.id.signal_data);
        sign_id = findViewById(R.id.sign_id);
    }
    int is_cc=0;
    //初始化参数
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    private void initView() {

        //列表遮罩初始化
    //    mask = findViewById(R.id.contact_mask);
        //搜索框初始化
        etInput = (TCLEditText) findViewById(R.id.text_name);
        local_phone = findViewById(R.id.local_phone);

        //对添加按钮进行监听
        add1=findViewById(R.id.add1);
        add1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==KeyEvent.KEYCODE_DPAD_DOWN && mData_contacts.size()==0){

                    L.i(TAG, "add1.requestFocuse------： ");
                }
                return false;
            }
        });
//        Drawable add1drawable = getResources().getDrawable(R.drawable.ic_add_focus);
//        Drawable add1no_drawable = getResources().getDrawable(R.drawable.ic_add_nor);
//        add1drawable.setBounds(0, 0, add1drawable.getMinimumWidth(), add1drawable.getMinimumHeight());
//        add1no_drawable.setBounds(0, 0, add1no_drawable.getMinimumWidth(), add1no_drawable.getMinimumHeight());
//        add1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(b){
//                   add1.setCompoundDrawables(add1drawable,null,null,null);
//                }else{
//                    add1.setCompoundDrawables(add1no_drawable,null,null,null);
//                }
//            }
//        });
//        Drawable copydrawable = getResources().getDrawable(R.drawable.ic_copy_focus);
//        Drawable copyno_drawable = getResources().getDrawable(R.drawable.ic_copy_nor);
        //对复制按钮进行监听和聚焦
        copy=findViewById(R.id.copy);
//        copydrawable.setBounds(0, 0, copydrawable.getMinimumWidth(), copydrawable.getMinimumHeight());
//        copyno_drawable.setBounds(0, 0, copyno_drawable.getMinimumWidth(), copyno_drawable.getMinimumHeight());
//        copy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(b){
//                    copy.setCompoundDrawables(copydrawable,null,null,null);
//                }else{
//                    copy.setCompoundDrawables(copyno_drawable,null,null,null);
//                }
//            }
//        });

        copy.setOnKeyListener(new View.OnKeyListener() {
          @Override
          public boolean onKey(View view, int i, KeyEvent keyEvent) {
              //向左不变
              if (i == KeyEvent.KEYCODE_DPAD_RIGHT) {
                  copy.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          copy.setFocusable(true);
                          copy.setFocusableInTouchMode(true);
                          copy.requestFocus();
                      } }, 1);
                  //向下聚焦列表（列表为空）
              }else if(i==KeyEvent.KEYCODE_DPAD_DOWN && mData_contacts.size()==0){
//                  copy.postDelayed(new Runnable() {
//                      @Override
//                      public void run() {
//                          copy.setFocusable(true);
//                          copy.setFocusableInTouchMode(true);
//                          copy.requestFocus();
//                      } }, 1);
                  //向下聚焦列表
              }else if(i==KeyEvent.KEYCODE_DPAD_DOWN){

//                  add1.setFocusable(true);
//                  add1.setFocusableInTouchMode(true);
//                  add1.requestFocus();

              }
              return false;
          }
        });
       // etInput.setBackgroundResource(R.color.gray_50);
        //对搜索按钮进行聚焦监听，其中is_cc用来判断接下来的按键方向
//        etInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if (b) {
//                    is_cc = 1;
//                    //etInput.setBackgroundResource(R.color.transparent);
//                  //  etInput.getBackground().setAlpha(0);
//                }else{
//                    is_cc = 0;
//                //    etInput.setBackgroundResource(R.color.gray_50);
//                   // etInput.getBackground().setAlpha(255);
//                }
//            }
//
//        });

        //对搜索按钮进行按键监听
        etInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                boolean is_pan=isSoftShowing();
                //按OK键时动作
                if(i == KeyEvent.KEYCODE_DPAD_CENTER){
                    is_cc = 1;
                    return false;
                }
                if(is_pan){
                    is_cc = 1;
                    return false;
                }else {
                    //按上键时动作
                    if (i == KeyEvent.KEYCODE_DPAD_UP && etInput.isFocusable()) {

                        if (is_cc == 1) {
//                            call_contracts.requestFocus();
//                            call_contracts.setNeedBreath(true);
                            is_cc = 0;
                        } else {
                            is_cc = 1;
                        }
                        return false;
                        //按左键时动作
                    } else if (i == KeyEvent.KEYCODE_DPAD_LEFT) {
                        is_cc = 1;
                        return false;
                        //按下键时动作
                    }else if(i==KeyEvent.KEYCODE_DPAD_DOWN && mData_contacts.size()==0){
                        etInput.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                etInput.setFocusable(true);
                                etInput.setFocusableInTouchMode(true);
                                etInput.requestFocus();
                            } }, 1);
                    }else if(i == KeyEvent.KEYCODE_DPAD_RIGHT){
                        etInput.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                etInput.setFocusable(true);
                                etInput.setFocusableInTouchMode(true);
                                etInput.requestFocus();
                            } }, 1);
                    }
                    return false;
                }
            }
        });

        //获取本机号码
        getPhoneNumber();

        //对通话记录按钮进行声明
        call_record = findViewById(R.id.record_button);
        call_record.setTextContent("5G通话");
        call_record.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {

                    //跳转到通话主界面，并清空栈顶界面
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ContactsActivity.this, MainActivity.class);
                    Log.i("PZR"+TAG, "onSignalStrengthsChanged: 跳转到通话主界面，并清空栈顶界面");
                    startActivity(intent);

            }
        });
        //对联系人按钮进行声明
        call_contracts = findViewById(R.id.contacts__button);
        call_contracts.requestFocus();
        call_contracts.setTextContent("联系人");
        call_contracts.setMiddleFocusStatus(true);
        call_contracts.setNeedBreath(true);
        call_contracts.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_LEFT){
                    call_record.setFocusable(true);
                }else if(code == KeyEvent.KEYCODE_DPAD_DOWN){
                    isReturn = 0;
                    call_record.setFocusable(false);
                    add1.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            add1.setFocusable(true);
                            add1.setFocusableInTouchMode(true);
                            add1.requestFocus();
                        } }, 1);
                }else{
                    isReturn = 0;
                    call_record.setFocusable(false);
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

        try {
            //初始化联系人列表
            initcontact();
        } catch (IOException e) {
            //IO异常抛出
            e.printStackTrace();
        } catch (JSONException e) {
            //JSONE异常抛出
            e.printStackTrace();
        }
    }





    /**
     * 获取本机号码手机号（从数据库中）
     */
    public void getPhoneNumber(){
        List<Phone> phoneList=phoneHelper.loadPhones();
        if(phoneList.size()>0) {
            if (phoneList.isEmpty() || phoneList.get(phoneList.size() - 1).getPhone().equals("")) {
                local_phone.setText("");
            } else {
                local_phone.setText(phoneList.get(phoneList.size() - 1).getPhone());
                PropertyUtils.set("com.tcl.android.phone", phoneList.get(phoneList.size() - 1).getPhone());
            }
        }
    }
    /**
     * 点击联系人按钮
     * @param view
     */
    public void contacts__buttons(View view){

        //Intent跳转到联系人页面
        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
        intent.putExtra("type", "1");
        startActivity(intent);
        //清空之前的页面
        finish();

    }


    /**
     * 提取11位的数字
     * @param sms
     * @return
     */
    public static String GetPhoneNumberFromSMSText(String sms){

        List<String> list=GetNumberInString(sms);
        for(String str:list){
            if(str.length()==11)
                return str;
        }
        return "";
    }

    /**
     * 对信息进行数字提取
     * @param str
     * @return
     */
    public static List<String> GetNumberInString(String str){
        List<String> list=new ArrayList<String>();
        String regex = "\\d*";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(str);
        while (m.find()) {
            if (!"".equals(m.group()))
                list.add(m.group());
        }
        return list;
    }

    /**
     * 联系人列表实现
     */
    private void initcontact() throws IOException, JSONException {

        recyclerView_contact = findViewById(R.id.recycler_contracts);
        //设置RecyclerView布局管理器
        linearLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView_contact.setLayoutManager(linearLayoutManager);

        //读取数据库中联系人列表的数据
        loadContact();
        //构建适配器
        myAdapter = new ContactAdaptere(this, mData_contacts,recyclerView_contact);

        Log.w(TAG, "initcontact: myAdapter " + myAdapter);
        recyclerView_contact.setAdapter(myAdapter);

        recyclerView_contact.addItemDecoration(new SimpleItemDecoration());
        //设置分分割线
//        recyclerView_contact.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        recyclerView_contact.setItemViewCacheSize(20);


        //判断遮罩的显示逻辑
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//        if (firstItemPosition > 0){
//            mask.setVisibility(View.VISIBLE);
//        }else {
//            mask.setVisibility(View.GONE);
//        }

        //设置联系人列表的监听器
        ContactAdaptere.setOnItemClickListener(new ContactAdaptere.OnItemClickListener() {

            @Override
            public void onFocusLiterner(View v, boolean b, int position) {
//                    fragmentPage(position);
                    //Log.i(TAG, "触摸---------" + position);
//                linearLayoutManager.smoothScrollToPosition(recyclerView_contact, new RecyclerView.State(),position);
            }

            //点击操作执行展示联系人操作
            @Override
            public void onItemClick(final int position) {
                    //fragment页面的操作
                    linearLayoutManager.setScrollEnabled(false);
                    fragmentPage(position);
            }

            //按键操作监听事件
            @Override
            public void onItemLeftClick(int keyCode, View view, int position) {

                switch (keyCode){
                    //触发下键操作监听
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        isdown=2;
                        break;
                    //触发右键执行展示联系人操作
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        //fragment页面的操作
                        linearLayoutManager.setScrollEnabled(false);
                        fragmentPage(position);
                        Log.i(TAG, "KEYCODE_DPAD_RIGHT: myAdapter ---------" + position);
                        break;

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }
        });

        //搜索界面的动态查询
        etInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                //调用过滤器
                getFilter().filter(charSequence);
                myAdapter.notifyDataSetChanged();

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * fragment页面的操作
     * @param position
     */
    public void fragmentPage(int position){

        //根据item位置读取详情信息
        Contact contact = mData_contacts.get(position);
        L.i(TAG, "fragmentPagei: name " + mData_contacts.size());
        id=contact.getId();
        name=contact.getName();
        L.i(TAG, "fragmentPagei: name " + name);
        phone_number=contact.getNumber();
        storage=contact.getStorage();
        photo=contact.getPhoto();
        //设置被点击item位置的背景颜色
        contact.setSign(1);
        L.i(TAG, "fragmentPagei: id " + id);
        //存储当前的位置，方便后面onKeydown方法使用
        positions = position;
        L.i(TAG, "fragmentPage: positions " + positions);

        //在fragment按下操作标识
        isdown= downState.down.getStatenum();
        //fragment页面显示
        isFragment= AuditState.VISIABLE.getStatenum();

        //判断是否有
        boolean is_Exit=isPhone_number(phone_number);

        //触发执行fragment布局页面
        fragment = new FirstFragment(contact);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contacts_detail, fragment);
        transaction.addToBackStack("fragment");

        transaction.commit();

        ss=1;

    }


    /**
     * 加载联系人数据库列表
     *
     */
    private void loadContact() {

        //调用数据库中展示数据库列表的方法
        contacts=contactsHelper.loadContacts();
        //循环从数据库contacts中取出数据
        for(int i=0;i<contacts.size();i++){
            String name = contacts.get(i).getName();
            String phoneNumber = contacts.get(i).getPhoneNumber();
            int gender=contacts.get(i).getPhoto();
            int id=contacts.get(i).getId();
            int storage=contacts.get(i).getStorage();
            mData_contacts.add(new Contact(id,name,phoneNumber,storage,gender));
        }
        if(mData_contacts.size()>=4) {
            mData_contacts.add(new Contact(5001, "0000", "", 3, 2));
            L.i(TAG, "已存空白数据");
        }
//        //开启短信列表服务
//        Intent timeService = new Intent(this, SimService.class);
//        startService(timeService);
    }

    /**
     * 跳转到添加联系人界面
     *
     * @param view
     */
    public void onKeyAdd(View view) {

        //跳转到添加联系人界面
        Intent intent = new Intent(getBaseContext(), AddContactActivity.class);
        intent.putExtra("type", "1");
        startActivity(intent);
        finish();

    }


    /**
     * 判断当前屏幕有没有软键盘
     * @return
     */
    private boolean isSoftShowing() {
        //获取当前屏幕内容的高度
        int screenHeight = getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.bottom != 0;
    }
    /**
     * 跳转到复制界面
     * @param view
     */
    public  void onKeyCopy(View view){
        //跳转到复制主界面
        Intent intent = new Intent(getBaseContext(), ContactsCopyActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 页面的按键监听
     * @param keyCode
     * @param event
     * @return true:代表屏蔽当前的按键，false代表解除被屏蔽的按键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        View view=getWindow().getDecorView().findFocus();
        buttVideo = findViewById(R.id.buttVideo);
        L.i(TAG, "onKeyDown: onKeyDown " + keyCode);
        //监听操作是否在fragment布局中处理
            if (fragment instanceof FirstFragment) {
                if(fragment.isVisible()) {
                    L.i(TAG, "onKeyDown: fragment " + isFragment);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT://左键
                            L.i("ooo", "view.getId()--:"+view.getId());
                            if (isFragment == AuditState.VISIABLE.getStatenum() && view.getId()== R.id.buttVideo) {
                                //移除当前的fragment
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.remove(fragment);
                                //commitNowAllowingStateLoss
                                fragmentTransaction.commit();
                                //更新当前的item状态
                                mData_contacts.get(positions).setSign(0);
                                recyclerView_contact.setDefaultSelect(positions);

                                linearLayoutManager.setScrollEnabled(true);
                               // myAdapter.notifyItemChanged(positions);
                                isFragment= AuditState.UNVISIABLE.getStatenum();
                                L.i(TAG, "onKeyDown: isFragment " + isFragment);
                            }
                            //不在fragment中按下键
                            isdown= downState.nodown.getStatenum();
                            //设置fragment页面的标识可见
                            isFragment= AuditState.VISIABLE.getStatenum();
                            ss=0;
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT://右键

                            isdown= downState.down.getStatenum();
                            //设置fragment页面的标识不可见
                            isFragment= AuditState.UNVISIABLE.getStatenum();
                            break;

                        case KeyEvent.KEYCODE_DPAD_DOWN://下键

                            isFragment= AuditState.UNVISIABLE.getStatenum();
                            if (isdown == downState.down.getStatenum()) {
                                //聚焦视频通话的控件
                                buttVideo = findViewById(R.id.buttVideo);
                                buttVideo.setFocusable(true);
                                buttVideo.setFocusableInTouchMode(true)  ;
                                buttVideo.requestFocus();
                                isFragment= AuditState.VISIABLE.getStatenum();
                                //屏蔽fragment中的下键
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP://上键
                            //判断是否是在fragment中进行下键
                            isdown = downState.down.getStatenum();;
//                            isFragment= AuditState.VISIABLE.getStatenum();
                            if(isdown== downState.down.getStatenum()){
                                //聚焦联系人头像的控件
                                editContact=findViewById(R.id.editContacts);
                                editContact.setFocusable(true);
                                editContact.setFocusableInTouchMode(true);
                                editContact.requestFocus();
                                //屏蔽fragment中的上键
                                return true;
                            }

                            break;


                    }
                }
                else{
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK://返回
                            Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                            Log.i("PZR"+TAG,
                                    "  KEYCODE_BACK------  ");
                            startActivity(intent);
                            finish();

                    }
                }
            }
            //当前是返回键
            if(keyCode==KeyEvent.KEYCODE_BACK){
                if(ss==1){
                    //移除当前的fragment
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(fragment);
                    fragmentTransaction.commit();
                    //更新当前的item状态
                    mData_contacts.get(positions).setSign(0);
                    recyclerView_contact.setDefaultSelect(positions);

                    myAdapter.notifyItemChanged(positions);

                    linearLayoutManager.setScrollEnabled(true);
                    isFragment= AuditState.UNVISIABLE.getStatenum();

                    //不在fragment中按下键
                    isdown= downState.nodown.getStatenum();
                    //设置fragment页面的标识可见
                    isFragment= AuditState.VISIABLE.getStatenum();
                    Log.i(TAG, "  KEYCODE_BACK-----"+ss);
                    return false;
                }
                Log.i(TAG, "  isReturn-----:"+getIntent().getIntExtra("isReturn",-1));
                if(getIntent().getIntExtra("isReturn",-1)==1){
                    finish();
                }else if(isReturn == 0) {
                    Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                    startActivity(intent);
                    finish();
                }
//                if(getIntent().getIntExtra("isReturn",-1)==1){
//                    getSupportFragmentManager().getFragments().clear();
//                    getSupportFragmentManager().popBackStack();
//                    Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
//                    Log.i(TAG,
//                            "  isReturn_BACK------  ");
//                    startActivity(intent);
//                    finish();
//                }
                Log.i(TAG, "  全局返回键盘-----");
                //System.exit(0);
            }

            //监听全局下键，解除屏蔽
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
               // linearLayoutManager.setScrollEnabled(true);
                if(isdown== downState.nodown.getStatenum()){

                    return false;
                }

            }
            //监听全局上键，解除屏蔽
            if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
              //  linearLayoutManager.setScrollEnabled(true);
                if(isdown== downState.nodown.getStatenum()){
                    return false;
                }
            }
            // 菜单按钮
            if(keyCode==KeyEvent.KEYCODE_MENU){
//                Intent OtherIntent = new Intent();
//                OtherIntent.setClassName("com.tcl.settings","com.tcl.settings.ShowWindowService");
//                OtherIntent.putExtra("flag", 1);
//                OtherIntent.putExtra("Type", "Settings");
//                OtherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                OtherIntent.setAction("com.tcl.settings.SHOW_WINDOW");
//                startService(OtherIntent);
            }
            return super.onKeyDown(keyCode, event);

    }
    @Override
    protected void onDestroy() {
        //将线程销毁掉
        Log.i("PZR"+TAG,"onDestroy");
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /*
    * onResume方法
    */
    @Override
    protected void onResume() {
        if(transaction!=null&&fragment!=null){
            Log.i("PZR_Contact_onResume","fragement");
            transaction.remove(fragment);
        }
        super.onResume();
    }


    /**
     * 过滤器重写方法实现
     * @return
     */
    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }
    private static int[] getScrollAmount(RecyclerView recyclerView, View view) {
        int[] out = new int[2];
        int position = recyclerView.getChildAdapterPosition(view);
        final int parentLeft = recyclerView.getPaddingLeft();
        final int parentTop = recyclerView.getPaddingTop();
        final int parentRight = recyclerView.getWidth() - recyclerView.getPaddingRight();
        final int parentBottom = recyclerView.getHeight() - recyclerView.getPaddingBottom();
        final int childLeft = view.getLeft() - view.getScrollX();
        final int childTop = view.getTop() - view.getScrollY();

        //item左边距减去Recyclerview不在屏幕内的部分，加当前Recyclerview一半的宽度就是居中
        final int dx = childLeft - parentLeft - ((parentRight - view.getWidth()) / 2);

        //同上
        final int dy = childTop - parentTop - (parentBottom - view.getHeight()) / 2;
        out[0] = dx;
        out[1] = dy;
        return out;


    }
    /**
     * 搜索过滤器实现类
     */
    public  class ArrayFilter extends Filter {

        private ArrayList<Contact> mOriginalValues;
        private final Object mLock = new Object();


        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            //过滤的结果
            FilterResults results = new FilterResults();
            //原始数据备份为空时，上锁，同步复制原始数据
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mData_contacts);
                }
            }
            //当首字母为空时
            if (prefix == null || prefix.length() == 0) {
                ArrayList<Contact> list;
                synchronized (mLock) {//同步复制一个原始备份数据
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                //此时返回的results就是原始的数据，不进行过滤
                results.count = list.size();
                //数据库查询
                //contacts_search=contactsHelper.searchContacts(etInput.getText().toString().trim());

            } else {
                //转化为小写
                String prefixString = prefix.toString().toLowerCase();
                //数据库查询
                //contacts_search=contactsHelper.searchContacts(etInput.getText().toString().trim());

                ArrayList<Contact> values;
                //同步复制一个原始备份数据
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<Contact> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    //从List<Contact>中拿到Contact对象
                    final Contact value = values.get(i);
                    //Contact对象的phonename属性作为过滤的参数
                    final String valueText = value.getNumber().toString().toLowerCase();
                    //Contact对象的name属性作为过滤的参数
                    final String valueTexts = value.getName().toString().toLowerCase();
                    if (valueText.startsWith(prefixString) || valueText.indexOf(prefixString.toString()) != -1 || valueTexts.startsWith(prefixString) || valueTexts.indexOf(prefixString.toString()) != -1) {//第一个字符是否匹配
                        newValues.add(value);//将这个item加入到数组对象中
                    } else {//处理首字符是空格
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        for (int k = 0; k < wordCount; k++) {
                            //一旦找到匹配的就break，跳出for循环
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                //此时的results就是过滤后的数组
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            //清空数据
            mData_contacts.clear();
            recyclerView_contact.setItemViewCacheSize(0);
           //   List<Contact> mData_contactss = new ArrayList<Contact>();
            //搜索后的数据
            mData_contacts.addAll((Collection<? extends Contact>) results.values);

            if(results.count>0) {
                //更新联系人列表的内容
                try {
                    myAdapter=new ContactAdaptere(getBaseContext(),mData_contacts,recyclerView_contact);
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                recyclerView_contact.setAdapter(myAdapter);
            }else{
                recyclerView_contact.setAdapter(myAdapter);
            }

        }
    }


    /**
     * 判断SIM卡是否有联系人
     * @param phonenumber
     * @return
     */
    public boolean isPhone_number(String phonenumber){
        Log.i(TAG, "---------------------------mData_contacts.size:"+mData_contacts.size());
        if(mData_contacts.size()>0) {
            Log.i(TAG, "---------------------------mData_contacts.size2:"+mData_contacts.size());
            for (int i = 0;i<mData_contacts.size();i++ ){
                if(phonenumber.equals(mData_contacts.get(i).getNumber())){
                    return true;
                }
            }
        }else{
            return false;
        }
        return false;
    }

    /**
     * 监听SIM卡的状态
     */
    class MyPhoneStateListener extends PhoneStateListener {


        private static final String TAG = "ZTY_MyPhoneStateListener";
        //判断当前网络是否有wifi
        int disConnect;
        /**
         * 监听当前SIM信号强度
         * @param signalStrength
         */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

                L.i(TAG, " signalStrength--------:" + signalStrength.getGsmSignalStrength());
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
//            }

        }

        /**
         * 监听网络是否断开
         * @param state
         */
        @Override
        public void onDataConnectionStateChanged(int state) {

                switch (state) {
                    case TelephonyManager.DATA_DISCONNECTED://网络断开
                        signal.setBackgroundResource(0);
                        signal_data.setBackgroundResource(0);
                        disConnect=0;
                        break;
                    case TelephonyManager.DATA_CONNECTING://网络正在连接
                        break;
                    case TelephonyManager.DATA_CONNECTED://网络连接上
                        disConnect=2;
                        //判断当前网络是5G还是4G
                        TelephonyManager telephonyManager = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
                        if (ActivityCompat.checkSelfPermission(ContactsActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
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

//            }

        }

        /**
         * 监听上下行网络
         * @param direction
         */
        @Override
        public void onDataActivity(int direction) {


                //数据网络的状态
                switch (direction) {
                    case TelephonyManager.DATA_ACTIVITY_NONE:
                        if(disConnect==0){
                            signal.setBackgroundResource(0);
                            signal_data.setBackgroundResource(0);
                        }else {
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
    }

    /**
     * 检查无线信号强度
     */
    public void checkWifiState() {

        WifiManager mWifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int wifi = mWifiInfo.getRssi();//获取wifi信号强度
        if (wifi > -50 && wifi < 0) {//最强
//            Log.e(TAG, "----------最强");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_3);
        } else if (wifi > -70 && wifi < -50) {//较强
//            Log.e(TAG, "-----------较强");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_2);
        } else if (wifi > -80 && wifi < -70) {//较弱
//            Log.e(TAG, "----------较弱");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_1);
        } else if (wifi > -100 && wifi < -80) {//微弱
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_0);
//            Log.e(TAG, "----------微弱");
        }

    }

    /**
     * p判断是否有网线插入
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

    /**
     * 网线的返回
     * @return
     */
    public boolean isPluggedIn(){
        Context context = getBaseContext();

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < networkInfo.length; i++) {
            if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
            //    Log.i(TAG, "有网络-------------");
                return true;
            }
        }
//        String state= execCommand("cat /sys/class/net/eth0/carrier");
//        if(state.trim().equals("1")){  //有网线插入时返回1，拔出时返回0
//            Log.i(TAG,"有网线-------------");
//            return true;
//        }
//        Log.i(TAG,"无网线-------------");
        return false;
    }

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
