package com.example.tclphone.phonebook;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
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

import androidx.appcompat.app.AlertDialog;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.MainActivity;
import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.utils.L;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLButton;
import com.tcl.uicompat.TCLCheckBox;
import com.tcl.uicompat.TCLDialog;
import com.tcl.uicompat.TCLEditText;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import org.json.JSONException;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 编辑联系人Activity
 */
public class  EditContactActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    //加密唯一标识
    private String key = "tcl.tv5g";

    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    private static final String TAG = "ZTY_EditContactActivity";

    //电话号码
    private TCLEditText getPhoneNum;
    //姓名
    private TCLEditText getName;
    //头像
    private RadioGroup gender;
    //存储位置
    private int storage;
    //复选框SIM卡
    private TCLCheckBox getSIM;
    //复选框本地
    private TCLCheckBox getPhone;
    //单选按钮
    private RadioGroup mRadioGroupGender;

    private AllCellsGlowLayout sim_layout;
    private AllCellsGlowLayout local_layout;

    private ImageView local_check;
    private ImageView sim_check;
    private RadioButton mFemale;
    private RadioButton mMale;

    TCLButton finishs;

    TCLButton  returns;

    private Intent intent;

    private boolean isSim;
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


        setContentView(R.layout.activity_edit_contact);
//        if(ContactsActivity.instance!=null){
//            Log.i("PZR"+TAG,"ContactsActivity.instance.finish()");
//            ContactsActivity.instance.finish();
//        }

        initViews();//初始化参数

    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.i("PZR"+TAG,"EditContactActivity:onDestory");
        super.onDestroy();
    }

    /**
     * 初始化界面
     */
    private void initViews(){

        sim_layout = findViewById(R.id.sim_layout);
        local_layout = findViewById(R.id.local_layout);
        local_check = findViewById(R.id.local_check);
        sim_check = findViewById(R.id.sim_check);

        finishs = findViewById(R.id.edits);

        returns = findViewById(R.id.delete_edits);
//        returns.setTextContent("删除");
//        returns.setMiddleFocusStatus(true);

        //初始化各个参数（从xml文件中取id）
        getPhoneNum = findViewById(R.id.textNum2);
        getPhoneNum.requestFocus();
        getName = findViewById(R.id.textName2);
        getSIM=findViewById(R.id.SIM);
        getPhone=findViewById(R.id.phone);
//        mFemale =  findViewById(R.id.imaFemale);
//        mMale =  findViewById(R.id.imaMale);

        //初始化编辑上的名称显示
        intent=getIntent();

        //跳转获取姓名
        String name=intent.getStringExtra("name");
        //跳转获取电话号码
        String phonenumber=intent.getStringExtra("phone_number");

        //跳转获取存储方式
        int storage=intent.getIntExtra("storage",-1);
        //跳转获取照片
        int photo=intent.getIntExtra("photo",-1);

        //判断姓名是否等于电话号码，若相等，姓名置空
        if (!name.equals(phonenumber)){
            getPhoneNum.setText(phonenumber);
            getName.setText(name);
        }

        getPhoneNum.setText(phonenumber);
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

                selectionStart = getName.getSelectionStart();
                selectionEnd = getName.getSelectionEnd();
                if (temp.length() > 6 && isContainChinese(temp.toString())) {
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

        sim_layout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(storage == auditState.SIM.getStatenum()){
                        sim_check.bringToFront();
                        L.i(TAG, "sim_layout--------: ");
                    }
                }
            }
        });
        local_layout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b){
                    if(storage == auditState.Phone.getStatenum()){
                        local_check.bringToFront();
                        L.i(TAG, "local_layout--------: ");
                    }
                };
            }
        });

        //判断存储位置
        if(storage== auditState.SIM.getStatenum()){
            getSIM.setChecked(true);
            sim_check.setVisibility(View.VISIBLE);
            sim_check.bringToFront();
       //     getSIM.setBackgroundResource(R.drawable.sim_press);
            isSim=true;

        }else if(storage== auditState.Phone.getStatenum()) {
            getPhone.setChecked(true);
            local_check.setVisibility(View.VISIBLE);
            local_check.bringToFront();
         //   getPhone.setBackgroundResource(R.drawable.localcard_press);
            isSim = false;
        }

