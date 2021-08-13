package com.example.tclphone.phonebook;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.MainActivity;
import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.phonebook.copyadapter.RecyclerAdapter;
import com.example.tclphone.phonebook.copyadapter.RecyclerAdapterPhone;
import com.example.tclphone.phonebook.copyadapter.contact_copy;
import com.example.tclphone.phonebook.util.CenterLayoutManager;
import com.example.tclphone.phonebook.util.SimpleItemDecoration;
import com.example.tclphone.utils.L;
import com.tcl.uicompat.TCLDialog;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactsCopyActivity extends BaseActivity implements View.OnClickListener {

    //日志打印标识
    private static final String TAG = "ZTY_ContactsCopyActivity";
    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    TCLNavigationItem local_list;//本机列表
    TCLNavigationItem sim_list;//SIM卡列表

    //本地列表参数
    private RecyclerView phone_recyclerView;
    //本地卡复制适配器
    RecyclerAdapterPhone phone_adapter;
    //复制类本地列表声明
    private List<contact_copy> phone_mData_contacts=new ArrayList<contact_copy>();
    //本地中的选项号列表
    private List<Integer> local_nums=new ArrayList();
    private List<String> local_name=new ArrayList();//手机列表选中的姓名列表
    private List<String> local_phnumber=new ArrayList();//手机选中的手机号列表
    private List<Integer> local_photo=new ArrayList();//手机选中的手机号列表
    boolean iscopy_sim=true;//是否复制到sim卡

    //Sim卡中的联系人
    private List<contact_copy> sim_mData_contacts=new ArrayList<contact_copy>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.contact_copy);

        //初始化参数
        init();

    }

    /**
     * 初始化参数
     */
    private void init() {


        local_list = findViewById(R.id.local_list);
        local_list.requestFocus();
        local_list.setTextContent("本地");
        local_list.setMiddleFocusStatus(true);
        local_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_LEFT){
                    sim_list.setFocusable(true);
                }else{
                    sim_list.setFocusable(false);
                }
                return false;
            }
        });

        sim_list = findViewById(R.id.sim_list);
        sim_list.setTextContent("SIM卡");
        sim_list.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //跳转到联系人主界面
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(ContactsCopyActivity.this, SIMCopyActivity.class);
                    Log.i(TAG, "onSignalStrengthsChanged: 跳转界面，并清空栈顶界面");
                    startActivity(intent);
                }
            }
        });
        sim_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_LEFT){
                    sim_list.setFocusable(true);
                }else{
                    sim_list.setFocusable(false);
                }
                return false;
            }
        });
        initlocal();
        loadContact_sim();
    }


    /**
     * 本地的列表
     */
    private void initlocal() {

        phone_recyclerView = (RecyclerView) findViewById(R.id.recycler_phones);
        phone_recyclerView.setHasFixedSize(true);
        //设置RecyclerView布局管理器
        LinearLayoutManager linearLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        phone_recyclerView.setLayoutManager(linearLayoutManager);

        //从数据库中加载本地的数据
        loadContact_local();

        phone_adapter = new RecyclerAdapterPhone(phone_mData_contacts,this,phone_recyclerView);
        phone_recyclerView.setAdapter(phone_adapter);

        //设置分分割线
        phone_recyclerView.addItemDecoration(new SimpleItemDecoration());

        phone_adapter.setOnItemListener(new RecyclerAdapterPhone.OnItemClickListener() {
            private contact_copy e;
            @Override
            public void setOnItemClick(int position,boolean isCheck) {
            }
            @Override
            public boolean setOnItemLongClick(int position) {
                return true;
            }

            //监听确认按键
            @Override
            public void setOnClick(int position) {
                //获取当前按键的位置
//                e=phone_mData_contacts.get(position);
//                L.i(TAG, "initphone: e" +e);
//                //对位置的背景颜色进行改变
//                if (e.getIsshow() == true) {
//                    e.setIsshow(false);
//                }else{
//                    e.setIsshow(true);
//                }
//                //刷新布局
//                phone_adapter.notifyDataSetChanged();
//                int id=e.getId();
//                String name=e.getName();
//                String phonmber=e.getPhonenumber();
//                L.i(TAG, "phonmber: --------:" +phonmber);
//                L.i(TAG, "name: --------:" +name);
//
//                int photo=e.getPhoto();
//                //将选中的位置添加到选项好列表中，方便数据库操作
//                local_nums.add(id);
//                local_name.add(name);
//                local_phnumber.add(phonmber);
//                local_photo.add(photo);
//                L.i(TAG, "local_name: --------:" +local_name.size());
            }

            @Override
            public void setKey(int position,KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN){
                    if(phone_mData_contacts.size()>=6) {
                        sim_list.setFocusable(false);
                        local_list.setFocusable(false);
                    }
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP){
                    local_list.setFocusable(true);
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
                    local_list.setFocusable(false);
                }
            }
        });
    }


    /**
     * 本地联系人列表
     *
     */
    private void loadContact_local() {
        //从数据库中获取本地卡的联系人列表
        List<Person> contacts=contactsHelper.loadAllLocalityContacts();
        //遍历联系人列表添加到声明的表中
        for(int i=0;i<contacts.size();i++){
            String name =contacts.get(i).getName();
            int id=contacts.get(i).getId();
            String phoneNumber = contacts.get(i).getPhoneNumber();
            int gender=contacts.get(i).getPhoto();
            phone_mData_contacts.add(new contact_copy(id,name,phoneNumber,gender));
            local_nums.add(id);
            local_name.add(name);
            local_phnumber.add(phoneNumber);
            local_photo.add(gender);
        }
    }

    /**
     * 全部复制
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void copy_local(View v) throws JSONException, IOException {

        iscopy_sim=true;
        final TCLDialog.Builder builder = new TCLDialog.Builder(ContactsCopyActivity.this);
        builder.setTitle("复制本地联系人到SIM卡").setContent("确定要复制吗？");
        builder.setLeftButton("确 定", new TCLDialog.OnClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                //判断sim中是否有手机卡中的联系人
                if(sim_mData_contacts.size()!=0) {
                    for (int j = 0; j < phone_mData_contacts.size(); j++) {
                        iscopy_sim=true;
                        for (int k = 0; k < sim_mData_contacts.size(); k++) {
                            if (local_phnumber.get(j).equals(sim_mData_contacts.get(k).getPhonenumber())) {
                                iscopy_sim=false;
                                break;
                            }
                        }
                        if(iscopy_sim){
                            insertSim(local_name.get(j), local_phnumber.get(j));
                            contactsHelper.addContacts(local_name.get(j), local_phnumber.get(j), 1, local_photo.get(j));
                        }
                    }
                }else{
                    for (int j = 0; j < phone_mData_contacts.size(); j++) {
                        insertSim(local_name.get(j), local_phnumber.get(j));
                        contactsHelper.addContacts(local_name.get(j), local_phnumber.get(j), 1, local_photo.get(j));
                    }
                }
                new TCLToast.Builder(ContactsCopyActivity.this)
                        .setText("复制成功")
                        .setDuration(TCLToast.LENGTH_LONG)
                        .show();

                //跳转回去
                Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
                intent.putExtra("type", "1");
                intent.putExtra("isReturn",1);
                startActivity(intent);
                finish();

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

    /**
     * 加载SIM卡中的联系人
     */
    private void loadContact_sim() {
        //从数据库中获取本地卡的联系人列表
        List<Person> contacts=contactsHelper.loadAllSimContacts();
        //遍历联系人列表添加到声明的表中
        for(int i=0;i<contacts.size();i++){
            String name =contacts.get(i).getName();
            int id=contacts.get(i).getId();
            String phoneNumber = contacts.get(i).getPhoneNumber();
            int gender=contacts.get(i).getPhoto();
            sim_mData_contacts.add(new contact_copy(id,name,phoneNumber,gender));
        }
    }
    /**
     * 添加SIM卡联系人
     * @param name
     * @param phonenumber
     */
    public void insertSim(String name,String phonenumber){
        Uri uri = Uri.parse("content://icc/adn");
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put("tag", name);
        contentValues.put("number", phonenumber);
        Uri insertUri = contentResolver.insert(uri, contentValues);
        L.i(TAG, "insertSim----------------------------OK");

    }

    @Override
    public void onClick(View view) {

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

        }else if(keyCode==KeyEvent.KEYCODE_BACK){
            Intent intent=new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setClass(this, ContactsActivity.class);
            intent.putExtra("type", "1");
            intent.putExtra("isReturn",1);
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
