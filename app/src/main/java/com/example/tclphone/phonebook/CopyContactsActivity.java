package com.example.tclphone.phonebook;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.phonebook.copyadapter.RecyclerAdapter;
import com.example.tclphone.phonebook.copyadapter.RecyclerAdapterPhone;
import com.example.tclphone.phonebook.copyadapter.contact_copy;
import com.example.tclphone.utils.L;
import com.tcl.uicompat.TCLNavigationItem;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 *  复制联系人到其他位置
 *  2020-8-26
 */
public class CopyContactsActivity extends BaseActivity implements View.OnClickListener  {

    String key = "tcl.tv5g";//加密唯一标识

    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    //日志打印标识
    private static final String TAG = "ZTY_CopyContactsActivity";
    private Intent intent;

    //SIM卡列表参数
    private RecyclerView sim_recyclerView;
    private List<contact_copy> sim_listCheck;
    //SIM卡复制适配器
    RecyclerAdapter sim_adapter;

    TCLNavigationItem copy_devert;
    TCLNavigationItem copy_devert2;
    TCLNavigationItem  copy_return;
    //复制类SIM列表声明
    private List<contact_copy> sim_mData_contacts=new ArrayList<contact_copy>();

    private CheckBox simcheck;
    private CheckBox phonecheck;

    private List<Integer> sim_nums=new ArrayList();//sim选中的选项号列表

    private List<String> sim_name=new ArrayList();//sim选中的姓名列表
    private List<String> sim_phnumber=new ArrayList();//sim选中的手机号列表
    private List<Integer> sim_photo=new ArrayList();//sim卡选中的头像列表

    //本地列表参数
    private RecyclerView phone_recyclerView;
    private List<contact_copy> phone_listCheck;
    //本地卡复制适配器
    RecyclerAdapterPhone phone_adapter;
    //复制类本地列表声明
    private List<contact_copy> phone_mData_contacts=new ArrayList<contact_copy>();
    //本地中的选项号列表
    private List<Integer> local_nums=new ArrayList();

