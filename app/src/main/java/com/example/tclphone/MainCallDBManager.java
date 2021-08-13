package com.example.tclphone;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.MyRecordsHelper;
import com.example.tclphone.dialog.TclCallActivity;
import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.rcs.call.module.JusCallActivity;
import com.juphoon.rcs.call.module.JusCallDelegate;
import com.juphoon.rcs.call.sdk.RcsAudioManager;
import com.juphoon.rcs.call.sdk.RcsCallSession;
import com.juphoon.rcs.call.sdk.bean.RcsCallMember;
import com.juphoon.rcs.call.sdk.delegate.RtpPacketDelegate;


import com.example.tclphone.litepal.LitePal;
import com.example.tclphone.litepal.LitePalBase;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MainCallDBManager {


    public static final String TAG= "PZR_DB";

    private static Boolean Flag = false;

    private static Boolean RtpPacketFlag = false;


    //更新通话纪录的时候，需要的中间缓存时间
    String tempTime;

    //存放纪录的id
    int recordId = 1;

    //通话是否接通Flag
    int statusFlag;


    private static Context mContext;
    private static MainCallDBManager mMainCallDBManager = new MainCallDBManager();

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    MyRecordsHelper myRecordsHelper = new MyRecordsHelper();

    public static MainCallDBManager getInstance() {
        return mMainCallDBManager;
    }

    private JusCallDelegate.JusCallListener jusCallListener= new JusCallDelegate.JusCallListener() {
        @Override
        public Class jusCallActivityClass() {
            //如果return null，则默认跳到JusCallActivity
//            return JusCallActivity.class;

            String result= PropertyUtils.get("com.tcl.AI.soundbox.state", "-1");
            MyApplication myApplication= (MyApplication) mContext.getApplicationContext();
            Log.i(TAG,"result是否为全场景模式: ------------- " +result);
            //若当前为全景模式
            if("1".equals(result)){
                myApplication.setIs_walleve(result);
                Log.i(TAG,"切换为全场景模式: ------------- ");
                rever();
            }else{
                myApplication.setIs_walleve(result);
                Log.i(TAG,"主界面: ------------- ");
            }

          return TclCallActivity.class;

        }

        //来电的一对一显示邀请，true表示一对一来电邀请，返回false，则标识标识屏蔽该文件邀请。默认返回true
        @Override
        public boolean jusCallIncomingReceived(RcsCallSession rcsCallSession) {
            return true;
        }

        //可添加一对一的来电的历史纪录
        @Override
        public void jusCallIncomingDone(RcsCallSession rcsCallSession) {

            Log.i(TAG,"startAudio begin");
            RcsAudioManager.startAudio(mContext);
            Log.i(TAG,"startAudio end");

            Log.i(TAG,"jusCallIncomingDone: "+rcsCallSession.getState());
            Log.i(TAG,"recordId : " +recordId);
            Date date = new Date(System.currentTimeMillis());
            String timeStart = sdf.format(date);
            tempTime = timeStart;
            String phoneName = rcsCallSession.getCallNumber();
            String duration = sdf.format(date);
            if(phoneName.contains("tel")) {
                phoneName = phoneName.substring(4);

            }

            int mode;
            if(rcsCallSession.isVideo()){
                mode = RecordConstants.VIDEO_INCOMMING;
            }else {
                mode = RecordConstants.AUDIO_INCOMMING;
            }
            int status = RecordConstants.DIALOG_FAILURE;

            statusFlag = status;

            try{
                recordId = myRecordsHelper.communicate(phoneName,timeStart,duration,mode,status);
                Log.i(TAG,"recordId : " +recordId);
            }catch (Exception e){
                e.printStackTrace();
                Log.i(TAG,""+e);
                Log.i(TAG,"CallIncoming failure");
            }
            Log.i(TAG,"CallIncoming insert success and recordId is "+ recordId);


        }


        //一对一呼出的历史纪录
        @Override
        public void jusCallOutgoing(RcsCallSession rcsCallSession) {
            Log.i(TAG,"jusCallOutgoing: "+rcsCallSession.getState());
            Log.i(TAG,"recordId : " +recordId);
            Date date = new Date(System.currentTimeMillis());
            String timeStart = sdf.format(date);
            tempTime = timeStart;
            String phoneName = rcsCallSession.getCallNumber();
            if(phoneName.contains("tel")){

                phoneName = phoneName.substring(4);

            }
            String duration = sdf.format(date);
            int mode;
            if(rcsCallSession.isVideo()){
                mode = RecordConstants.VIDEO_OUTGOING;
            }else {
                mode = RecordConstants.AUDIO_OUTGOING;
            }
            int status = RecordConstants.DIALOG_FAILURE;

            statusFlag = status;
            try{
                recordId = myRecordsHelper.communicate(phoneName,timeStart,duration,mode,status);
                Log.i(TAG,"recordId : " +recordId);
            }catch (Exception e){
                Log.i(TAG,""+e);
                Log.i(TAG,"CallOutgoing record insert failure");
            }
            Log.i(TAG,"CallOutgoing record insert success");

        }

        //一对一已经接通的历史纪录
        @Override
        public void jusCallTalking(RcsCallSession rcsCallSession) {
            Log.i(TAG,"jusCallTalking: "+rcsCallSession.getState());
            Log.i(TAG,"recordId : " +recordId);
            Date date = new Date(System.currentTimeMillis());
            String timeStart = sdf.format(date);
            tempTime = timeStart;
            String duration = sdf.format(date);
            int status = RecordConstants.DIALOG_SUCCESS;
            statusFlag = status;
            try{
                myRecordsHelper.editRecords(recordId,timeStart,duration,status);
                Log.i(TAG,"recordId : " +recordId);
            }catch (Exception e){
                Log.i(TAG,""+e);
                Log.i(TAG,"CallTalking failure!");
            }
            Log.i(TAG,"CallTalking success!");

        }


        //可添加未接通或者通话结束的历史纪录
        @Override
        public void jusCallTermed(RcsCallSession rcsCallSession) {
            Log.i(TAG,"CallTermed");
            Log.i(TAG,"recordId : " +recordId);
            Date date = new Date(System.currentTimeMillis());
            String timeStart = tempTime;
            String duration = sdf.format(date);

            try{
                myRecordsHelper.editRecords(recordId,timeStart,duration,statusFlag);
                Log.i(TAG,"recordId : " +recordId);
            }catch (Exception e){
                Log.i(TAG,""+e);
                Log.i(TAG,"termed failure!");
            }
            Log.i(TAG,"termed success!");
        }

        //
        @Override
        public void jusCallToConf(RcsCallSession rcsCallSession) {

        }

        @Override
        public String jusCallRingFilePath(RcsCallSession rcsCallSession) {
            return null;
        }

        @Override
        public int jusCallRingFileRawId(RcsCallSession rcsCallSession) {
            return R.raw.ringtone_a_journey;
        }

        @Override
        public RcsCallMember jusCallMemberWithNumber(String s) {
            return null;
        }

        @Override
        public boolean jusConfIncomingReceived(RcsCallSession rcsCallSession) {
            return false;
        }

        @Override
        public void jusConfIncomingDone(RcsCallSession rcsCallSession) {

        }

        @Override
        public void jusConfOutgoing(RcsCallSession rcsCallSession) {

        }

        @Override
        public void jusConfConned(RcsCallSession rcsCallSession) {

        }

        @Override
        public void jusConfDisced(RcsCallSession rcsCallSession) {

        }

        @Override
        public void jusConfIvtAcpt(RcsCallSession rcsCallSession, RcsCallMember rcsCallMember) {

        }

        @Override
        public void jusConfKickAcpt(RcsCallSession rcsCallSession, RcsCallMember rcsCallMember) {

        }

        @Override
        public void jusConfUpdt(RcsCallSession rcsCallSession, RcsCallMember rcsCallMember) {

        }
    };

    //音视频加密
    public void setRtpPacket() {

    }






    @SuppressLint("LongLogTag")
    public  void init(Context context){
        if(!Flag){
//            SQLiteDatabase.loadLibs(context);//加载加密数据库SQOCipher相关的库

            LitePal.getDatabase();;//若数据库没有创建，该语句将创建数据库
            Flag = true;
            mContext = context;
            JusCallDelegate.init(context);
            JusCallDelegate.setJusCallListener(jusCallListener);
        }
        if(!RtpPacketFlag){
            RtpPacketFlag = true;
            // 设置允许音频加解密
            RtpPacketDelegate.setRtpAudioEnable(true);
            // 设置允许视频加解密
            RtpPacketDelegate.setRtpVideoEnable(true);
            RtpPacketDelegate.setListener(new RtpPacketDelegate.RtpPacketListener() {
                @Override
                public void onSendRtpPacket(int id, String rmtAddr, byte[] pData) {
                    // TODO 加密发送数据
                    // RtpPacketDelegate.sendEncrypedPacket(id, rmtAddr, pEncrptedData);
                    RtpPacketDelegate.sendEncrypedPacket(id, rmtAddr, pData);// 透传
                }

                @Override
                public void onRecvRtpPacket(int id, boolean video, byte[] pData) {
                    // TODO 收到数据进行解密
                    // RtpPacketDelegate.recvDecrypedPacket(id, video, pDecrptedData);
                    RtpPacketDelegate.recvDecrypedPacket(id, video, pData); // 透传
                }
            });
        }
    }

    /**
     * 切换全场景
     */
    public  void rever(){
        String PACKAGE_NAME = "com.tcl.walleve";
        String SERVICE_NAME = "com.tcl.soundbox.SoundBoxMsgService";//com.tiidian.seed.service.SeedService
        Intent i = new Intent();
        i.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
        MyApplication.getContext().getApplicationContext().startService(i);
    }

}
