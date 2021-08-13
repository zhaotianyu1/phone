package com.example.tclphone.phonebook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.icu.text.Transliterator;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.MainActivity;
import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.utils.L;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLButton;
import com.tcl.uicompat.TCLCheckBox;
import com.tcl.uicompat.TCLEditText;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import org.json.JSONException;

import java.io.IOException;
import java.security.Key;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 新增联系人Activity
 */
public class AddContactActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    private static final String TAG = "ZTY_AddContactActivity";
    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    //电话号码
    private TCLEditText getPhoneNum;
    //姓名
    private TCLEditText getName;

    //复选框SIM卡
    private TCLCheckBox getSIM;
    //复选框本地
    private TCLCheckBox getPhone;
    //单选按钮
    private RadioGroup mRadioGroupGender;

    //存储方式
    private int storage;
    private Intent intent;
    private RadioGroup gender;
    //前端传来的电话号码
    private String phonenumbers;

    TCLButton finishs;

    TCLButton  returns;
    AllCellsGlowLayout sims;

    ImageView sim_check;
    ImageView local_check;

//    private RadioButton mFemale;
//
//    private RadioButton mMale;
    //判断页面是从主界面还是联系人界面传过来，true为主界面，false为联系人界面
    private boolean isPage=false;

    int mHsfs;

    //存储位置的状态，0在本地，1在SIM卡，2在所有的位置,3为空
    public enum auditState {
        Phone(0),
        SIM(1),
        ALL(2),
        Other(3);
        private final int statenum;

        auditState(int statenum){
            this.statenum = statenum;
        }
        public int getStatenum() {
            return statenum;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_add_contact);
        initViews();

    }

    /**
     * 初始化界面
     */
    private void initViews(){

        sim_check = findViewById(R.id.sim_check);
        local_check = findViewById(R.id.local_check);

        finishs = findViewById(R.id.buttadd2);
        finishs.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==KeyEvent.KEYCODE_DPAD_LEFT || i== KeyEvent.KEYCODE_DPAD_RIGHT) {
                    finishs.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finishs.setFocusable(true);
                            finishs.setFocusableInTouchMode(true);
                            finishs.requestFocus();
                        } }, 1);
                }
                return false;
            }
        });
      //  returns = findViewById(R.id.buttadd3);
//        returns.setTextContent("返回");
//        returns.setMiddleFocusStatus(true);

        //初始化各个参数（从xml文件中取id）
        getPhoneNum = findViewById(R.id.textNum2);
        getName = findViewById(R.id.textName2);
        getPhoneNum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(i==KeyEvent.KEYCODE_DPAD_LEFT || i== KeyEvent.KEYCODE_DPAD_RIGHT){
//                    getPhoneNum.setFocusable(true);
//                    getPhoneNum.setFocusableInTouchMode(true);
//                    getPhoneNum.requestFocus();
                //    getName.setFocusable(false);
                    getPhoneNum.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getPhoneNum.setFocusable(true);
                            getPhoneNum.setFocusableInTouchMode(true);
                            getPhoneNum.requestFocus();
                        } }, 1);
                    L.i(TAG, "KEYCODE_DPAD_LEFT: ");
                }else{
                  //  getName.setFocusable(true);
                }
                return false;
            }
        });
        getPhoneNum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = getPhoneNum.getText().toString();
               if(str.length()>=20){
                   TCLToast toast = TCLToast.makeText(getBaseContext(),"最多支持20位号码", TCLToast.LENGTH_SHORT);
                   toast.setGravity(Gravity.CENTER,0,0);
                   toast.show();
               }
            }
        });
        getName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() ==KeyEvent.KEYCODE_DPAD_LEFT || keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT){
                    getName.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getName.setFocusable(true);
                            getName.setFocusableInTouchMode(true);
                            getName.requestFocus();
                        } }, 1);
                    L.i(TAG, "KEYCODE_DPAD_LEFT: ");
                }else{
                   // finishs.setFocusable(true);
                }
                return false;
            }
        });
        getSIM=findViewById(R.id.SIM);
        getPhone=findViewById(R.id.phone);
        int num = 6;
        getName.addTextChangedListener(new TextWatcher() {
            private CharSequence temp;
            private int selectionStart;
            private int selectionEnd;
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                temp = charSequence;
            }

            @Override

            public void afterTextChanged(Editable s) {

                int number = num - s.length();
                selectionStart = getName.getSelectionStart();
                selectionEnd = getName.getSelectionEnd();
                //删除多余输入的字（不会显示出来）
                if (temp.length() > num && isContainChinese(temp.toString())) {
                    s.delete(selectionStart - 1, selectionEnd);
                    getName.setText(s);
                    false_length();
                    //设置光标在最后
                    getName.setSelection(s.length());
                }else if (temp.length()>10){
                    s.delete(selectionStart - 1, selectionEnd);
                    getName.setText(s);
                    length_false();
                    //设置光标在最后
                    getName.setSelection(s.length());
                }


            }
        });