//        //判断头像,1为女性头像，0为男性头像
//        if(photo==1){
//            mFemale.setChecked(true);
//            mFemale.setBackgroundResource(R.drawable.femalefocus2);
//        }else if(photo==0){
//            mMale.setChecked(true);
//            mMale.setBackgroundResource(R.drawable.malefocus2);
//        }

    }



    /**
     * 完成回到联系人
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void onEdit_Add(View v) throws JSONException, IOException {

        //跳转获取姓名
        String old_name=intent.getStringExtra("name");
        //跳转获取电话号码
        String old_phonenumber=intent.getStringExtra("phone_number");

        //获取前端的id
        int id=intent.getIntExtra("id",-1);

        //获取前端联系电话
        String phonenumber = getPhoneNum.getText().toString();
        //获取前端的姓名
        String name =  getName.getText().toString();

        int photo = 2;
//        //若男性被选中
//        if(mMale.isChecked()){
//            photo=0;
//        }//若女性被选中
//        if(mFemale.isChecked()){
//            photo=1;
//        }
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        //手机号/姓名为空情况下需要重新填写
        if(phonenumber.isEmpty()||phonenumber.length()==0) {
            edit_false();
            Log.i(TAG, "------------------------------------");
        }else if(name == null ||name.equals("")){
            false_name();
        } else if(!getSIM.isChecked() && !getPhone.isChecked()){
            sim_false();
        }else if(tm.getSimState() != TelephonyManager.SIM_STATE_READY){
            if(getSIM.isChecked()){
                Log.i(TAG, "无SIM卡存储");
                sim_false();
            } else if(getPhone.isChecked()){
                Log.i(TAG, "无SIM卡存储本地");
                storage=0;//存在本机
                //添加联系人信息到数据库中
                contactsHelper.editContacts(id,name, phonenumber, storage, photo);
                edit_success();
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
                storage=0;//全部都存储;
                //添加联系人信息到数据库中
                contactsHelper.editContacts(id,name, phonenumber, storage, photo);
                edit_success();
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
            }
        }
        else{
            if (tm.getSimState() == TelephonyManager.SIM_STATE_READY) {
                Log.i(TAG, "有SIM卡");
                boolean is_SIM=contactsHelper.hasContacts_Sim(old_phonenumber);
                boolean is_local=contactsHelper.hasContacts_local(old_phonenumber);
                if(getSIM.isChecked() && getPhone.isChecked()){
                    //判断当前联系人的id，之前都存在于SIM卡和本地中
                        int storages=intent.getIntExtra("storage",-1);
                        if(storages==1){
                            updateSim(old_name, old_phonenumber, name, phonenumber);//更新系统SIM卡
                            contactsHelper.editContacts(id, name, phonenumber, storages, photo);
                            storage=0;
                            contactsHelper.addContacts(name, phonenumber, storage, photo);
                        }else if(storages==0) {
                            //添加联系人信息到数据库中
                            // int result_local=contactsHelper.loadContactByphone(old_phonenumber,storage);
                            insertSim(name, phonenumber);
                            storage = 1;
                            contactsHelper.addContacts(name, phonenumber, storage, photo);
                            contactsHelper.editContacts(id, name, phonenumber, storages, photo);
                            Log.i(TAG, "有SIM卡，全部存储2---------------------2");
                        }
                }else if(getPhone.isChecked()){
                    //判断当前联系人的id，之前是否存在于SIM卡中
                        int storages=intent.getIntExtra("storage",-1);
                        if(storages==0){
                            storage=0;
                            contactsHelper.editContacts(id,name, phonenumber, storage, photo);
                        }else if(storages==1){
                            storage=0;//存在本机
                            contactsHelper.addContacts(name, phonenumber, storage, photo);
                        }
                        Log.i(TAG, "有SIM卡，全部存储2---------------------6");

                }else if(getSIM.isChecked()){

                        //判断当前联系人的id，之前是否存在于SIM卡中
                        int storages=intent.getIntExtra("storage",-1);
                        if(storages==0){
                            insertSim(name,phonenumber);
                            storage=1;
                            contactsHelper.addContacts(name, phonenumber, storage, photo);
                        }else if(storages==1){
                            updateSim(old_name, old_phonenumber, name, phonenumber);//更新系统SIM卡
                            storage=1;
                            contactsHelper.editContacts(id,name, phonenumber, storage, photo);
                        }
                }
            }
            //普通对话框，添加成功
            edit_success();
            if(ContactsActivity.instance!=null){
                Log.i("PZR"+TAG,"ContactsActivity.instance.finish()");
                ContactsActivity.instance.finish();
            }

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    //跳转到主界面
                    Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                    intent.putExtra("type", "1");
                    intent.putExtra("isReturn",1);
                    startActivity(intent);
                    finish();
                }
            },1000);//延时1s执行

        }
    }

    private void length_false() {
        //声明普通的对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("姓名过长，请重新输入");
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
        //TCLToast.makeText(getBaseContext(), "姓名过长，请重新输入", TCLToast.LENGTH_SHORT).show();
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
     * 修改SIM卡中的联系人
     * @param oldName
     * @param oldPhone
     * @param newName
     * @param newPhone
     */
    public void updateSim(String oldName, String oldPhone, String newName,
                          String newPhone) {
        try {
            Uri uri = Uri.parse("content://icc/adn");
            Cursor cursor = this.getContentResolver().query(uri, null, null,
                    null, null);
            ContentValues values = new ContentValues();
            values.put("tag", oldName);
            values.put("number", oldPhone);
            values.put("newTag", newName);
            values.put("newNumber", newPhone);
            int update = this.getContentResolver().update(uri, values, null, null);
            Log.i(TAG, "update-------- "+update);
            if(update != 1) {
                if (oldName == null || oldName.equals("")) {
                    ContentValues value = new ContentValues();
                    value.put("tag", oldPhone);
                    value.put("number", oldPhone);
                    value.put("newTag", newName);
                    value.put("newNumber", newPhone);
                    int updates = getContentResolver().update(uri, value, null, null);
                    Log.i(TAG, "updates-------- "+updates);
                }
            }
        }catch (Exception e){
            Log.i(TAG, "系统SIM卡更新失败 ");
            e.printStackTrace();
        }

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
        Uri uri = Uri.parse("content://icc/adn");
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        if(name == null ||name.equals("")){
            name =phonenumber;
        }
        contentValues.put("tag", name);
        contentValues.put("number", phonenumber);
        Uri insertUri = contentResolver.insert(uri, contentValues);
    }
    /**
     * 删除联系人操作
     * */
