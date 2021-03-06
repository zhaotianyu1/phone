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
 * ??????????????????Acitvity
 */
public class ContactsActivity extends FragmentActivity implements View.OnClickListener, Filterable {

    //??????????????????
    private static final String TAG = "ContacsActivity";
    //??????????????????????????????
    MyContactsHelper contactsHelper = new MyContactsHelper();
    //?????????????????????
    private SWRecyclerView recyclerView_contact;
    //???????????????
    public List<Contact> mData_contacts = new ArrayList<Contact>();
    public List<Contact> alist = new ArrayList<>();
    //????????????????????????
    public ContactAdaptere myAdapter;
    private List<Contacts> contacts_search;
    //?????????????????????
    private int id;
    private String name;
    private String phone_number;
    private int photo;
    private int storage;

    //??????
    private AllCellsGlowLayout editContact;
    //?????????
    private TCLEditText etInput;
    //???????????????
    private ArrayFilter mFilter;
    //???????????????
    private Button contacts__button;
    //????????????
    private AllCellsGlowLayout buttVideo;
    //??????????????????????????????
    private int positions;
    //????????????
    private ImageView mask;

    TCLNavigationItem call_record;

    TCLNavigationItem call_contracts;
    //??????????????????
    private TextView local_phone;

    List<Person> contacts;

    static Activity instance;

    //RecyclerView???????????????
    private CenterLayoutManager linearLayoutManager;
    //FragmentManager???????????????
    private FragmentManager fragmentManager;
    //??????fragment?????????????????????=0??????????????????>0???????????????
    private int isFragment = 0;
    //?????????fragment??????????????????????????????1?????????fragment???????????????2????????????fragment???????????????
    private int isdown;
    private Context context;
    private ImageView iv_sign;
    //??????fragment??????
    private Fragment fragment;

    private LinearLayout benji;
    //??????fragment????????????
    private FragmentTransaction transaction;
    SimStateReceive simStateReceive;
    PhoneHelper phoneHelper = new PhoneHelper();
    TextView sign_id;
    private TextView signal_data;
    private TextView signal;
    int ss = 0;//??????????????????
    TelephonyManager Tel;
    private int isReturn = 0;//??????????????????
    TCLButton add1;
    TCLButton copy;
    private int count=0;
    SimHelper simHelper = new SimHelper();
    //??????????????????
    public static final String SENT_SMS_ACTION = "SENT_SMS_ACTION";

    MyPhoneStateListener MyListener;
//    public static ContactsActivity instance =null;

