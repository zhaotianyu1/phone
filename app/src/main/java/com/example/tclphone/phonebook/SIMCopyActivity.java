package com.example.tclphone.phonebook;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.example.tclphone.phonebook.copyadapter.RecyclerAdapter;
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

public class SIMCopyActivity  extends BaseActivity implements View.OnClickListener{

    //日志打印标识
    private static final String TAG = "SIMCopyActivity";
    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    TCLNavigationItem local_list;//本机列表
    TCLNavigationItem sim_list;//SIM卡列表

    //SIM卡列表参数
    private RecyclerView sim_recyclerView;
    private List<String> sim_name=new ArrayList();//sim选中的姓名列表
    private List<String> sim_phnumber=new ArrayList();//sim选中的手机号列表
    private List<Integer> sim_photo=new ArrayList();//sim卡选中的头像列表
    private List<Integer> sim_nums=new ArrayList();//sim选中的选项号列表
    //复制类SIM列表声明
    private List<contact_copy> sim_mData_contacts=new ArrayList<contact_copy>();
    //SIM卡复制适配器
    RecyclerAdapter sim_adapter;
    boolean iscopy_local=true;//是否复制到本地卡

    //本地列表声明
    private List<contact_copy> phone_mData_contacts=new ArrayList<contact_copy>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //防止截屏攻击
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.sim_copy);

        //初始化参数
        init();

    }
    /**
     * 初始化参数
     */
    private void init() {

        local_list = findViewById(R.id.local_list);
        local_list.setTextContent("本地");
        local_list.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    //跳转到联系人主界面
                    Intent intent = new Intent();
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(SIMCopyActivity.this, ContactsCopyActivity.class);
                    Log.i(TAG, "onSignalStrengthsChanged: 跳转界面，并清空栈顶界面");
                    startActivity(intent);
                }
            }
        });
        local_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_RIGHT){
                    local_list.setFocusable(true);
                }else{
                    local_list.setFocusable(false);
                }
                return false;
            }
        });

        sim_list = findViewById(R.id.sim_list);
        sim_list.setTextContent("SIM卡");
        sim_list.setMiddleFocusStatus(true);
        sim_list.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                int code = keyEvent.getKeyCode();
                if( code == KeyEvent.KEYCODE_DPAD_RIGHT){
                    local_list.setFocusable(true);
                }else{
                    local_list.setFocusable(false);
                }
                return false;
            }
        });
        initsim();
        loadContact_local();
    }

    /**
     * SIM卡的列表
     */
    private void initsim() {
        sim_recyclerView = (RecyclerView) findViewById(R.id.recycler_sims);
        sim_recyclerView.setHasFixedSize(true);

        //设置RecyclerView布局管理器
        LinearLayoutManager linearLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        sim_recyclerView.setLayoutManager(linearLayoutManager);


        //读取SIM卡数据库中的数据列表
        loadContact_sim();

        sim_adapter = new RecyclerAdapter(sim_mData_contacts,this,sim_recyclerView);
        sim_recyclerView.setAdapter(sim_adapter);

        //设置分分割线
        sim_recyclerView.addItemDecoration(new SimpleItemDecoration());

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
            @Override
            public void setKey(int position,KeyEvent keyEvent) {
                if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN){
                    if(sim_mData_contacts.size()>=6) {
                        sim_list.setFocusable(false);
                        local_list.setFocusable(false);
                    }
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    sim_list.setFocusable(true);
                }else if(keyEvent.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT){
                    sim_list.setFocusable(false);
                }
            }
            //按确认键监听的操作
            @Override
            public void setOnClick(int position) {
                //获取当前按键的位置
//                e=sim_mData_contacts.get(position);
//                L.i(TAG, "initviewssim: e" +e);
//                //对位置的背景颜色进行改变
//                if (e.getIsshow() == true) {
//                    e.setIsshow(false);
//                }else{
//                    e.setIsshow(true);
//                }
//                //刷新布局
//                sim_adapter.notifyDataSetChanged();
//                int id=e.getId();
//                String name=e.getName();
//                String phonmber=e.getPhonenumber();
//                L.i(TAG, "id: --------:" +id);
//                L.i(TAG, "phonmber: --------:" +phonmber);
//                L.i(TAG, "name: --------:" +name);
//                int storage=e.getStorage();
//                int photo=e.getPhoto();
//                //将选中的位置添加到选项列表中，方便数据库操作
//                sim_nums.add(id);
//                sim_name.add(name);
//                sim_phnumber.add(phonmber);
//                sim_photo.add(photo);
//                L.i(TAG, "sim_name: --------:" +sim_name.size());
            }

        });
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
            sim_nums.add(id);
            sim_name.add(name);
            sim_phnumber.add(phoneNumber);
            sim_photo.add(gender);
        }
    }

    /**
     * 全部复制
     * @param v
     * @throws JSONException
     * @throws IOException
     */
    public void copy_sim(View v) throws JSONException, IOException {
        iscopy_local=true;
        final TCLDialog.Builder builder = new TCLDialog.Builder(SIMCopyActivity.this);
        builder.setTitle("复制SIM卡联系人到本地").setContent("确定要复制吗？");
        builder.setLeftButton("确 定", new TCLDialog.OnClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                if(phone_mData_contacts.size()!=0) {
                    //判断本地是否有sim卡中的联系人
                    for (int j = 0; j < sim_nums.size(); j++) {
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
                    for (int j = 0; j < sim_nums.size(); j++) {
                        contactsHelper.addContacts(sim_name.get(j), sim_phnumber.get(j), 0, sim_photo.get(j));
                    }
                }
                new TCLToast.Builder(SIMCopyActivity.this)
                        .setText("复制成功")
                        .setDuration(TCLToast.LENGTH_LONG)
                        .show();

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
