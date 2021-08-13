package com.example.tclphone.phonebook.fragment;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tclphone.MyApplication;
import com.example.tclphone.R;
import com.example.tclphone.RscInitHelper;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.phonebook.ContactsActivity;
import com.example.tclphone.phonebook.EditContactActivity;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.utils.L;
import com.juphoon.cmcc.lemon.MtcCallDb;
import com.juphoon.rcs.call.module.JusCallDelegate;
import com.juphoon.rcs.login.module.JusLoginDelegate;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLNavigationItem;
import com.tcl.uicompat.TCLToast;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Fragment配置类
 */
public class FirstFragment extends Fragment implements View.OnFocusChangeListener, View.OnClickListener{


    private static String TAG = "ZTY_FF";


    int[] photos = new int[]{R.drawable.ic_popup_avatar_nor,R.drawable.ic_popup_avatar_focus};

    //页面的控件
    private TextView name;
    private TextView phone_number;
    private TextView photo;
    private AllCellsGlowLayout buttPhone;
    private AllCellsGlowLayout editContacts;
    private Contact contact ;

    public FirstFragment() {

    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {   // 不在最前端显示 相当于调用了onPause();
            return;
        }else{  // 在最前端显示 相当于调用了onResume();
            //网络数据刷新
        }
    }

    /**
     * 初始化存参数
     * @param contact

     */
    public FirstFragment(Contact contact) {
        this.contact = new Contact();
        this.contact= contact;
        L.i(TAG, "contact--------: "+contact.getId());
        L.i(TAG, "contact--------: "+contact.getNumber());
        L.i(TAG, "name--------: "+contact.getName());
        String names=contact.getName();
        this.contact.setId(contact.getId());
        if("".equals(names)||names==null){
            contact.setName("");
        }else{
            this.contact.setName(names);
        }
        this.contact.setPhonenumber(contact.getNumber());

    }

    public static boolean  onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == event.KEYCODE_DPAD_UP) {

        }
        return false;
    }


    /**
     * 创建fragment视图View
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        //加载内容页面的布局文件(将内容页面的XML布局文件转成View类型的对象)
        View view=inflater.inflate(R.layout.contact_datails,container,//内容页面的布局文件
                false);//false表示需要手动调用addView方法将view添加到contain


        //true表示不需要手动调用addView方
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        //获取内容页面当中控件
        name=view.findViewById(R.id.contact_name);
        phone_number=view.findViewById(R.id.con_list_details);
        photo=view.findViewById(R.id.peo_photo_contact_details);
        buttPhone=view.findViewById(R.id.buttPhone);
        //将控件存入字段
        name.setText(contact.getName());
        phone_number.setText(contact.getNumber());

        //自动聚焦到视频通话中
        AllCellsGlowLayout buttAudio=view.findViewById(R.id.buttVideo);
        buttAudio.setFocusable(true);
        buttAudio.requestFocus();
        buttAudio.setFocusableInTouchMode(true);


        //监听phone按钮操作
        view.findViewById(R.id.buttPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = contact.getNumber();

                    //语音通话
                if(number.isEmpty()||number.length()== 0){
                    TCLToast.makeText(getActivity(),"拨打失败，请输入正确的电话号码",TCLToast.LENGTH_SHORT).show();
                }else {
                    //视频通话
                    tclCall(number, false);

                }

            }
        });
        view.findViewById(R.id.buttVideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = contact.getNumber();
                    //视频通话
                if(number.isEmpty()||number.length()== 0){
                    TCLToast.makeText(getActivity(),"拨打失败，请输入正确的电话号码",TCLToast.LENGTH_SHORT).show();
                }else {
                    tclCall(number, true);

                }

            }
        });

        //监听编辑键
        view.findViewById(R.id.editContacts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), EditContactActivity.class);
                intent.putExtra("id",contact.getId());
                intent.putExtra("name",contact.getName());
                intent.putExtra("phone_number", contact.getNumber());
                intent.putExtra("storage",contact.getStorage());
                intent.putExtra("photo",contact.getPhoto());
                intent.putExtra("type",1);
//              getActivity().onBackPressed();

                Log.i("PZR"+TAG,"ContactsActivity.finish()");
              //  getActivity().finish();
                getActivity().startActivity(intent);
            }
        });
        return view;

    }
    /**
     * 跳转到编辑界面
     * @param view
     */
    public void edit_contact(View view) {

        //跳转到编辑界面，并传入需要的数据字段



    }



    //是否是紧急通话
    public boolean checkEmergencyCall(String number){
        for (String s : RecordConstants.EMERGENCY_CALL)
        {
            if(s.equals(number))
            {
                Log.i(TAG,number+"是紧急通话");
                return true;
            }
        }
        Log.i(TAG,number+"不是紧急通话");
        return false;
    }

    public void tclCall(String number,Boolean b){

        TelephonyManager tm = (TelephonyManager) getContext().getSystemService(Service.TELEPHONY_SERVICE);
        int state=tm.getSimState();
        if(RscInitHelper.getInstance().getIsInit() && RcsLoginManager.isLogined()){

            Log.i(TAG,"RcsLoginManager.isLogined() : true");
            if(checkEmergencyCall(number)){
                TCLToast.makeText(getActivity(),"当前版本不支持紧急通话",TCLToast.LENGTH_SHORT).show();
            }else {
                String peerNumber = "tel:" + number;
                JusCallDelegate.call(peerNumber, b);
                getActivity().finish();
            }

        }else if(state != TelephonyManager.SIM_STATE_READY){
            Log.i(TAG,"未插入SIM卡，无法发起呼叫");
            TCLToast.makeText(getActivity(),"未插入SIM卡，无法发起呼叫",TCLToast.LENGTH_SHORT).show();
        }else if(!RscInitHelper.getInstance().getIsInit()){
            Log.i(TAG,"sdk加载失败");
            TCLToast.makeText(getActivity(),"加载sdk失败，无法发起呼叫",TCLToast.LENGTH_SHORT).show();
        }else if (!MyApplication.getInstance().isReady()) {
            Log.i(TAG, "SIM卡异常");
            TCLToast.makeText(getActivity(), "SIM卡异常", TCLToast.LENGTH_SHORT).show();
        }else {
            Log.i(TAG,"登录失败");
            TCLToast.makeText(getActivity(),"登录失败，无法发起呼叫",TCLToast.LENGTH_SHORT).show();
        }

    }






    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }
    @Override
    public void onClick(View view) {

    }

    protected static final int ANIMATION_DURATION = 200;

    protected static final float FOCUS_SCALE = 1.1f;

    protected static final float NORMAL_SCALE = 1.0f;

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (null == v){
            return;
        }
        if (hasFocus) {
            v.bringToFront();
            v.animate().scaleX(FOCUS_SCALE).scaleY(FOCUS_SCALE).setDuration(ANIMATION_DURATION).start();
            v.setSelected(true);
        } else {
            v.animate().scaleX(NORMAL_SCALE).scaleY(NORMAL_SCALE).setDuration(ANIMATION_DURATION).start();
            v.setSelected(false);
        }
    }



}