    private Handler handler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {

            Tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            String operator = Tel.getSimOperator();
//            L.i(TAG, "???????????? " + operator);
            if (!operator.isEmpty()) {
                //????????????
                if (operator.equals("46000") || operator.equals("46002") ||operator.equals("46004")||operator.equals("46008")|| operator.equals("46007")||operator.equals("46020")){
                    sign_id.setText("????????????");
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
//                            L.i(TAG, "????????????---------??? sendTextMessage");
//                            count+=1;
//                        }
                        } else {
                            getPhoneNumber();
                        }
                    }

                } else if (operator.equals("46001")|| operator.equals("46006")||operator.equals("46009")||operator.equals("46010"))  {//????????????
                    sign_id.setText("????????????");
                    List<Phone> phoneList=phoneHelper.loadPhones();
                    if(phoneList.get(phoneList.size() - 1).getPhone().equals("")){
                        local_phonenumber();
                        getPhoneNumber();
                    }
                }  else if (operator.equals("46003") || operator.equals("46011")||operator.equals("46005")||operator.equals("46012")) {//????????????

                    sign_id.setText("????????????");
                    List<Phone> phoneList=phoneHelper.loadPhones();
//                    L.i(TAG, "phoneList.size-----123----???"+phoneList.size());
                    if(phoneList.size()>0) {
                        if (phoneList.get(phoneList.size() - 1).getPhone().equals("") || is_change()) {
                            local_phonenumber();
//                        while (count<1) {
//                            Intent sentIntent = new Intent(SENT_SMS_ACTION);
//                            PendingIntent sentPI = PendingIntent.getBroadcast(getBaseContext(), 0, sentIntent, 0);
//                            L.i(TAG, "????????????---------??? sendTextMessage");
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
            //??????????????????
            internet();
            // ????????????SIM????????????????????????
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
            // ???3???????????????
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
         //       Log.i(TAG, "--------?????????????????????----");
                iv_sign.setBackgroundResource(R.drawable.ic_network);
                signal_data.setBackgroundResource(0);
                signal.setBackgroundResource(0);
            }else if(Tel.getSimState() == TelephonyManager.SIM_STATE_READY){
           //     Log.i(TAG, "--------?????????????????????----");
                Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                        PhoneStateListener.LISTEN_DATA_ACTIVITY |
                        PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            }else if(Tel.getSimState() != TelephonyManager.SIM_STATE_READY){
                Log.i(TAG, "-------???SIM???---");
                iv_sign.setBackgroundResource(0);
                signal_data.setBackgroundResource(0);
                signal.setBackgroundResource(0);
            }
        } else if (wifiNetInfo.isConnected()) {
          //  Log.i(TAG, "-------???Wifi????????????---");
            checkWifiState();
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
        }else if(mobNetInfo.isConnected()) {
        //    L.i(TAG, "-----???????????????????????? ---------");
            Tel.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS |
                    PhoneStateListener.LISTEN_DATA_ACTIVITY |
                    PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        }else {
            Log.i(TAG, "-------???SIM???---");
            iv_sign.setBackgroundResource(0);
            signal_data.setBackgroundResource(0);
            signal.setBackgroundResource(0);
        }
    }

    @Override
    public void onClick(View view) {

    }

    /**
     * ????????????SIM????????????????????????
     * @throws IOException
     * @throws JSONException
     */
    private void updateTime() throws IOException, JSONException {
        L.i(TAG, "updateTime??? ---------");
        L.i(TAG, "contacts???size??? ---------"+mData_contacts.size());

        List<Person> personList= contactsHelper.loadContacts();
        L.i(TAG, "personList???size??? ---------"+personList.size());
        List<Contact> contactList = new ArrayList<>(); //??????
        if(personList.size()+1>mData_contacts.size() || personList.size()+1<mData_contacts.size()){
            L.i(TAG, "updateTime??? --------- start");
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
            L.i(TAG, "updateTime??? --------- end");
        }
    }

    //?????????fragment?????????????????????0??????????????????1???????????????
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
     * ??????????????????
     * @return
     */
    public boolean is_change() {
        String old_simiccid = "";
        List<SIM> simList = simHelper.loadSim();
        if (simList.size() > 0) {
            old_simiccid = simList.get(simList.size() - 1).getIccid();
        }
    //    Log.w(TAG, "old_simiccid??? " + old_simiccid);
        //??????SIM???????????????
        String iccid = "N/A";
        iccid = Tel.getSimSerialNumber();
       // Log.w(TAG, "iccid??? " + iccid);
        if (iccid != null) {
            simHelper.addSim(iccid);
        }
        //??????SIM???????????????????????????????????????????????????SIM???????????????
        if (!old_simiccid.equals(iccid)) {
            return true;
        }
        return false;
    }

    public ContactsActivity() {
    }

    //?????????fragment??????????????????1?????????fragment????????????,2????????????fragment???????????????
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
     * ????????????????????????
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
        //??????????????????
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_detail_layout);
        instance =this;
        initView();//???????????????
        init_status();//??????????????????
        //??????SIM?????????????????????
        MyListener = new MyPhoneStateListener();
        handler.post(runnable); //???????????????????????????????????????
