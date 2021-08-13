package com.example.tclphone.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.juphoon.rcs.call.module.JusCallDelegate;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.uicompat.TCLToast;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TvTclCallService extends Service {
    private static final String TAG = "Mem_CC TvTclCallService";

   // private DBHelper dbHelper;
    private SQLiteDatabase db;

    MyContactsHelper myContactsHelper=new MyContactsHelper();
    public TvTclCallService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //super.onCreate();
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.O){
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            //数字是随便写的“40”，
            nm.createNotificationChannel(new NotificationChannel("40", "App Service", NotificationManager.IMPORTANCE_DEFAULT));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "40");

            //其中的2，是也随便写的，正式项目也是随便写
            startForeground(2 ,builder.build());
        }
        Log.i(TAG, "onCreate");
    }
    //服务执行的操作
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.w(TAG, "onStartCommand: RcsLoginManager.isLogined()"+ RcsLoginManager.isLogined());
        Log.i(TAG, "onStartCommand");
        if(!RcsLoginManager.isLogined()){
            TCLToast.makeText(this, "未登录", TCLToast.LENGTH_SHORT).show();
            onDestroy();
        }
        Bundle personname = intent.getExtras();
        String name = personname.getString("query");
        String domi = personname.getString("domain");
        Log.i(TAG, "onStartCommand name "+name);
        Log.i(TAG, "onStartCommand domi "+domi);
        String save = null;
        boolean isVideo = false;

        isVideo = true;
        Log.i(TAG, "onStartCommand isVideo "+isVideo);
        if(name.contains("给")){
            save = name.substring(name.indexOf("给")+1);
            Log.i(TAG, "onStartCommand: save "+save);
            boolean check = checkStrIsNum(save);
            Log.i(TAG, "onStartCommand: check="+check);
            if(check == true){
                save = "tel:" + save;
                JusCallDelegate.call(save, isVideo);
            }
            else{
                String result = Query(save);
                if(result.equals("")){
                    TCLToast.makeText(this, "没有此联系人"+save, TCLToast.LENGTH_SHORT).show();
                }else  {
                    result = "tel:" + result;
                    JusCallDelegate.call(result, isVideo);
                }
            }
        }else {
            boolean check = checkStrIsNum(name);
            Log.i(TAG, "onStartCommand: check="+check);
            if(check == true){
                name = "tel:" + name;
                JusCallDelegate.call(name, isVideo);
            }else{
                String result = Query(name);
                if(result.equals("")){
                    TCLToast.makeText(this, "没有此联系人"+name, TCLToast.LENGTH_SHORT).show();
                }else  {
                    result = "tel:" + result;
                    JusCallDelegate.call(result, isVideo);
                }
            }

        }


        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }
    public String Query(String name){
        Log.i(TAG, "Query: name="+name);
        String result="";
        List<Person> list= myContactsHelper.phoneByName(name);
        try {
             result=list.get(0).getPhoneNumber();
        }catch (Exception e){
             result="";
        }


      //  dbHelper = new DBHelper(getBaseContext(),1);
        //得到数据库
      //  db = dbHelper.getWritableDatabase();
//        Cursor cursor= db.query("contactTable",null,null,null,null,null,null);
//        Cursor cursor2=db.rawQuery("select * from contactTable",null);
//        String phone = null;
//        String test = "%"+name+"%";
//        String queryy = new StringBuilder().append("select * from contactTable where name = '")
//                .append(name).append("'").toString();


        ///Cursor cur = db.query("contactTable", new String[]{"name,phoneNumber,gender"},"name like ?", new String[]{test},null,null,null, String.valueOf(0));
//        Cursor cur = db.rawQuery(queryy,null);
//        if(cur.moveToNext()){
//            Log.i(TAG, "Query: cur.moveToNext() "+cur.moveToNext());
//            do{
//                String na = cur.getString(cur.getColumnIndex("name"));
//                String gender = cur.getString(cur.getColumnIndex("gender"));
//                phone = cur.getString(cur.getColumnIndex("phoneNumber"));
//                Log.i(TAG, "name:" + na);
//                Log.i(TAG, "content:" + gender);
//                Log.i(TAG, "phone:" + phone);
//
//            }while(cur.moveToNext());
//        }
//        Cursor cur= db.query("contactTable",null,null,null,null,null,null);
//        if(cur.moveToFirst()){
//            do{
//                String na = cur.getString(cur.getColumnIndex("name"));
//                String gender = cur.getString(cur.getColumnIndex("gender"));
//                phone = cur.getString(cur.getColumnIndex("phoneNumber"));
//                Log.i(TAG, "name:" + na);
//                Log.i(TAG, "content:" + gender);
//                Log.i(TAG, "phone:" + phone);
//                if(na.equals(name)){
//                    Log.i(TAG, "Query: phone ="+phone);
//                    return phone;
//                }
//            }while(cur.moveToNext());		}else phone = "";
//        cur.close();
//        db.close();
//        Log.i(TAG, "Query: phone ="+phone);
        return result;
    }
    private static Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
    /**
     * 利用正则表达式来判断字符串是否为数字
     */
    public static boolean checkStrIsNum(String str) {
        String bigStr;
        try {
            /** 先将str转成BigDecimal，然后在转成String */
            bigStr = new BigDecimal(str).toString();
        } catch (Exception e) {
            /** 如果转换数字失败，说明该str并非全部为数字 */
            return false;
        }
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        if (!isNum.matches() || str.length()!=11) {
            return false;
        }
        return true;
    }

}