//        mFemale = findViewById(R.id.imaFemale);//女性头像
//        mMale = findViewById(R.id.imaMale);//男性头像


        //初始化编辑上的名称显示
        intent=getIntent();

        //初始聚焦输入手机号
        getPhoneNum.setFocusable(true);
        getPhoneNum.requestFocus();
        getPhoneNum.setFocusableInTouchMode(true);

        //获取页面跳转过来的电话字段
        String type=intent.getStringExtra("type");

        //判断页面是从主界面还是联系人界面传过来，联系人界面跳转为2
        if(type.equals("0")) {
            String  phonenumbers = intent.getStringExtra("phoneNumber");
            getPhoneNum.setText(phonenumbers);
            isPage=true;
        }
        L.i(TAG, "initViews: isPage " + isPage);

    }


    /**
     * 添加联系人
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void onAdd(View v) throws JSONException, IOException {

        //获取输入的联系电话
        String phonenumber = getPhoneNum.getText().toString().trim();

        //获取输入的姓名
        String name = getName.getText().toString().trim();


        //根据按钮选择头像类型，默认是2。
        int photo = 2;


        //初始化SIM卡
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //手机号/为空情况下需要重新填写
        if(phonenumber.isEmpty()||phonenumber.length()==0) {
            //抛出失败
            add_falses();
        }else if(name ==null ||name.equals("")){
            false_name();
        }else if(!isNumeric(phonenumber)){
            add_falses();
        } else if(!getSIM.isChecked() && !getPhone.isChecked()){
            //抛出失败
            add_false();
        }else if(tm.getSimState() != TelephonyManager.SIM_STATE_READY){//如果未查SIM卡
            if(getSIM.isChecked()){
                Log.i(TAG, "无SIM卡存储");
                sim_false();
            } else if(getPhone.isChecked()){
                Log.i(TAG, "无SIM卡存储本地");
                storage=0;//存在本机
                //添加联系人信息到数据库中
                contactsHelper.addContacts(name, phonenumber, storage, photo);
                add_success();
                //跳转到主界面
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //跳转到主界面
                        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                        intent.putExtra("type", "1");
                        startActivity(intent);
                        finish();
                    }
                },1000);//延时1s执行
            }else{
                Log.i(TAG, "无SIM卡存储本地2");
                storage=0;//存在本机;
                //添加联系人信息到数据库中
                contactsHelper.addContacts(name, phonenumber, storage, photo);
                add_success();

                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //跳转到主界面
                        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                        intent.putExtra("type", "1");
                        startActivity(intent);
                        finish();
                    }
                },1000);//延时1s执行

            }

        }
        //有插SIM卡
        else{
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                Log.i(TAG, "有SIM卡");
                //SIM卡和手机全选
                if(getSIM.isChecked() && getPhone.isChecked()){
                    storage=1;//存在SIM卡
                    insertSim(name,phonenumber);//调用系统SIM卡插入
                    contactsHelper.addContacts(name, phonenumber, storage, photo);//插入数据库
                    Log.i(TAG, "有SIM卡，全部存储------------------------1");
                    storage=0;//存在本地
                    //添加联系人信息到数据库中
                    contactsHelper.addContacts(name, phonenumber, storage, photo);//插入本地
                    Log.i(TAG, "有SIM卡，全部存储------------------------2");
                //手机选中
                }else if(getPhone.isChecked()){
                    storage=0;//存在本机
                    try {
                        contactsHelper.addContacts(name, phonenumber, storage, photo);//插入本地
                        Log.i(TAG, "storage------------------------"+ storage);
                        Log.i(TAG, "有SIM卡，存储成功");
                    }catch (Exception e){
                        Log.i(TAG, "有SIM卡，存储失败");
                        e.printStackTrace();
                    }
                    Log.i(TAG, "有SIM卡，全部存储------------------------3");
                }else if(getSIM.isChecked()){
                    storage=1;//存在SIM卡
                    insertSim(name,phonenumber);//调用系统SIM卡插入
                    contactsHelper.addContacts(name, phonenumber, storage, photo);//插入数据库
                    Log.i(TAG, "有SIM卡，全部存储------------------------4");
                }
            }
            //普通对话框，添加成功
            add_success();

             Timer timer = new Timer();
             timer.schedule(new TimerTask() {
                    @Override
                     public void run() {
                        //跳转到主界面
                        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                        intent.putExtra("type", "1");
                        startActivity(intent);
                        finish();
                                         }
                 },1000);//延时1s执行


        }

    }


    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }


    private void length_false() {
        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("姓名过长，仅支持最长11位名称");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },2000);

        TCLToast tclToast = new TCLToast(getBaseContext(),"姓名过长，本机仅支持最长10位英文名称",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);

    }

    private void false_name() {
        // TCLToast.makeText(getBaseContext(), "姓名过长，仅支持最长11位名称", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"姓名不能为空",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);

    }
    private void false_length() {
        // TCLToast.makeText(getBaseContext(), "姓名过长，仅支持最长11位名称", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"姓名过长，本机仅支持最长6位中文名称",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);

    }
    /**
     * 手机号码格式错误
     */
    private void add_falses() {
        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("手机号码格式错误，请重新输入");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },2000);
        TCLToast tclToast = new TCLToast(getBaseContext(),"手机号码格式错误，请重新输入",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);
       // TCLToast.makeText(getBaseContext(), "手机号码格式错误，请重新输入", TCLToast.LENGTH_SHORT).show();
    }

    /**
     * 添加到SIM卡中
     * @param name
     * @param phonenumber
     */
    public void insertSim(String name,String phonenumber){
        try {
            Uri uri = Uri.parse("content://icc/adn");
            ContentResolver contentResolver = getContentResolver();
            ContentValues contentValues = new ContentValues();
            if(name == null ||name.equals("")){
                name =phonenumber;
            }
            contentValues.put("tag", name);
            contentValues.put("number", phonenumber);
            Log.i(TAG, "name---------------------:"+name);
            Log.i(TAG, "number---------------------:"+phonenumber);
            Uri uri1 = contentResolver.insert(uri, contentValues);
            Log.i(TAG, "insert_uri---------------------:"+uri1);
            Log.i(TAG,"添加到系统SIM卡成功=-");
        }catch (Exception e){
            Log.i(TAG,"添加到系统SIM卡失败");
            e.printStackTrace();
        }
    }

    /**
     * 返回联系人列表按钮
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void onreturn(View v) throws JSONException, IOException {

        //成功后的跳转判断
        if(isPage) {
            Intent intent = new Intent(getBaseContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }else{
            Intent intent = new Intent(this, ContactsActivity.class);
            startActivity(intent);
            finish();
        }

    }
    /**
     * 普通对话框,无SIM卡
     * */
    public void is_false(){

        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("已存手机号码，无需添加");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },2000);
      //  TCLToast.makeText(getBaseContext(), "已存手机号码，无需添加", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"已存手机号码，无需添加",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);
    }
    /**
     * 普通对话框,无SIM卡
     * */
    public void sim_false(){

        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("无SIM卡");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },2000);
        //TCLToast.makeText(getBaseContext(), "无SIM卡", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"无SIM卡",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);
    }

    /**
     * 普通对话框,填写报错
     * */
    public void add_false(){

        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("存储位置错误，请重新输入");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },2000);
       // TCLToast.makeText(getBaseContext(), "存储位置错误，请重新输入", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"存储位置错误，请重新输入",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);
    }
    /**
     * 普通对话框,填写成功
     * */
    public void add_success(){

        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("添加成功");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },500);
       // TCLToast.makeText(getBaseContext(), "添加成功", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"添加成功",TCLToast.LENGTH_SHORT);
        tclToast.setGravity(Gravity.CENTER,0,0);
        tclToast.show();
        final Timer t =new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                tclToast.cancel();
                t.cancel();
            }
        },1000);

    }
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {

    }
    /**
     * 页面的按键监听
     * @param keyCode
     * @param event
     * @return true:代表屏蔽当前的按键，false代表解除被屏蔽的按键
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode==KeyEvent.KEYCODE_MENU){
//            Intent OtherIntent = new Intent();
//            OtherIntent.setClassName("com.tcl.settings","com.tcl.settings.ShowWindowService");
//            OtherIntent.putExtra("flag", 1);
//            OtherIntent.putExtra("Type", "Settings");
//            OtherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            OtherIntent.setAction("com.tcl.settings.SHOW_WINDOW");
//            startService(OtherIntent);

        }
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isPage) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                Log.i(TAG, "若前端传过来type值为2");
                //跳转页面的同时关闭当前页面.
                finish();
            } else{
                Intent intent = new Intent(this, ContactsActivity.class);
                intent.putExtra("isReturn",1);
                startActivity(intent);
                //跳转页面的同时关闭当前页面.
                Log.i(TAG, "跳转到联系人界面");
                finish();
            }

        }
        return super.onKeyDown(keyCode, event);
    }
    /**
     * 存入sim卡
     * @param view
     */
    public void sims(View view){
        if(!getSIM.isChecked()){
            getSIM.setChecked(true);
            sim_check.setVisibility(View.VISIBLE);
            sim_check.bringToFront();
           // getSIM.setBackgroundResource(R.drawable.sim_press);
        }else{
            getSIM.setChecked(false);
            sim_check.setVisibility(View.INVISIBLE);
           // getSIM.setBackgroundResource(R.drawable.sim);
        }
    }
    /**
     * 存入本地
     * @param view
     */
    public void locals(View view){
        if(!getPhone.isChecked()){
            getPhone.setChecked(true);
            local_check.setVisibility(View.VISIBLE);
            local_check.bringToFront();
          //  getPhone.setBackgroundResource(R.drawable.localcard_press);
        }else{
            getPhone.setChecked(false);
            local_check.setVisibility(View.INVISIBLE);
          //  getPhone.setBackgroundResource(R.drawable.localcard);
        }
    }

    /**
     * 男性
     * @param view
     */
    public void add_imaMales(View view){

//        if(mMale.isChecked()){
//            mMale.setChecked(false);
//            mMale.setBackgroundResource(R.drawable.malefocus);
//        }else{
//            mMale.setChecked(true);
//            mFemale.setChecked(false);
//            mMale.setBackgroundResource(R.drawable.malefocus2);
//            mFemale.setBackgroundResource(R.drawable.femalefocus);
//        }
    }
    /**
     * 女性
     * @param view
     */
    public void add_imaFemales(View view){
//        if(!mFemale.isChecked()){
//
//            mFemale.setChecked(true);
//            mMale.setChecked(false);
//            mFemale.setBackgroundResource(R.drawable.femalefocus2);
//            mMale.setBackgroundResource(R.drawable.malefocus);
//        }else{
//
//            mFemale.setChecked(false);
//            mFemale.setBackgroundResource(R.drawable.femalefocus);
//        }
    }


    /**
     * 私有方法，用于判断电话号码是否合法
     * @param str
     * @return
     */
    private boolean isContainChina(String str){
        //含有中文就false
        if(isContainChinese(str)){
            L.i("MyContactsHelper","电话号码含有中文！");
            return false;
        }

        //含有英文就false
        if(str.matches(".*[a-zA-z].*")){
            L.i("MyContactsHelper","电话号码含有英文！");
            return false;
        }

        return true;
    }
    /**
     * 私有方法，用于判断字符串内是否含有中文
     * @param str 输入的字符串
     * @return true-含有中文 false-不含中文
     */
    private boolean isContainChinese(String str){
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) return true;
        return false;
    }
    public int getChineseCount(String str){
        int count = 0;
        String regEx = "[\u4e00-\u9fa5]";//[a-zA-z_]
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        while(m.find())
        {
            count ++;
        }
        return count;

    }

}
