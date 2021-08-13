package com.example.tclphone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;


import androidx.annotation.RequiresApi;

import com.example.tclphone.litepal.LitePal;
import com.example.tclphone.litepal.LitePalApplication;
import com.example.tclphone.utils.PropertyUtils;
import com.tcl.areaconfiger.HardwareInfo;
import com.tcl.ff.component.animer.glow.view.utils.AnimerConfig;
import com.tcl.uicompat.util.TCLThemeUtils;
import com.tcl.walleve.wallevehelperhot.WalleveApi;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class MyApplication extends LitePalApplication {
    @SuppressLint("StaticFieldLeak")
    private static MyApplication myApplication;

    private static final String TAG = "PZR_application";

    private LinkedList<Activity> aList;

    private int type = 0;

    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private String is_walleve;


    private boolean isNetworkUp=false;
    private boolean isServiceRunning = false;
    private int initSDKResult = -1;
    public boolean isSupport5G;

    private boolean isConvert = false;

    int countActivities = 0;

    public boolean isRead = false;

    public boolean isReady = true;

    public boolean isCursor = false;

    @Override
    public void onCreate() {
        super.onCreate();
        aList = new LinkedList<>();
        myApplication = this;
        mContext = this;

        Log.i(TAG,"onCreate start service!");
       // decryptedFile("app/src/main/assets/liteps.xml","app/src/main/assets/litepal.xml");
        //初始化远场语音api
        WalleveApi api = WalleveApi.getInstance();
        api.init(this);

        LitePal.initialize(this);
        TCLThemeUtils.setCustomTheme(this, R.style.UI_5_AppTheme);
        TCLThemeUtils.initRegisterActivity(this);
        AnimerConfig.init(this);
        AnimerConfig.setGlowAnimerSwitchValue(true);

//      startService(new Intent(getApplicationContext(),CallService.class));
        isSupport5G= HardwareInfo.getInstance(mContext).isSupport5G();
        if (isSupport5G)
        {
            if (Build.VERSION.SDK_INT >= 26) {
                Log.i(TAG,"Application start service!");
                startForegroundService(new Intent(getApplicationContext(), CallService.class));
            }else {
                Log.i(TAG,"Application start service!");
                startService(new Intent(getApplicationContext(), CallService.class));
            }
        }

    }

    public static MyApplication getInstance(){
        return myApplication;
    }

    public static Context getContext(){

        return mContext;
    }

    public boolean isConvert() {
        return isConvert;
    }

    public void setConvert(boolean convert) {
        isConvert = convert;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public boolean isCursor() {
        return isCursor;
    }

    public void setCursor(boolean cursor) {
        isCursor = cursor;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIs_walleve() {
        return is_walleve;
    }

    public void setIs_walleve(String is_walleve) {
        this.is_walleve = is_walleve;
    }

    public boolean getIsServiceRunning()
    {
        return this.isServiceRunning;
    }

    public void setIsServiceRunning(boolean b)
    {
        this.isServiceRunning = b;
    }


    public boolean getIsNetworkUp() {
        return this.isNetworkUp;
    }

    public void setIsNetworkUp(boolean is_receive) {
        this.isNetworkUp = is_receive;
    }

    public int getInitSDKResult(){
        return this.initSDKResult;
    }

    public void setInitSDKResult(int i){
        this.initSDKResult=i;
    }

    @Override
    public void onTerminate() {
        Log.i(TAG,"Application terminated!");
        super.onTerminate();
    }

    public void addActivity(Activity activity){

        if(!aList.contains(activity)){
            countActivities = countActivities + 1;
            aList.add(activity);
            Log.i(TAG,"Activity的个数" + countActivities);
        }
    }

    public void removeActivity(Activity activity){
        if(aList.contains(activity)){
            countActivities = countActivities - 1;
            aList.remove(activity);
            Log.i(TAG,"Activity的个数" + countActivities);
        }
    }

    public void removeAllActivity(){
        for(Activity activity : aList){
            countActivities = countActivities - 1;
            activity.finish();
            Log.i(TAG,"Activity的个数" + countActivities);
        }
        aList.clear();

    }


    @SuppressWarnings("static-access")
    //文件加密的实现方法
    public static void encryptFile(String fileName, String encryptedFileName){
        try {
            FileInputStream fis = new FileInputStream(fileName);
            FileOutputStream fos = new FileOutputStream(encryptedFileName);

            //秘钥自动生成
            KeyGenerator keyGenerator=KeyGenerator.getInstance("AES");
            keyGenerator.init(128);
            Key key=keyGenerator.generateKey();


            byte[] keyValue=key.getEncoded();

            fos.write(keyValue);//记录输入的加密密码的消息摘要

            SecretKeySpec encryKey= new SecretKeySpec(keyValue,"AES");//加密秘钥

            byte[] ivValue=new byte[16];
            Random random = new Random(System.currentTimeMillis());
            random.nextBytes(ivValue);
            IvParameterSpec iv = new IvParameterSpec(ivValue);//获取系统时间作为IV

            fos.write("MyFileEncryptor".getBytes());//文件标识符

            fos.write(ivValue);	//记录IV
            Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
            cipher.init(cipher.ENCRYPT_MODE, encryKey,iv);

            CipherInputStream cis=new CipherInputStream(fis, cipher);

            byte[] buffer=new byte[1024];
            int n=0;
            while((n=cis.read(buffer))!=-1){
                fos.write(buffer,0,n);
            }
            cis.close();
            fos.close();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @SuppressWarnings("static-access")
    //文件解密的实现代码
    public static void decryptedFile(String encryptedFileName, String decryptedFileName){

        try {
            FileInputStream fis = new FileInputStream(encryptedFileName);
            FileOutputStream fos = new FileOutputStream(decryptedFileName);

            byte[] fileIdentifier=new byte[15];

            byte[] keyValue=new byte[16];
            fis.read(keyValue);//读记录的文件加密密码的消息摘要
            fis.read(fileIdentifier);
            if(new String (fileIdentifier).equals("MyFileEncryptor")){
                SecretKeySpec key= new SecretKeySpec(keyValue,"AES");
                byte[] ivValue= new byte[16];
                fis.read(ivValue);//获取IV值
                IvParameterSpec iv= new IvParameterSpec(ivValue);
                Cipher cipher = Cipher.getInstance("AES/CFB/PKCS5Padding");
                cipher.init(cipher.DECRYPT_MODE, key,iv);
                CipherInputStream cis= new CipherInputStream(fis, cipher);
                byte[] buffer=new byte[1024];
                int n=0;
                while((n=cis.read(buffer))!=-1){
                    fos.write(buffer,0,n);
                }
                cis.close();
                fos.close();
            }else{
            }
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }  catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
