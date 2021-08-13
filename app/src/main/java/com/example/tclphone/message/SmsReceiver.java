package com.example.tclphone.message;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;


import com.example.tclphone.MyApplication;
import com.example.tclphone.WelcomeActivity;
import com.example.tclphone.db.MessageHelper;
import com.example.tclphone.db.Phone;
import com.example.tclphone.db.PhoneHelper;
import com.example.tclphone.phonebook.ContactsActivity;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.utils.L;
import com.example.tclphone.utils.PropertyUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SmsReceiver extends BroadcastReceiver {

    private Handler handler;

    private static final String TAG = "SmsReceiver";
    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    //时间戳
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //短信的数据库操作类
    MessageHelper messageHelper=new MessageHelper();

    PhoneHelper phoneHelper=new PhoneHelper();
    String phonenumber;
    String contents;
    int nums=0;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.i(TAG, "onReceive: action = " + action);
        //判断广播消息
        if (action.equals(SMS_RECEIVED_ACTION)){
            Bundle bundle = intent.getExtras();
            //如果不为空
            if (bundle!=null){
                //将pdus里面的内容转化成Object[]数组
                Object pdusData[] = (Object[]) bundle.get("pdus");// pdus ：protocol data unit  ：
                //解析短信
                SmsMessage[] msg = new SmsMessage[pdusData.length];
                for (int i = 0;i < msg.length;i++){
                    byte pdus[] = (byte[]) pdusData[i];
                    msg[i] = SmsMessage.createFromPdu(pdus);
                }
                StringBuffer content = new StringBuffer();//获取短信内容
                StringBuffer phoneNumber = new StringBuffer();//获取地址
                //分析短信具体参数
                for (SmsMessage temp : msg){
                    content.append(temp.getMessageBody());
                    phoneNumber.append(temp.getOriginatingAddress());
                }
                String result_phone=trimStr(phoneNumber.toString(),"+86");
                //接收时间
                Date date = new Date(System.currentTimeMillis());
                String timeStart = sdf.format(date);

                phonenumber=result_phone;
                contents=content.toString();
                Log.i(TAG, "onReceive: 收到短信：");
                String local="";
                if(result_phone.equals("10086")){
                    local= ContactsActivity.GetPhoneNumberFromSMSText(contents);
                    if(local.length()==11){
                        phoneHelper.addPhones(local);
                        PropertyUtils.set("com.tcl.android.phone", local);
                    }
                    if(MyApplication.getInstance().getType()==20){
                        convert_network4G_5G();
                        MyApplication.getInstance().setType(0);
                    }
                }else if(result_phone.equals("10001")){
                    local= ContactsActivity.GetPhoneNumberFromSMSText(contents);
                    if(local.length()==11){
                        phoneHelper.addPhones(local);
                        PropertyUtils.set("com.tcl.android.phone", local);
                    }
                    if(MyApplication.getInstance().getType()==20){
                        convert_network4G_5G();
                        MyApplication.getInstance().setType(0);
                        MyApplication.getInstance().setConvert(false);
                    }
                }
                //存入数据库
                messageHelper.addMessage(result_phone,"",0,1,content.toString(),timeStart,0);

            }
        }


    }
    public void convert_network4G_5G(){
        Class<?> c = null;
        int NETWORK_MODE_LTE_ONLY = 23;
        try {
            L.i(TAG, "begin---");
            c = Class.forName("android.telephony.TelephonyManager");
            if(c == null){
                L.i(TAG, "c==null--");
            }
            L.i(TAG, "begin---1");
            Constructor cons = c.getDeclaredConstructor();
            cons.setAccessible(true);
            TelephonyManager telephonyManager = (TelephonyManager) cons.newInstance();
            L.i(TAG, "begin---2");
            Method set = c.getMethod("setPreferredNetworkType", int.class, int.class);
            L.i(TAG, "begin---3");
            set.invoke(telephonyManager,23,23);
            L.i(TAG, "NETWORK_MODE_LTE_ONLY---"+NETWORK_MODE_LTE_ONLY);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public static String trimStr(String str, String indexStr){
        if(str == null){
            return null;
        }
        StringBuilder newStr = new StringBuilder(str);
        if(newStr.indexOf(indexStr) == 0){
            newStr = new StringBuilder(newStr.substring(indexStr.length()));//开头
        }else if(newStr.indexOf(indexStr) == newStr.length() - indexStr.length()){
            newStr = new StringBuilder(newStr.substring(0,newStr.lastIndexOf(indexStr)));

        }
        return newStr.toString();
    }


}