//    public void on_delete(View v) {
//
//        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        //声明对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        DialogInterface.OnClickListener dialogOnClick = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                switch (i) {
//                    //确定删除按钮
//                    case DialogInterface.BUTTON_POSITIVE:
//                        if(tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
//                            //获取前端的id
//                            int id=intent.getIntExtra("id",-1);
//                            //调用数据库进行删除
//                            contactsHelper.deleteContacts(id);
//                            delete_success();
//
//                            Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    //跳转到主界面
//                                    Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
//                                    intent.putExtra("type", "1");
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            },1000);//延时1s执行
//                        }else{
//                            //跳转获取存储方式
//                            int storage=intent.getIntExtra("storage",-1);
//                            //跳转获取姓名
//                            String name=intent.getStringExtra("name");
//                            //跳转获取电话号码
//                            String phonenumber=intent.getStringExtra("phone_number");
//                            int id=intent.getIntExtra("id",-1);
//                            //判断当前联系人的id
//                            if(storage==1) {
//                                deleteContact(name, phonenumber);
//                                contactsHelper.deleteContacts(id);
//                            }else if(storage==0){
//                                contactsHelper.deleteContacts(id);
//                            }
//
//                            delete_success();
//                            Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    //跳转到主界面
//                                    Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                }
//                            },1000);//延时1s执行
//                        }
//                        break;
//                    //取消删除按钮
//                    case DialogInterface.BUTTON_NEGATIVE:
//                        break;
//                }
//            }
//        };
//        builder.setTitle("删除联系人");
//        builder.setMessage("确定要删除吗？");
//        builder.setPositiveButton("确定",dialogOnClick);
//        builder.setNegativeButton("取消",dialogOnClick);
//        builder.create().show();
//
//    }

    public void on_delete(View view){
        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        final TCLDialog.Builder builder = new TCLDialog.Builder(EditContactActivity.this);
        builder.setTitle("删除联系人").setContent("确定要删除吗？");
        builder.setLeftButton("确 定", new TCLDialog.OnClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                if(tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
                    //获取前端的id
                    int id=intent.getIntExtra("id",-1);
                    //调用数据库进行删除
                    contactsHelper.deleteContacts(id);
                    delete_success();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //跳转到主界面
                            Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("type", "1");
                            intent.putExtra("isReturn",1);
                            startActivity(intent);
                            finish();
                        }
                    },1000);//延时1s执行
                }else{
                    //跳转获取存储方式
                    int storage=intent.getIntExtra("storage",-1);
                    //跳转获取姓名
                    String name=intent.getStringExtra("name");
                    //跳转获取电话号码
                    String phonenumber=intent.getStringExtra("phone_number");
                    int id=intent.getIntExtra("id",-1);
                    //判断当前联系人的id
                    if(storage==1) {
                        SimDelete(name, phonenumber);
                        contactsHelper.deleteContacts(id);
                    }else if(storage==0){
                        contactsHelper.deleteContacts(id);
                    }

                    delete_success();
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            //跳转到主界面
                            Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.putExtra("isReturn",1);
                            startActivity(intent);
                            finish();
                        }
                    },1000);//延时1s执行
                }
                dialog.dismiss();
            }
        });
        builder.setRightButton("取 消", new TCLDialog.OnClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public void SimDelete(String name, String phoneNumber) {
        Log.i(TAG, "准备删除");
       Uri uri = Uri.parse("content://icc/adn");
        Cursor cursor = this.getContentResolver().query(uri, null, null,
                null, null);
        Log.i(TAG, ">>>>>> " + cursor.getCount());
        String where = "";
        if(name.equals("")){
            name = null;
             where = "number='" + phoneNumber + "'";
            Log.i(TAG, "name===:");
        }else{
            where = "tag='" + name + "'";
            where += " AND number='" + phoneNumber + "'";
        }
        int i =this.getContentResolver().delete(uri, where, null);
        Log.i(TAG, "phNumber--:"+phoneNumber +"is:" + i);
        if(i != 1){
            if(name == null ||name.equals("")) {
                String names = phoneNumber;
                String wheres = "tag='" + names + "'";
                wheres += " AND number='" + phoneNumber + "'";
                int deletes = getContentResolver().delete(uri, wheres, null);
                Log.i(TAG, "delete2---------------------: " + deletes);
            }
        }

    }
    //删除SIM卡中的联系人
    public void deleteContact(String name, String phone) {

        // 这种方式删除数据时不行，查阅IccProvider源码发现，在provider中重写的delete方法并没有用到String[]
        // whereArgs这个参数
        // int delete = getContentResolver().delete(uri,
        // " tag = ? AND number = ? ",
        String emails ="";
        String anrs = "";
        Uri uri = Uri.parse("content://icc/adn");
        String where = "tag='" + name + "'";
        where += " AND number='" + phone + "'";
        where += " AND emails='"+emails+ "'";
        where += " AND anrs='"+anrs+ "'";
        int delete = getContentResolver().delete(uri, where, null);
        Log.i(TAG, "delete1---------------------: "+delete);
        if(delete != 1){
            if(name == null ||name.equals("")) {
                String names = phone;
                String wheres = "tag='" + names + "'";
                wheres += " AND number='" + phone + "'";
                int deletes = getContentResolver().delete(uri, wheres, null);
                Log.i(TAG, "delete2---------------------: " + deletes);
            }
        }
    }

    /**
     * 普通对话框,编辑错误
     * */
    public void sim_false(){

        //声明对话框
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
       // TCLToast.makeText(getBaseContext(), "存储位置错误，请重新输入", TCLToast.LENGTH_SHORT).show();
    }

    /**
     * 普通对话框,编辑错误
     * */
    public void edit_false(){

        //声明对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("编辑失败，手机格式错误");
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
        TCLToast tclToast = new TCLToast(getBaseContext(),"编辑失败，手机格式错误",TCLToast.LENGTH_SHORT);
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
       // TCLToast.makeText(getBaseContext(), "编辑失败，手机格式错误", TCLToast.LENGTH_SHORT).show();
    }

    /**
     * 普通对话框,编辑成功
     * */
    public void edit_success(){

        //声明对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("修改成功");
//        builder.setCancelable(true);
//        final AlertDialog dlg =builder.create();
//        dlg.show();
        //TCLToast.makeText(getBaseContext(), "修改成功", TCLToast.LENGTH_SHORT).show();
        TCLToast tclToast = new TCLToast(getBaseContext(),"修改成功",TCLToast.LENGTH_SHORT);
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
      //  dlg.dismiss();
//        final TCLDialog.Builder builder = new TCLDialog.Builder(this);
//        builder.setContent("修改成功");
//        builder.show();


    }
    /**
     * 普通对话框,编辑成功
     * */
    public void delete_success() {

        //声明对话框
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage("删除成功");
//        builder.setCancelable(true);
//        final AlertDialog dlg = builder.create();
//        dlg.show();
//        final Timer t =new Timer();
//        t.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                dlg.dismiss();
//                t.cancel();
//            }
//        },500);
        TCLToast tclToast = new TCLToast(getBaseContext(),"删除成功",TCLToast.LENGTH_SHORT);
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
       // TCLToast.makeText(getBaseContext(), "删除成功", TCLToast.LENGTH_SHORT).show();
    }
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

    }

    /**
     * 返回键返回上一级
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //按键返回上一级
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //若前端传过来type值为2，则返回跳转到通话界面，否则回到联系人界面
            if(intent.getIntExtra("type",-1)==2) {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                Log.i(TAG, "若前端传过来type值为2--");
                //跳转页面的同时关闭当前页面.if
                finish();
            } else {
//                Intent intent = new Intent(this, ContactsActivity.class);
//                intent.putExtra("isReturn",1);
//                startActivity(intent);
//                //跳转页面的同时关闭当前页面.
//                Log.i(TAG, "跳转到通话界面");
//                finish();
            }

        }else if(keyCode==KeyEvent.KEYCODE_MENU){
//            Intent OtherIntent = new Intent();
//            OtherIntent.setClassName("com.tcl.settings","com.tcl.settings.ShowWindowService");
//            OtherIntent.putExtra("flag", 1);
//            OtherIntent.putExtra("Type", "Settings");
//            OtherIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            OtherIntent.setAction("com.tcl.settings.SHOW_WINDOW");
//            startService(OtherIntent);

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
          //  getSIM.setBackgroundResource(R.drawable.sim_press);
        }else{
            getSIM.setChecked(false);
            sim_check.setVisibility(View.GONE);
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
           // getPhone.setBackgroundResource(R.drawable.localcard_press);
        }else{
            getPhone.setChecked(false);
            local_check.setVisibility(View.GONE);
          //  getPhone.setBackgroundResource(R.drawable.localcard);
        }
    }

    /**
     * 男性
     * @param view
     */
    public void add_imaMale(View view){

        if(mMale.isChecked()){

            mMale.setChecked(false);
            mMale.setBackgroundResource(R.drawable.malefocus);
        }else{

            mMale.setChecked(true);
            mFemale.setChecked(false);
            mMale.setBackgroundResource(R.drawable.malefocus2);
            mFemale.setBackgroundResource(R.drawable.femalefocus);

        }
    }
    /**
     * 女性
     * @param view
     */
    public void add_imaFemale(View view){
        if(!mFemale.isChecked()){
            mFemale.setChecked(true);
            mMale.setChecked(false);
            mFemale.setBackgroundResource(R.drawable.femalefocus2);
            mMale.setBackgroundResource(R.drawable.malefocus);

        }else{
            mFemale.setChecked(false);
            mFemale.setBackgroundResource(R.drawable.femalefocus);

        }
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
            return true;
        }

//        //含有英文就false
//        if(str.matches(".*[a-zA-z].*")){
//            L.i("MyContactsHelper","电话号码含有英文！");
//            return false;
//        }
        return false;
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
}