//        if (BuildConfig.LOG_DEBUG) {
//            LogUtils.isShowLog = true;
//        } else {
//            LogUtils.isShowLog = false;
//        }

        if(isRoot()){
            TCLToast.makeText(getBaseContext(), "???????????????Root??????", TCLToast.LENGTH_SHORT).show();
        }
    }

   // String key = "tcl.tv5g";//??????????????????

    /**
     * ???????????????
     */
    public void init_status() {
        iv_sign = findViewById(R.id.Iv_sign);
        signal = findViewById(R.id.signal);
        signal_data = findViewById(R.id.signal_data);
        sign_id = findViewById(R.id.sign_id);
    }
    int is_cc=0;
    //???????????????
    @SuppressLint({"WrongViewCast", "ClickableViewAccessibility"})
    private void initView() {

        //?????????????????????
    //    mask = findViewById(R.id.contact_mask);
        //??????????????????
        etInput = (TCLEditText) findViewById(R.id.text_name);
        local_phone = findViewById(R.id.local_phone);

        //???????????????????????????
        add1=findViewById(R.id.add1);
        add1.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==KeyEvent.KEYCODE_DPAD_DOWN && mData_contacts.size()==0){

                    L.i(TAG, "add1.requestFocuse------??? ");
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
        //????????????????????????????????????
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
              //????????????
              if (i == KeyEvent.KEYCODE_DPAD_RIGHT) {
                  copy.postDelayed(new Runnable() {
                      @Override
                      public void run() {
                          copy.setFocusable(true);
                          copy.setFocusableInTouchMode(true);
                          copy.requestFocus();
                      } }, 1);
                  //????????????????????????????????????
              }else if(i==KeyEvent.KEYCODE_DPAD_DOWN && mData_contacts.size()==0){
//                  copy.postDelayed(new Runnable() {
//                      @Override
//                      public void run() {
//                          copy.setFocusable(true);
//                          copy.setFocusableInTouchMode(true);
//                          copy.requestFocus();
//                      } }, 1);
                  //??????????????????
              }else if(i==KeyEvent.KEYCODE_DPAD_DOWN){

//                  add1.setFocusable(true);
//                  add1.setFocusableInTouchMode(true);
//                  add1.requestFocus();

              }
              return false;
          }
        });
       // etInput.setBackgroundResource(R.color.gray_50);
        //??????????????????????????????????????????is_cc????????????????????????????????????
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

        //?????????????????????????????????
        etInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                boolean is_pan=isSoftShowing();
                //???OK????????????
                if(i == KeyEvent.KEYCODE_DPAD_CENTER){
                    is_cc = 1;
                    return false;
                }
                if(is_pan){
                    is_cc = 1;
                    return false;
                }else {
                    //??????????????????
                    if (i == KeyEvent.KEYCODE_DPAD_UP && etInput.isFocusable()) {

                        if (is_cc == 1) {
//                            call_contracts.requestFocus();
//                            call_contracts.setNeedBreath(true);
                            is_cc = 0;
                        } else {
                            is_cc = 1;
                        }
                        return false;
                        //??????????????????
                    } else if (i == KeyEvent.KEYCODE_DPAD_LEFT) {
                        is_cc = 1;
                        return false;
                        //??????????????????
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

        //??????????????????
        getPhoneNumber();

        //?????????????????????????????????
        call_record = findViewById(R.id.record_button);
        call_record.setTextContent("5G??????");
        call_record.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean b) {

                    //????????????????????????????????????????????????
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ContactsActivity.this, MainActivity.class);
                    Log.i("PZR"+TAG, "onSignalStrengthsChanged: ????????????????????????????????????????????????");
                    startActivity(intent);

            }
        });
        //??????????????????????????????
        call_contracts = findViewById(R.id.contacts__button);
        call_contracts.requestFocus();
        call_contracts.setTextContent("?????????");
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
                    Log.i(TAG, "???????????????-------");
                    if(isReturn ==1){
                        Log.i(TAG, "??????finish-------");
                        finish();
                    }else{
                        Log.i(TAG, "??????isReturn+1-------");
                        isReturn = 1;
                    }
                }
                return false;
            }
        });

        try {
            //????????????????????????
            initcontact();
        } catch (IOException e) {
            //IO????????????
            e.printStackTrace();
        } catch (JSONException e) {
            //JSONE????????????
            e.printStackTrace();
        }
    }





    /**
     * ????????????????????????????????????????????????
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
     * ?????????????????????
     * @param view
     */
    public void contacts__buttons(View view){

        //Intent????????????????????????
        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
        intent.putExtra("type", "1");
        startActivity(intent);
        //?????????????????????
        finish();

    }


    /**
     * ??????11????????????
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
     * ???????????????????????????
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
     * ?????????????????????
     */
    private void initcontact() throws IOException, JSONException {

        recyclerView_contact = findViewById(R.id.recycler_contracts);
        //??????RecyclerView???????????????
        linearLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView_contact.setLayoutManager(linearLayoutManager);

        //??????????????????????????????????????????
        loadContact();
        //???????????????
        myAdapter = new ContactAdaptere(this, mData_contacts,recyclerView_contact);

        Log.w(TAG, "initcontact: myAdapter " + myAdapter);
        recyclerView_contact.setAdapter(myAdapter);

        recyclerView_contact.addItemDecoration(new SimpleItemDecoration());
        //??????????????????
//        recyclerView_contact.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
//        recyclerView_contact.setItemViewCacheSize(20);


        //???????????????????????????
        int firstItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
//        if (firstItemPosition > 0){
//            mask.setVisibility(View.VISIBLE);
//        }else {
//            mask.setVisibility(View.GONE);
//        }

        //?????????????????????????????????
        ContactAdaptere.setOnItemClickListener(new ContactAdaptere.OnItemClickListener() {

            @Override
            public void onFocusLiterner(View v, boolean b, int position) {
//                    fragmentPage(position);
                    //Log.i(TAG, "??????---------" + position);
//                linearLayoutManager.smoothScrollToPosition(recyclerView_contact, new RecyclerView.State(),position);
            }

            //???????????????????????????????????????
            @Override
            public void onItemClick(final int position) {
                    //fragment???????????????
                    linearLayoutManager.setScrollEnabled(false);
                    fragmentPage(position);
            }

            //????????????????????????
            @Override
            public void onItemLeftClick(int keyCode, View view, int position) {

                switch (keyCode){
                    //????????????????????????
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        isdown=2;
                        break;
                    //???????????????????????????????????????
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        //fragment???????????????
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

        //???????????????????????????
        etInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {

                //???????????????
                getFilter().filter(charSequence);
                myAdapter.notifyDataSetChanged();

            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    /**
     * fragment???????????????
     * @param position
     */
    public void fragmentPage(int position){

        //??????item????????????????????????
        Contact contact = mData_contacts.get(position);
        L.i(TAG, "fragmentPagei: name " + mData_contacts.size());
        id=contact.getId();
        name=contact.getName();
        L.i(TAG, "fragmentPagei: name " + name);
        phone_number=contact.getNumber();
        storage=contact.getStorage();
        photo=contact.getPhoto();
        //???????????????item?????????????????????
        contact.setSign(1);
        L.i(TAG, "fragmentPagei: id " + id);
        //????????????????????????????????????onKeydown????????????
        positions = position;
        L.i(TAG, "fragmentPage: positions " + positions);

        //???fragment??????????????????
        isdown= downState.down.getStatenum();
        //fragment????????????
        isFragment= AuditState.VISIABLE.getStatenum();

        //???????????????
        boolean is_Exit=isPhone_number(phone_number);

        //????????????fragment????????????
        fragment = new FirstFragment(contact);
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.contacts_detail, fragment);
        transaction.addToBackStack("fragment");

        transaction.commit();

        ss=1;

    }


    /**
     * ??????????????????????????????
     *
     */
    private void loadContact() {

        //????????????????????????????????????????????????
        contacts=contactsHelper.loadContacts();
        //??????????????????contacts???????????????
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
            L.i(TAG, "??????????????????");
        }
//        //????????????????????????
//        Intent timeService = new Intent(this, SimService.class);
//        startService(timeService);
    }

    /**
     * ??????????????????????????????
     *
     * @param view
     */
    public void onKeyAdd(View view) {

        //??????????????????????????????
        Intent intent = new Intent(getBaseContext(), AddContactActivity.class);
        intent.putExtra("type", "1");
        startActivity(intent);
        finish();

    }


    /**
     * ????????????????????????????????????
     * @return
     */
    private boolean isSoftShowing() {
        //?????????????????????????????????
        int screenHeight = getWindow().getDecorView().getHeight();
        //??????View???????????????bottom
        Rect rect = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.bottom != 0;
    }
    /**
     * ?????????????????????
     * @param view
     */
    public  void onKeyCopy(View view){
        //????????????????????????
        Intent intent = new Intent(getBaseContext(), ContactsCopyActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * ?????????????????????
     * @param keyCode
     * @param event
     * @return true:??????????????????????????????false??????????????????????????????
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        View view=getWindow().getDecorView().findFocus();
        buttVideo = findViewById(R.id.buttVideo);
        L.i(TAG, "onKeyDown: onKeyDown " + keyCode);
        //?????????????????????fragment???????????????
            if (fragment instanceof FirstFragment) {
                if(fragment.isVisible()) {
                    L.i(TAG, "onKeyDown: fragment " + isFragment);
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT://??????
                            L.i("ooo", "view.getId()--:"+view.getId());
                            if (isFragment == AuditState.VISIABLE.getStatenum() && view.getId()== R.id.buttVideo) {
                                //???????????????fragment
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                fragmentTransaction.remove(fragment);
                                //commitNowAllowingStateLoss
                                fragmentTransaction.commit();
                                //???????????????item??????
                                mData_contacts.get(positions).setSign(0);
                                recyclerView_contact.setDefaultSelect(positions);

                                linearLayoutManager.setScrollEnabled(true);
                               // myAdapter.notifyItemChanged(positions);
                                isFragment= AuditState.UNVISIABLE.getStatenum();
                                L.i(TAG, "onKeyDown: isFragment " + isFragment);
                            }
                            //??????fragment????????????
                            isdown= downState.nodown.getStatenum();
                            //??????fragment?????????????????????
                            isFragment= AuditState.VISIABLE.getStatenum();
                            ss=0;
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT://??????

                            isdown= downState.down.getStatenum();
                            //??????fragment????????????????????????
                            isFragment= AuditState.UNVISIABLE.getStatenum();
                            break;

                        case KeyEvent.KEYCODE_DPAD_DOWN://??????

                            isFragment= AuditState.UNVISIABLE.getStatenum();
                            if (isdown == downState.down.getStatenum()) {
                                //???????????????????????????
                                buttVideo = findViewById(R.id.buttVideo);
                                buttVideo.setFocusable(true);
                                buttVideo.setFocusableInTouchMode(true)  ;
                                buttVideo.requestFocus();
                                isFragment= AuditState.VISIABLE.getStatenum();
                                //??????fragment????????????
                                return true;
                            }
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP://??????
                            //??????????????????fragment???????????????
                            isdown = downState.down.getStatenum();;
//                            isFragment= AuditState.VISIABLE.getStatenum();
                            if(isdown== downState.down.getStatenum()){
                                //??????????????????????????????
                                editContact=findViewById(R.id.editContacts);
                                editContact.setFocusable(true);
                                editContact.setFocusableInTouchMode(true);
                                editContact.requestFocus();
                                //??????fragment????????????
                                return true;
                            }

                            break;


                    }
                }
                else{
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK://??????
                            Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                            Log.i("PZR"+TAG,
                                    "  KEYCODE_BACK------  ");
                            startActivity(intent);
                            finish();

                    }
                }
            }
            //??????????????????
            if(keyCode==KeyEvent.KEYCODE_BACK){
                if(ss==1){
                    //???????????????fragment
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(fragment);
                    fragmentTransaction.commit();
                    //???????????????item??????
                    mData_contacts.get(positions).setSign(0);
                    recyclerView_contact.setDefaultSelect(positions);

                    myAdapter.notifyItemChanged(positions);

                    linearLayoutManager.setScrollEnabled(true);
                    isFragment= AuditState.UNVISIABLE.getStatenum();

                    //??????fragment????????????
                    isdown= downState.nodown.getStatenum();
                    //??????fragment?????????????????????
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
                Log.i(TAG, "  ??????????????????-----");
                //System.exit(0);
            }

            //?????????????????????????????????
            if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
               // linearLayoutManager.setScrollEnabled(true);
                if(isdown== downState.nodown.getStatenum()){

                    return false;
                }

            }
            //?????????????????????????????????
            if(keyCode==KeyEvent.KEYCODE_DPAD_UP){
              //  linearLayoutManager.setScrollEnabled(true);
                if(isdown== downState.nodown.getStatenum()){
                    return false;
                }
            }
            // ????????????
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
        //??????????????????
        Log.i("PZR"+TAG,"onDestroy");
        handler.removeCallbacks(runnable);
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    /*
    * onResume??????
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
     * ???????????????????????????
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

        //item???????????????Recyclerview????????????????????????????????????Recyclerview???????????????????????????
        final int dx = childLeft - parentLeft - ((parentRight - view.getWidth()) / 2);

        //??????
        final int dy = childTop - parentTop - (parentBottom - view.getHeight()) / 2;
        out[0] = dx;
        out[1] = dy;
        return out;


    }
    /**
     * ????????????????????????
     */
    public  class ArrayFilter extends Filter {

        private ArrayList<Contact> mOriginalValues;
        private final Object mLock = new Object();


        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            //???????????????
            FilterResults results = new FilterResults();
            //???????????????????????????????????????????????????????????????
            if (mOriginalValues == null) {
                synchronized (mLock) {
                    mOriginalValues = new ArrayList<>(mData_contacts);
                }
            }
            //?????????????????????
            if (prefix == null || prefix.length() == 0) {
                ArrayList<Contact> list;
                synchronized (mLock) {//????????????????????????????????????
                    list = new ArrayList<>(mOriginalValues);
                }
                results.values = list;
                //???????????????results???????????????????????????????????????
                results.count = list.size();
                //???????????????
                //contacts_search=contactsHelper.searchContacts(etInput.getText().toString().trim());

            } else {
                //???????????????
                String prefixString = prefix.toString().toLowerCase();
                //???????????????
                //contacts_search=contactsHelper.searchContacts(etInput.getText().toString().trim());

                ArrayList<Contact> values;
                //????????????????????????????????????
                synchronized (mLock) {
                    values = new ArrayList<>(mOriginalValues);
                }

                final int count = values.size();
                final ArrayList<Contact> newValues = new ArrayList<>();

                for (int i = 0; i < count; i++) {
                    //???List<Contact>?????????Contact??????
                    final Contact value = values.get(i);
                    //Contact?????????phonename???????????????????????????
                    final String valueText = value.getNumber().toString().toLowerCase();
                    //Contact?????????name???????????????????????????
                    final String valueTexts = value.getName().toString().toLowerCase();
                    if (valueText.startsWith(prefixString) || valueText.indexOf(prefixString.toString()) != -1 || valueTexts.startsWith(prefixString) || valueTexts.indexOf(prefixString.toString()) != -1) {//???????????????????????????
                        newValues.add(value);//?????????item????????????????????????
                    } else {//????????????????????????
                        final String[] words = valueText.split(" ");
                        final int wordCount = words.length;

                        for (int k = 0; k < wordCount; k++) {
                            //????????????????????????break?????????for??????
                            if (words[k].startsWith(prefixString)) {
                                newValues.add(value);
                                break;
                            }
                        }
                    }
                }
                //?????????results????????????????????????
                results.values = newValues;
                results.count = newValues.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            //????????????
            mData_contacts.clear();
            recyclerView_contact.setItemViewCacheSize(0);
           //   List<Contact> mData_contactss = new ArrayList<Contact>();
            //??????????????????
            mData_contacts.addAll((Collection<? extends Contact>) results.values);

            if(results.count>0) {
                //??????????????????????????????
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
     * ??????SIM?????????????????????
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
     * ??????SIM????????????
     */
    class MyPhoneStateListener extends PhoneStateListener {


        private static final String TAG = "ZTY_MyPhoneStateListener";
        //???????????????????????????wifi
        int disConnect;
        /**
         * ????????????SIM????????????
         * @param signalStrength
         */
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

                L.i(TAG, " signalStrength--------:" + signalStrength.getGsmSignalStrength());
                //???????????????????????????asu???
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
         * ????????????????????????
         * @param state
         */
        @Override
        public void onDataConnectionStateChanged(int state) {

                switch (state) {
                    case TelephonyManager.DATA_DISCONNECTED://????????????
                        signal.setBackgroundResource(0);
                        signal_data.setBackgroundResource(0);
                        disConnect=0;
                        break;
                    case TelephonyManager.DATA_CONNECTING://??????????????????
                        break;
                    case TelephonyManager.DATA_CONNECTED://???????????????
                        disConnect=2;
                        //?????????????????????5G??????4G
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
                        //??????????????????
                        int networkType = telephonyManager.getNetworkType();
                        if (networkType == TelephonyManager.NETWORK_TYPE_NR) {//5G??????
                            Log.i(TAG, "5G");
                            signal.setBackgroundResource(R.drawable.ic_status_bar_5g);

                        } else if (networkType == TelephonyManager.NETWORK_TYPE_LTE) {//4G??????
                            Log.i(TAG, "4G");
                            signal.setBackgroundResource(R.drawable.ic_status_bar_4g);
                        }
                        break;
                }

//            }

        }

        /**
         * ?????????????????????
         * @param direction
         */
        @Override
        public void onDataActivity(int direction) {


                //?????????????????????
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
     * ????????????????????????
     */
    public void checkWifiState() {

        WifiManager mWifiManager = (WifiManager) getBaseContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
        int wifi = mWifiInfo.getRssi();//??????wifi????????????
        if (wifi > -50 && wifi < 0) {//??????
//            Log.e(TAG, "----------??????");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_3);
        } else if (wifi > -70 && wifi < -50) {//??????
//            Log.e(TAG, "-----------??????");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_2);
        } else if (wifi > -80 && wifi < -70) {//??????
//            Log.e(TAG, "----------??????");
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_1);
        } else if (wifi > -100 && wifi < -80) {//??????
            iv_sign.setBackgroundResource(R.drawable.ic_wifi_0);
//            Log.e(TAG, "----------??????");
        }

    }

    /**
     * p???????????????????????????
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
     * ???????????????
     * @return
     */
    public boolean isPluggedIn(){
        Context context = getBaseContext();

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();
        for (int i = 0; i < networkInfo.length; i++) {
            if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED) {
            //    Log.i(TAG, "?????????-------------");
                return true;
            }
        }
//        String state= execCommand("cat /sys/class/net/eth0/carrier");
//        if(state.trim().equals("1")){  //????????????????????????1??????????????????0
//            Log.i(TAG,"?????????-------------");
//            return true;
//        }
//        Log.i(TAG,"?????????-------------");
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