    private List<String> local_name=new ArrayList();//手机列表选中的姓名列表
    private List<String> local_phnumber=new ArrayList();//手机选中的手机号列表
    private List<Integer> local_photo=new ArrayList();//手机选中的手机号列表
    protected Cursor mCursor = null;
    private String mString = "";
    boolean iscopy_sim=true;//是否复制到sim卡
    boolean iscopy_local=true;//是否复制到本地卡

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_copy_contacts);
        //初始化sim卡列表
        initviewssim();
        //初始化手机卡列表
        initphone();

    }
    /**
     * 初始化sim卡列表
     */
    private void initviewssim() {


        copy_devert=findViewById(R.id.copy_devert);
        copy_devert2=findViewById(R.id.copy_devert2);
        copy_devert.setMiddleFocusStatus(true);

        copy_devert.setTextContent("复制");
        copy_devert2.setTextContent("复制");
        copy_devert2.setMiddleFocusStatus(true);

        copy_return=findViewById(R.id.copy_return);
        copy_return.setTextContent("返回");
        copy_return.setMiddleFocusStatus(true);


        sim_recyclerView = (RecyclerView) findViewById(R.id.recycler_sim);
        sim_recyclerView.setHasFixedSize(true);
       // simcheck=findViewById(R.id.item_checkbox);
        //设置RecyclerView布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        sim_recyclerView.setLayoutManager(linearLayoutManager);
        sim_listCheck=new ArrayList<>();

        //读取SIM卡数据库中的数据列表
        loadContact_sim();

        sim_adapter = new RecyclerAdapter(sim_mData_contacts,this,sim_recyclerView);
        sim_recyclerView.setAdapter(sim_adapter);
        //设置分分割线
        sim_recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        sim_adapter.setOnItemListener(new RecyclerAdapter.OnItemClickListener() {
            private contact_copy e;

            @Override
            public void setOnItemClick(int position,boolean isCheck) {

            }
            @Override
            public boolean setOnItemLongClick(int position) {
                return true;
            }

            @Override
            public void setOnItemCheckedChanged(int position, boolean isCheck) {

            }

            //按确认键监听的操作
            @Override
            public void setOnClick(int position) {
                //获取当前按键的位置
                e=sim_mData_contacts.get(position);
                L.i(TAG, "initviewssim: e" +e);
                //对位置的背景颜色进行改变
                if (e.getIsshow() == true) {
                    e.setIsshow(false);
                }else{
                    e.setIsshow(true);
                }
                //刷新布局
                sim_adapter.notifyDataSetChanged();
                int id=e.getId();
                String name=e.getName();
                String phonmber=e.getPhonenumber();
                L.i(TAG, "id: --------:" +id);
                L.i(TAG, "phonmber: --------:" +phonmber);
                L.i(TAG, "name: --------:" +name);
                int storage=e.getStorage();
                int photo=e.getPhoto();
                //将选中的位置添加到选项列表中，方便数据库操作
                local_nums.add(id);
                sim_name.add(name);
                sim_phnumber.add(phonmber);
                sim_photo.add(photo);
                L.i(TAG, "sim_name: --------:" +sim_name.size());
                L.i(TAG, "local_nums: --------:" +local_nums.size());
            }

            @Override
            public void setKey(int position, KeyEvent keyEvent) {

            }

        });

    }

    /**
     * 初始化手机卡列表
     */
    private void initphone() {
        phone_recyclerView = (RecyclerView) findViewById(R.id.recycler_phone);
        phone_recyclerView.setHasFixedSize(true);
        //设置RecyclerView布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        phone_recyclerView.setLayoutManager(linearLayoutManager);
        phone_listCheck=new ArrayList<>();

        //从数据库中加载本地的数据
        loadContact_local();

        phone_adapter = new RecyclerAdapterPhone(phone_mData_contacts,this,phone_recyclerView);
        phone_recyclerView.setAdapter(phone_adapter);

        //设置分分割线
        phone_recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
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
                e=phone_mData_contacts.get(position);
                L.i(TAG, "initphone: e" +e);
                //对位置的背景颜色进行改变
                if (e.getIsshow() == true) {
                    e.setIsshow(false);
                }else{
                    e.setIsshow(true);
                }
                //刷新布局
                phone_adapter.notifyDataSetChanged();
                int id=e.getId();
                String name=e.getName();
                String phonmber=e.getPhonenumber();
                L.i(TAG, "phonmber: --------:" +phonmber);
                L.i(TAG, "name: --------:" +name);

                int photo=e.getPhoto();
                //将选中的位置添加到选项好列表中，方便数据库操作
                sim_nums.add(id);
                local_name.add(name);
                local_phnumber.add(phonmber);
                local_photo.add(photo);
                L.i(TAG, "local_name: --------:" +local_name.size());
            }

            @Override
            public void setKey(int position, KeyEvent keyEvent) {

            }


        });
    }
    /**
     * Sim卡联系人列表
     *
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
        }
    }

    /**
     * sim复制到本地
     * @param
     */
    public void sim_local(View v){
        iscopy_local=true;
        Set<Contact> repeatList = new HashSet<Contact>();//用于存放重复的元素的list
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogOnClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    //确认按钮
                    case DialogInterface.BUTTON_POSITIVE:
                        if(phone_mData_contacts.size()!=0) {
                            //判断本地是否有sim卡中的联系人
                            for (int j = 0; j < local_nums.size(); j++) {
                                iscopy_local=true;
                                 for (int k = 0; k < phone_mData_contacts.size(); k++) {
                                    if (sim_phnumber.get(j).equals(phone_mData_contacts.get(k).getNumber())) {
                                        iscopy_local=false;
                                        break;
                                    }
                                }
                                if(iscopy_local){
                                    contactsHelper.addContacts(sim_name.get(j), sim_phnumber.get(j), 0, sim_photo.get(j));
                                }
                            }
                        }else{
                            for (int j = 0; j < local_nums.size(); j++) {
                                contactsHelper.addContacts(sim_name.get(j), sim_phnumber.get(j), 0, sim_photo.get(j));
                            }
                        }
                        Intent intent = new Intent(getBaseContext(), CopyContactsActivity.class);
                        intent.putExtra("type", "1");
                        startActivity(intent);
                        finish();
                        break;
                    //取消按钮
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;

                }
            }
        };

        builder.setTitle("复制SIM卡联系人到本地");
        builder.setMessage("确定要复制吗？");
        builder.setPositiveButton("确定",dialogOnClick);
        builder.setNegativeButton("取消",dialogOnClick);
        builder.create().show();

    }

    /**
     * 本地复制到sim卡
     * @param v
     */
    public void local_sim(View v){

        L.i(TAG, "onClick: peerNumber  local_sim" +sim_nums);
        iscopy_sim=true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        DialogInterface.OnClickListener dialogOnClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i) {
                    //确认按钮
                    case DialogInterface.BUTTON_POSITIVE:
                        //判断sim中是否有手机卡中的联系人
                        if(sim_mData_contacts.size()!=0) {
                            for (int j = 0; j < sim_nums.size(); j++) {
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
                            for (int j = 0; j < sim_nums.size(); j++) {
                                    insertSim(local_name.get(j), local_phnumber.get(j));
                                    contactsHelper.addContacts(local_name.get(j), local_phnumber.get(j), 1, local_photo.get(j));
                            }
                        }

                        copy_success();
                        //跳转回去
                        Intent intent = new Intent(getBaseContext(), CopyContactsActivity.class);
                        intent.putExtra("type", "1");
                        startActivity(intent);
                        finish();
                        break;
                    //取消按钮
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };
        builder.setTitle("复制本地联系人到SIM卡");
        builder.setMessage("确定要复制吗？");
        builder.setPositiveButton("确定",dialogOnClick);
        builder.setNegativeButton("取消",dialogOnClick);
        builder.create().show();
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

    /**
     * 返回按钮到主界面按钮
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void on_return(View v) throws JSONException, IOException {

        Intent intent = new Intent(getBaseContext(), ContactsActivity.class);
        intent.putExtra("type", "1");
        startActivity(intent);
        finish();

    }

    /**
     * 复制成功
     * @param
     */
    public void copy_success(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("复制成功");
        builder.setCancelable(true);
        final AlertDialog dlg =builder.create();
        dlg.show();
    }


    @Override
    public void onClick(View v) {

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
            startActivity(intent);
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
}
