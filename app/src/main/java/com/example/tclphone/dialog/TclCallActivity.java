
package com.example.tclphone.dialog;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Drawable;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraAccessException;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.tclphone.BaseActivity;
import com.example.tclphone.MainActivity;
import com.example.tclphone.MyApplication;
import com.example.tclphone.R;
import com.example.tclphone.WelcomeActivity;
import com.example.tclphone.broadcastreceiver.BlueToothReceiver;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.MyRecordsHelper;

import com.example.tclphone.db.Person;

import com.example.tclphone.db.Records;
import com.example.tclphone.utils.PropertyUtils;
import com.juphoon.cmcc.lemon.MtcCall;
import com.juphoon.cmcc.lemon.MtcCallConstants;
import com.juphoon.cmcc.lemon.MtcRing;
import com.juphoon.cmcc.lemon.MtcRingConstants;
import com.juphoon.rcs.call.module.JusCallDelegate;
import com.juphoon.rcs.call.module.adapter.MultiCallGridAdapter;
import com.juphoon.rcs.call.module.bluetooth.MtcBluetoothHelper;
import com.juphoon.rcs.call.module.bluetooth.MtcHeadsetPlugReceiver;
import com.juphoon.rcs.call.module.enrichcall.XmlPreCall;
import com.juphoon.rcs.call.module.utils.JusOrientationListener;
import com.juphoon.rcs.call.module.utils.JusProximityUtils;
import com.juphoon.rcs.call.module.utils.JusRingUtils;
import com.juphoon.rcs.call.module.view.MultiCallView;
import com.juphoon.rcs.call.module.view.OneCallView;
import com.juphoon.rcs.call.module.view.Statistics;
import com.juphoon.rcs.call.sdk.CallReasonInfo;
import com.juphoon.rcs.call.sdk.RcsAudioManager;
import com.juphoon.rcs.call.sdk.RcsCallDefines;
import com.juphoon.rcs.call.sdk.RcsCallSession;
import com.juphoon.rcs.call.sdk.RcsVideoCallProvider;
import com.juphoon.rcs.call.sdk.bean.RcsCallMember;
import com.juphoon.rcs.call.sdk.listener.RcsCallSessionListener;
import com.juphoon.rcs.call.sdk.manager.RcsCallManager;
import com.juphoon.rcs.login.sdk.common.RcsConstants;
import com.juphoon.rcs.login.sdk.common.RcsDebug;
import com.juphoon.rcs.login.sdk.common.RcsNetworkHelper;
import com.juphoon.rcs.login.sdk.listener.RcsLoginListener;
import com.juphoon.rcs.login.sdk.manager.RcsLoginManager;
import com.tcl.cameraservice.TclCameraService;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.tosapi.camera.TvCameraApi;
import com.tcl.tvmanager.TvManager;
import com.tcl.uicompat.TCLButton;
import com.tcl.uicompat.TCLToast;
import com.tcl.uicompat.util.TCLThemeUtils;
import com.tcl.walleve.wallevehelperhot.WalleveApi;
import com.tcl.walleve.wallevehelperhot.api.IControlApi;
import com.tcl.walleve.wallevehelperhot.listener.control.ITtsListener;
import com.tcl.walleve.wallevehelperhot.listener.control.IVoiceRunningListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
import static android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;


/**
 * Created by JeffWang on 16/2/19.
 */
public class TclCallActivity extends BaseActivity implements RcsCallSessionListener,
        JusOrientationListener.Callback, RcsNetworkHelper.NetworkListener, RcsLoginListener, AdapterView.OnItemClickListener,
//        DoodleLayout.BackgroundProvider,OnDoodleStartListener,
        MtcHeadsetPlugReceiver.Callback,
        MtcBluetoothHelper.Callback {

    private static final String TAG = "PZR_TclCallActivity";//TclCallActivity.class.getSimpleName();

    private static final String VTAG = "VPZR";

    private TelephonyManager mTelephonyManager;
    private static final String JUS_INTENT_FROM_NOTIFICATION = "intent_from_notification";
    private static final int JUS_NOTIFICATION_ID = 1;
    private long mNotificationBase;

    private static final int REQUEST_PICK_PHONE = 100;

    //audio state
    private static final int AUDIO_RECEIVER = 0;
    private static final int AUDIO_HEADSET = 1;
    private static final int AUDIO_SPEAKER = 2;
    private static final int AUDIO_BLUETOOTH = 3;
    private static final int AUDIO_STRINGS[] = {
            R.string.receiver,
            R.string.headset,
            R.string.speaker};

    private static final int AUDIO_DRAWABLES[] = {
            R.drawable.call_audio_receiver,
            R.drawable.call_audio_headset,
            R.drawable.call_audio_speaker,
            R.drawable.call_audio_bluetooth};

    private RcsCallSession mCurSession;
    private RcsCallSession mAnotherSession;
    private long mStartTime;
    private long mAnotherStartTime;
    private MultiCallGridAdapter mMultiCallGridAdapter;
    private ArrayList<RcsCallMember> mMultiCallMemberList;

    private SurfaceView mRemoteView;
    private SurfaceView mLocalView;

    private OneCallView mOneCallView;
    private MultiCallView mMultiCallView;

    private View mViewStatistic;
    private Statistics mStatistics;
    private AlertDialog mWaitAlertDialog;
    private AlertDialog mBadConnectDialog;
    private AlertDialog mMediaErrorDialog;
    private JusOrientationListener mOrientationListener;
    private boolean isNeverTipBadStatus;
    private boolean isMediaErrorKeepCall;

    private boolean mHasShowOperation = true;
    private boolean mHasShowNotification = false;

    private boolean mIsConference;
    private boolean mHasConferenceHold;
    private int mKickingPartpIndex = -1;

    private AudioManager mAudioManager;

    private String mRingFilePath;
    private int mRingRawId;
    private TextView changeVideo;


    private String peerNumber = "";

    private ViewGroup mRoot;
    //    private DoodleButtonsView mDoodleButtonsView;
    private MtcBluetoothHelper mMtcBluetoothHelper;
    private MtcHeadsetPlugReceiver mHeadsetPlugReceiver;
    private int mAudio = 0;
    private AlertDialog mAlertDialog;
    private InComingStateListener listener;

    private TextView mAvatar;

    private boolean isIncoming = false;


    private AllCellsGlowLayout cancel;
    private ImageButton mCancel;
    private TextView mCancelText;

    TextView call_name;

    IControlApi controlApi;
    boolean isFarWalleveOpen;

    private AllCellsGlowLayout mAcceptVoice;
    private TextView mAcceptVoiceText;

    private AllCellsGlowLayout mAcceptVideo;
    private TextView mAcceptVideoText;

    private ImageButton call_mute;
    private TextView mute_text;

    private View one_call_view;

    private View audio_outgoing_call_view;
    private View audio_incoming_call_view;
    private View audio_talking_view;

    private View video_outgoing_call_view;
    private View video_incoming_call_view;
    private View video_talking_view;

    private TextView state_call;

    private LinearLayout linearLayout;

    private WindowManager wManager;
    private WindowManager.LayoutParams mParams;

    private boolean isMute = false;

    private volatile boolean mLocalViewIsRemove = false;

    private int currentAudio;
    private int systemAudio_20;
    private int callSystemAudio;
    private int preCallSystemAudio;

    private volatile boolean isAuthenticationOk = false;


    private boolean isCameraOn = false;

    private BlueToothReceiver mReceiver;

    private boolean isRecomingApplication = false;

    private int audioVol;

    private volatile boolean isCallIncomingVol = true;

    private boolean isOutgoingCallVideo;

    private OneCallView slocalView;



    public static final String START_ACTION = "CALL_STATE_RINGING";

    public static final String  PHONE_ACTION = "CALL_STATE_OFFHOOK";

    public static final String FINISH_ACTION = "CALL_STATE_IDLE";


    private ImageButton remove_video;
    private TextView remove_video_text;

    int[] photos = new int[] {R.drawable.phone_call_avatar};


    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        Window window = getWindow();
        WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;

        window.setAttributes(attrs);
        //防止截屏攻击
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE);
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent.getBooleanExtra(JUS_INTENT_FROM_NOTIFICATION, false)) {
            RcsDebug.log(TAG, "onCreate JUS_INTENT_FROM_NOTIFICATION");
            removeNotification();
            finish();
            return;
        }
        mIsConference = intent.getBooleanExtra(JusCallDelegate.EXTRA_IS_CONFERENCE, false);

        mRingFilePath = intent.getStringExtra(JusCallDelegate.EXTRA_RING_FILEPATH);
        mRingRawId = intent.getIntExtra(JusCallDelegate.EXTRA_RING_FILERAWID, 0);

        LayoutInflater inflater = getLayoutInflater();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        currentAudio = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        callSystemAudio= mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        preCallSystemAudio = (int) (0.4*callSystemAudio);

        systemAudio_20 = 20;

        audioVol = currentAudio;


        setContentView(R.layout.activity_tcl_call);

        mCancelText = findViewById(R.id.cancel_text);

        mAcceptVoice = findViewById(R.id.voice_accept);
        mAcceptVoiceText =  findViewById(R.id.voice_accept_text);
        mAcceptVideo = findViewById(R.id.video_accept);

        mute_text = findViewById(R.id.call_mute_text);

        audio_incoming_call_view = findViewById(R.id.audio_incoming_call);
        audio_outgoing_call_view = findViewById(R.id.audio_outgoing_call);
        audio_talking_view = findViewById(R.id.audio_call_talking);


        video_incoming_call_view = findViewById(R.id.video_incoming_call);
        video_outgoing_call_view = findViewById(R.id.video_outgoing_call);
        video_talking_view = findViewById(R.id.video_call_talking);

        one_call_view = findViewById(R.id.one_call_main);

        mAvatar = findViewById(R.id.avatar);
        call_name = findViewById(R.id.call_name);


        slocalView=findViewById(R.id.localSurfaceView);


//        call_name.setAlpha((float) 0.7);
        //获取电话服务
        mTelephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        // 手动注册对PhoneStateListener中的listen_call_state状态进行监听
        listener = new InComingStateListener();
        mTelephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        mMtcBluetoothHelper = new MtcBluetoothHelper(this);
        mMtcBluetoothHelper.setCallback(this);
        mMtcBluetoothHelper.start();
        mHeadsetPlugReceiver = new MtcHeadsetPlugReceiver();
        mHeadsetPlugReceiver.start(this);
        mHeadsetPlugReceiver.setCallback(this);

        bluetoothStateChanged();


        initViews();
        RcsLoginManager.addRegisterListener(this);
        RcsNetworkHelper.addNetworkListener(this);
        try {
            handleIntent(intent);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        isCallIncomingVol = false;

        mMtcBluetoothHelper.stop();
        mHeadsetPlugReceiver.stop(this);
        Log.i(TAG,"unregisterReceiver");


        releaseVideo(mCurSession);

        //在onDestory将远场语音设置回原来的状态
        if(!isFarWalleveOpen&&isAuthenticationOk){
            controlApi.setFarSpeechSwitch(false);
        }

        if(isFarWalleveOpen&&!isAuthenticationOk){
            controlApi.setFarSpeechSwitch(true);
        }

        //注销控制，释放资源
        controlApi.unregisterControl();

        if("1".equals(MyApplication.getInstance().getIs_walleve())){
            rever();
        }

        unregisterReceiver(mReceiver);
        RcsDebug.log(TAG, "onDestroy");
        RcsNetworkHelper.removeNetworkListener(this);
        RcsLoginManager.removeRegisterListener(this);
        if (mCurSession != null) {
            RcsCallManager.getInstance().removeCallSession(mCurSession.getCallId());
            mCurSession.close();
            mCurSession = null;
        }
        mTelephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);

        PropertyUtils.set("com.tcl.android.phoneState", "CALL_STATE_IDLE");
        PropertyUtils.set("com.tcl.phoneState", "0");
        Intent i = new Intent(FINISH_ACTION);
        this.sendBroadcast(i);

        JusProximityUtils.stop();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: ");
        RcsDebug.log(TAG, "onResume");
        super.onResume();
        isRecomingApplication = true;
        Log.i(TAG,"isRecomingApplication : "+isRecomingApplication);
        if (mCurSession != null && mCurSession.isInCall()) {
            mAudioManager.requestAudioFocus(null, AudioManager.STREAM_VOICE_CALL, AudioManager.AUDIOFOCUS_GAIN);
        }
        if (mHasShowNotification)
            removeNotification();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: ");
        RcsDebug.log(TAG, "onPause");

        isRecomingApplication = false;
        Log.i(TAG,"isRecomingApplication : "+isRecomingApplication);
        super.onPause();

        if (mCurSession != null && mCurSession.getState() < RcsCallSession.State.TERMINATING &&
                mCurSession.getState() > RcsCallSession.State.INITIATED) {
            Log.i(TAG,"postNotification");
            postNotification();
        } else if (mHasShowNotification) {
            Log.i(TAG,"removeNotification");
            removeNotification();
        }
        Log.i(TAG,"onPause finish");
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed: ");
        if (mCurSession.isInCall()) {
            return;
        }
//        super.onBackPressed();
    }

    //蓝牙设备监听
    private void bluetoothStateChanged()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        mReceiver = new BlueToothReceiver();
        Log.i(TAG,"registerReceiver");
        registerReceiver(mReceiver,filter);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initViews() {
        Log.i(TAG, "initViews: ");
        mMultiCallView = (MultiCallView) findViewById(R.id.multi_call_main);
        mMultiCallView.initView();


        mRoot = (ViewGroup) findViewById(R.id.parent);
        mOneCallView = (OneCallView) findViewById(R.id.one_call_main);
        mOneCallView.initViews();



        if (mIsConference) {
            mMultiCallView.setVisibility(View.VISIBLE);
            mOneCallView.setVisibility(View.GONE);
//            mAnswer.setFocusable(true);
        } else {
            mMultiCallView.setVisibility(View.GONE);
            mOneCallView.setVisibility(View.VISIBLE);
//            mAnswer.setFocusable(true);
        }

//        mEnd.setFocusable(true);
//        mEnd.requestFocus();

    }





    @SuppressLint({"WrongConstant"})
    private void updateViews(RcsCallSession callSession) {
        Log.i(TAG, "updateViews: ");
        Log.i(TAG, " callSession:" + callSession);
        RcsDebug.log(TAG, " callSession:" + callSession);
        int callState;
        if (callSession == null) {
            callState = RcsCallSession.State.INVALID;
        } else {
            callState = callSession.getState();
        }
        Log.i(TAG, "updateViews: callState = " + callState);


        if(callSession.getCallType() == RcsCallDefines.CALL_TYPE_ONE_VIDEO)
        {


            if(callState == RcsCallSession.State.OUTGOING)
            {
//
//                shrinkPreview();
                video_outgoing_call_view.setVisibility(View.VISIBLE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);

                cancel = video_outgoing_call_view.findViewById(R.id.cancel);
                cancel.setFocusable(true);
                cancel.requestFocus();
//                setTvAudio(preCallSystemAudio,true);

            }
            else if (callState == RcsCallSession.State.INCOMING)
            {
                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.VISIBLE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);

                cancel = video_incoming_call_view.findViewById(R.id.cancel);
                mAcceptVideo.setFocusable(true);
                mAcceptVideo.requestFocus();

            }
            else if (callState == RcsCallSession.State.ALERTED)
            {
//                shrinkPreview();
                video_outgoing_call_view.setVisibility(View.VISIBLE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);



                cancel = video_outgoing_call_view.findViewById(R.id.cancel);
                mOneCallView.setStateText("对方已振铃");
                cancel.setFocusable(true);
                cancel.requestFocus();

            }
            else if (callState == RcsCallSession.State.TALKING)
            {
//                if(isCallIncomingVol){
//                    setTvAudio(callSystemAudio,true);
//                }

                Log.i(TAG,"VIDEO CALL TALKING");
                mAvatar.setVisibility(View.INVISIBLE);
                call_name.setVisibility(View.INVISIBLE);

                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.VISIBLE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);

                remove_video = video_talking_view.findViewById(R.id.transfer_to_voice_talking);
                cancel = video_talking_view.findViewById(R.id.cancel);
                mOneCallView.mViewMenuVideo = video_talking_view.findViewById(R.id.call_panel_opration_video);
                mOneCallView.mBtnMute = video_talking_view.findViewById(R.id.call_mute);
                cancel.setFocusable(true);
                cancel.requestFocus();
            }

        }
        else
        {

            if(callState == RcsCallSession.State.OUTGOING){
                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.VISIBLE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);

                cancel = audio_outgoing_call_view.findViewById(R.id.cancel);
                cancel.setFocusable(true);
                cancel.requestFocus();
                cancel.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if(keyCode==KeyEvent.KEYCODE_DPAD_DOWN) {
                            cancel.setFocusable(true);
                            cancel.setFocusableInTouchMode(true);
                            cancel.requestFocus();
                            Log.i(TAG, "cancel: cancel ================ ");

                        }
                        return false;
                    }

                });
//                setTvAudio(preCallSystemAudio,true);


            }else if(callState == RcsCallSession.State.INCOMING){
                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.VISIBLE);
                audio_talking_view.setVisibility(View.GONE);

                cancel = audio_incoming_call_view.findViewById(R.id.cancel);
                mAcceptVoice.setFocusable(true);
                mAcceptVoice.requestFocus();

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (audioVol<systemAudio_20 && isCallIncomingVol){
//                            audioVol = audioVol+3;
//                            setTvAudio(audioVol,true);
//                            Log.i(TAG,"audioVol is : "+audioVol);
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                        while (audioVol<preCallSystemAudio && isCallIncomingVol)
//                        {
//                            audioVol = audioVol+1;
//                            setTvAudio(audioVol,true);
//                            Log.i(TAG,"audioVol is : "+audioVol);
//                            try {
//                                Thread.sleep(500);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                }).start();



            }else if(callState == RcsCallSession.State.ALERTED){

                //
                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.VISIBLE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.GONE);

                cancel = audio_outgoing_call_view.findViewById(R.id.cancel);
                mOneCallView.setStateText("对方已振铃");
                cancel.setFocusable(true);
                cancel.requestFocus();
            }
            else if(callState == RcsCallSession.State.TALKING){
                Log.i(TAG,"AUDIO call talking");
//                if(isCallIncomingVol){
//                    setTvAudio(callSystemAudio,true);
//                }

                mAvatar.setVisibility(View.VISIBLE);
                call_name.setVisibility(View.VISIBLE);

                video_outgoing_call_view.setVisibility(View.GONE);
                video_incoming_call_view.setVisibility(View.GONE);
                video_talking_view.setVisibility(View.GONE);
                audio_outgoing_call_view.setVisibility(View.GONE);
                audio_incoming_call_view.setVisibility(View.GONE);
                audio_talking_view.setVisibility(View.VISIBLE);

                mOneCallView.mViewMenu = audio_talking_view.findViewById(R.id.call_panel_operation);
                mOneCallView.mBtnMute= audio_talking_view.findViewById(R.id.call_mute);

                if(isMute)
                {
                    mOneCallView.mBtnMute.setBackground(ContextCompat.getDrawable(this,R.drawable.ic_call_slient_focus));
                }else {
                    mOneCallView.mBtnMute.setBackground(ContextCompat.getDrawable(this,R.drawable.ic_call_slient_nor));
                }

                cancel = audio_talking_view.findViewById(R.id.cancel);
                cancel.requestFocus();

            }
        }


    }

    private void handleIntent(Intent intent) throws CameraAccessException {
        Log.i(TAG, "handleIntent: ");
        int callId = intent.getIntExtra(JusCallDelegate.EXTRA_CALL_ID, RcsCallDefines.INVALIDID);
        Log.i(TAG,"handleIntent callId : "+ callId);
        if (mIsConference) {
            Log.i(TAG, "handleIntent: "+mIsConference);
            mMultiCallView.setPanelEnabled(false);
            if (callId == RcsCallDefines.INVALIDID) {
                mMultiCallMemberList = intent.getParcelableArrayListExtra(JusCallDelegate.EXTRA_PEER_LIST_NUMBERS);
                mMultiCallView.updateView(true, null);
                multiCall();
            } else {
                mMultiCallMemberList = new ArrayList<>();
                RcsCallSession rcsCallSession = RcsCallManager.getInstance().getCallSession(callId);
                rcsCallSession.setListener(this);
                mCurSession = rcsCallSession;
                RcsCallMember callMember = rcsCallSession.getCallMember();
                String displayName = callMember.getDisplayName() != null ? callMember.getDisplayName() : callMember.getNumber();
                mMultiCallView.updateView(false, displayName);
                mMultiCallView.updateViewWithConnected(false);
                ring();
            }
            mMultiCallGridAdapter = new MultiCallGridAdapter(getBaseContext());
            mMultiCallGridAdapter.setList(mMultiCallMemberList);
            mMultiCallView.getGridview().setAdapter(mMultiCallGridAdapter);
            mMultiCallView.getGridview().setOnItemClickListener(this);
            mMultiCallGridAdapter.notifyDataSetChanged();
        } else {

            Intent i = new Intent(START_ACTION);
            Intent come = new Intent(PHONE_ACTION);

            //获取远场语音控制器
            controlApi = WalleveApi.getInstance().getControlApi();
            isAuthenticationOk = isNetworkAvailable(this);

            if(isAuthenticationOk){
                Log.i(TAG,"鉴权成功，开关远场语音");
                //注册控制器
                controlApi.registerControl(new IVoiceRunningListener() {
                    @Override
                    public boolean voiceWakeUp(@NotNull String s, int i, float v) {
                        return false;
                    }

                    @Override
                    public void voiceSleep() {

                    }
                    //通过回调获取远场语音的状态
                    @Override
                    public void farSwitchStatus(boolean b) {
                        Log.i(TAG,"b : " + b);
                        isFarWalleveOpen = b;
                        //判断远场语音是否是开启状态
                        //是，不用操作；否，打开远场语音
                        Log.i(TAG,"isFarWalleveOpen : " + isFarWalleveOpen);
                        if(!isFarWalleveOpen && !isDeviceNameMatch("TCL_RC")){
                            Log.i(TAG,"远程语音是关闭的,现在开启");
                            controlApi.setFarSpeechSwitch(true);
                            TCLToast.makeText(getBaseContext(),"已为您自动打开远程语音",TCLToast.LENGTH_SHORT).show();

                        }else {
                            Log.i(TAG,"远程语音是开启状态的");
                        }
                    }

                    @Override
                    public void handleMessage(@Nullable String s) {

                    }

                    @Override
                    public void requestOfflineWords() {

                    }

                    @Override
                    public void enableSuccess() {

                    }

                    @Override
                    public void enableFailed(@Nullable String s) {

                    }
                });
                //调用远场语音的requestFarSwitchStatus函数，触发监听器的farSwitchStatus回调
                // 将状态值b付给成员变量isFarWalleveOpen
                controlApi.requestFarSwitchStatus();
            }else {

                controlApi.registerControl(new IVoiceRunningListener() {
                    @Override
                    public boolean voiceWakeUp(@NotNull String s, int i, float v) {
                        return false;
                    }

                    @Override
                    public void voiceSleep() {

                    }

                    @Override
                    public void farSwitchStatus(boolean b) {

                        isFarWalleveOpen = b;

                        if(isFarWalleveOpen&&isDeviceNameMatch("TCL_RC")) {
                            Log.i(TAG, "远场语音是开的，现在关闭");
                            TCLToast.makeText(getBaseContext(),"当前状态无网络，请关闭远场语音再重试",TCLToast.LENGTH_LONG).show();
                        }

                        if(!isDeviceNameMatch("TCL_RC"))
                        {
                            TCLToast.makeText(getBaseContext(),"当前状态无网络，请连接蓝牙遥控后，重启电视再拨打或打开网络重试",TCLToast.LENGTH_LONG).show();
                        }

                        Log.i(TAG,"鉴权失败，开关远场语音");
                        Log.i(TAG,"取消tts权限");
                    }

                    @Override
                    public void handleMessage(@Nullable String s) {

                    }

                    @Override
                    public void requestOfflineWords() {

                    }

                    @Override
                    public void enableSuccess() {

                    }

                    @Override
                    public void enableFailed(@Nullable String s) {

                    }
                });
                //调用远场语音的requestFarSwitchStatus函数，触发监听器的farSwitchStatus回调
                // 将状态值b付给成员变量isFarWalleveOpen
                controlApi.requestFarSwitchStatus();

            }



            if (callId == RcsConstants.INVALIDID) {
                Log.i(TAG, "handleIntent: RcsConstants.INVALIDID "+RcsConstants.INVALIDID);
                boolean isVideo = intent.getBooleanExtra(JusCallDelegate.EXTRA_IS_VIDEO, false);
                peerNumber = intent.getStringExtra(JusCallDelegate.EXTRA_PEER_NUMBER);

                PropertyUtils.set("com.tcl.android.phoneState", "CALL_STATE_RINGING");
                PropertyUtils.set("com.tcl.phoneState", "1");

                if(isVideo){
                    if(!TvCameraApi.getInstance().getSwitchStatus(0)){
                        TCLToast.makeText(this,"请打开摄像头物理开关",TCLToast.LENGTH_SHORT).show();
                    }
                }


                this.sendBroadcast(i);
                call(peerNumber, isVideo);

            } else {
                RcsCallSession rcsCallSession = RcsCallManager.getInstance().getCallSession(callId);

                if(rcsCallSession != null) {
                    Log.i(TAG,"rcsCallSession is not null");
                    rcsCallSession.setListener(this);
                    if(rcsCallSession.isVideo())
                    {
                        if(!TvCameraApi.getInstance().getSwitchStatus(0)){
                            TCLToast.makeText(this,"请打开摄像头物理开关",TCLToast.LENGTH_SHORT).show();
                        }
                        TvCameraApi.getInstance();
                    }
                    this.sendBroadcast(come);
                    PropertyUtils.set("com.tcl.android.phoneState", "CALL_STATE_OFFHOOK");
                    PropertyUtils.set("com.tcl.phoneState", "1");
                    handleIncoming(rcsCallSession);
                }else {
                    Log.i(TAG,"rcsCallSession is null");
                    finish();
                }
            }
        }

    }
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
        Log.i(TAG, "网络不可用");
        return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
        if (info != null) {
            for (int i = 0; i < info.length; i++) {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    Log.i(TAG, "网络不可用");
                    return true;
            }
            }
        }
    }
        return false;
    }


    @SuppressLint("MissingPermission")
    private boolean isDeviceNameMatch(String deviceName){
        BluetoothAdapter adapter =BluetoothAdapter.getDefaultAdapter();
        if(adapter!=null){
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            for(Iterator<BluetoothDevice> iterator = devices.iterator(); iterator.hasNext();) {
                BluetoothDevice device = (BluetoothDevice) iterator.next();
                if (device.getName().contains(deviceName)) {
                    return true;
                }
            }
            return false;
        }else {
            return false;
        }

    }

    private void handleIncoming(RcsCallSession rcsCallSession) {

        Log.i(TAG,"isIncoming"+isIncoming);
        isIncoming = true;

        Log.i(TAG, "handleIncoming: ");
        RcsDebug.log(TAG, "handleIncoming:" + mCurSession);
        if (rcsCallSession == null)
            return;

        if (mCurSession == null) {
            mCurSession = rcsCallSession;
//            mDoodleLayer.setSessionId(mCurSession.getCallId());

            if (mHandler.hasMessages(MSG_SESS_TERM))
                mHandler.removeMessages(MSG_SESS_TERM);

            Log.i(TAG,"1 Incomming successful!");
            // setStateText(0);
            Log.i(TAG,"Incoming ring");

            ring();

            Log.i(TAG,"2 Incoming successful!");
            RcsCallMember callMember = rcsCallSession.getCallMember();
            String number = callMember.getNumber();
            if(number.contains("tel")){
                number = number.substring(4);
            }

            MyRecordsHelper myRecordsHelper = new MyRecordsHelper();
            Person contacts = myRecordsHelper.loadContactsByPhoneNumber(number);
            if(contacts!=null&&contacts.getName()!=null&&!contacts.getName().equals("")){
                Drawable draPhoto = ResourcesCompat.getDrawable(getResources(),photos[0],null);
                mAvatar.setBackground(draPhoto);
                String callName = contacts.getName();
                call_name.setText(callName);

            }else if(contacts!=null&&(contacts.getName()==null||contacts.getName().equals(""))){
                Drawable draPhoto = ResourcesCompat.getDrawable(getResources(),photos[0],null);
                mAvatar.setBackground(draPhoto);
                call_name.setText(number);
            }
            else {
                mAvatar.setBackground(ResourcesCompat.getDrawable(getResources(),photos[0],null));
                call_name.setText(number);
            }

            mOneCallView.mChrState = findViewById(R.id.state_call);
            mOneCallView.mChrState.setVisibility(View.INVISIBLE);

            isOutgoingCallVideo = rcsCallSession.isVideo();

            if(!rcsCallSession.isVideo()){
                updateViews(rcsCallSession);
            }

            if (rcsCallSession.isVideo()) {
                updateViews(rcsCallSession);
            }


        }
        Log.i(TAG,"4 Incomming successful!");
    }

    @SuppressLint({"WrongConstant", "WrongViewCast", "ResourceAsColor"})
    private void call(String peerNumber, boolean is_Video) throws CameraAccessException {

        boolean isVideo = is_Video;
        Log.i(VTAG, "call begin and isVideo is" + isVideo);
        if (TextUtils.isEmpty(peerNumber))
            return;

        if (mHandler.hasMessages(MSG_SESS_TERM))
            mHandler.removeMessages(MSG_SESS_TERM);

        if(isVideo)
        {
            if(isServiceCall(peerNumber)){
                TCLToast.makeText(this,"当前环境不支持视频通话，已转为语音通话",TCLToast.LENGTH_SHORT).show();
                isVideo = false;
            }
        }


        int callType = isVideo ? RcsCallDefines.CALL_TYPE_ONE_VIDEO : RcsCallDefines.CALL_TYPE_ONE_VOICE;

        RcsCallSession callSession = RcsCallManager.getInstance().createCallSession(getBaseContext(), this);

        callSession.setCallType(callType);

        Log.i(VTAG, "call start");

        callSession.start(peerNumber);

        if (mCurSession == null) {
            mCurSession = callSession;


        } else {
            mAnotherSession = callSession;

        }



        isOutgoingCallVideo = isVideo;
        RcsCallMember callMember = callSession.getCallMember();
        String number = callMember.getNumber();
        if(number.contains("tel")){
            number = number.substring(4);
        }
        MyRecordsHelper myRecordsHelper = new MyRecordsHelper();
        Person contacts = myRecordsHelper.loadContactsByPhoneNumber(number);
        if(contacts!=null&&contacts.getName()!=null&& !contacts.getName().equals("")){

            Log.i(TAG,Integer.toString(contacts.getPhoto()));

            Drawable draPhoto = ResourcesCompat.getDrawable(getResources(),photos[0],null);
            mAvatar.setBackground(draPhoto);
            mAvatar.bringToFront();
            String callName = contacts.getName();
            Log.i(TAG,callName);
            call_name.setText(callName);
        }else if(contacts!=null&&(contacts.getName()==null||contacts.getName().equals(""))){
            Drawable draPhoto = ResourcesCompat.getDrawable(getResources(),photos[0],null);
            mAvatar.setBackground(draPhoto);
            mAvatar.bringToFront();
            call_name.setText(number);
        }
        else {
            mAvatar.setBackground(ResourcesCompat.getDrawable(getResources(),photos[0],null));
            mAvatar.bringToFront();
            call_name.setText(number);
        }

        mOneCallView.mChrState = findViewById(R.id.state_call);
        mOneCallView.setStateText("正在呼叫...");

        if (isVideo)
        {


            Log.i(VTAG,"changeCameraState");
            changeCameraStateOpen();

            Log.i(VTAG,"createVideoViews");

            createVideoViews();
            Log.i(VTAG,"startPreviewView");
            startPreviewView();
//            shrinkPreview();
            Log.i(VTAG,"shrinkPreview");
        }
    }


    private void multiCall() {
        Log.i(TAG, "multiCall: ");
        mCurSession = RcsCallManager.getInstance().createCallSession(getBaseContext(), this);
        RcsCallMember[] callMembers = new RcsCallMember[mMultiCallMemberList.size()];
        for (int index = 0; index < mMultiCallMemberList.size(); index++) {
            callMembers[index] = mMultiCallMemberList.get(index);
        }
        mCurSession.startConference(callMembers);
    }

    private void changeCameraStateOpen(){
        isCameraOn = TvCameraApi.getInstance().getStatus(0);
        Log.i(TAG,"TvCameraApi.getInstance().getStatus(0) : "+isCameraOn);
        if(!isCameraOn) {
            Log.i(TAG, "设置开启摄像头软开关");
            TvCameraApi.getInstance().setStatus(0, true);
        }
    }

    private void changeCameraStateClose(){
        if(!isCameraOn) {
            Log.i(TAG, "设置关闭摄像头软开关");
            TvCameraApi.getInstance().setStatus(0, false);
        }

    }

    private void term(RcsCallSession rcsCallSession, CallReasonInfo reasonInfo) {
        Log.i(TAG, "term: "+ reasonInfo.toString());
        Log.i(TAG, "term getExtraCode: "+ reasonInfo.getExtraCode());
        RcsDebug.log(TAG, "term: " + reasonInfo.toString());

        if (mStatistics != null && mStatistics.isShow()) {
            mStatistics.hideStat();
        }
        ringStop();



        if(reasonInfo.getExtraCode()==RcsCallDefines.MTC_CALL_ERR_REQ_TERMED||reasonInfo.getExtraCode()==RcsCallDefines.MTC_CALL_ERR_TEMP_UNAVAIL)
        {
            TCLToast.makeText(this,"未接通",TCLToast.LENGTH_SHORT).show();
        }else {
            TCLToast.makeText(this,"通话结束",TCLToast.LENGTH_SHORT).show();
        }



        if(rcsCallSession.isVideo())
        {
            changeCameraStateClose();
        }

        releaseVideo(rcsCallSession);

        Log.i(TAG,"isRecomingApplication : " + isRecomingApplication);
        if(isRecomingApplication)
        {
            Intent intent = new Intent(TclCallActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }

        JusProximityUtils.stop();
        removeNotification();

        RcsDebug.log(TAG,"extraCode "+reasonInfo.getExtraCode());
        int delay = 0;
        int stateTextId = 0;
        switch (reasonInfo.getExtraCode()) {
            case MtcCallConstants.EN_MTC_CALL_TERM_REASON_DECLINE:
            case MtcCallConstants.MTC_CALL_TERM_CANCEL:
                break;
            case MtcCallConstants.EN_MTC_CALL_TERM_REASON_NORMAL:
                stateTextId = R.string.call_ending;
                delay = 500;
                break;
            case MtcCallConstants.MTC_CALL_TERM_REPLACE:
            case MtcCallConstants.MTC_CALL_TERM_BYE:
            case MtcCallConstants.MTC_CALL_TERM_TIMEOUT:
            case MtcCallConstants.MTC_CALL_TERM_DECLINE:
            case MtcCallConstants.MTC_CALL_TERM_BUSY:
                delay = 1000;
                stateTextId = R.string.call_ended;
                break;
            case MtcCallConstants.MTC_CALL_ERR_USER_NOTREG:
                delay = -1;
                stateTextId = R.string.offline;
                break;
            case MtcCallConstants.MTC_CALL_ERR_NOT_FOUND:
                delay = 500;
                stateTextId = R.string.not_found;
                break;
            case MtcCallConstants.MTC_CALL_ERR_TEMP_UNAVAIL:
                delay = 500;
                stateTextId = R.string.temporarily_unavailable;
                break;
            case MtcCallConstants.EN_MTC_CALL_TERM_REASON_NOT_AVAILABLE:
            case MtcCallConstants.MTC_CALL_ERR_FORBIDDEN:
            default:
                delay = 1000;
                stateTextId = R.string.temporarily_unavailable;
                break;
        }
        if (isEmergencyCall()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("网络不支持WLAN紧急呼叫");
            builder.setPositiveButton(android.R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });
            builder.create();
            builder.show();
        } else {
            if (delay == 0) {
                finish();
            } else {
                if (rcsCallSession.isConference()) {
                    mMultiCallView.stopSenderStatus();
                } else {
                    Log.i(TAG,"termed:updateViews");
//                    updateViews(rcsCallSession);
                }
                RcsDebug.log(TAG,"stateTextId "+stateTextId);
                RcsDebug.log(TAG,"not found "+ R.string.not_found);
                RcsDebug.log(TAG,"unva  "+ R.string.temporarily_unavailable);
                Log.i(TAG, "term: unva  "+ R.string.temporarily_unavailable);
                if(R.string.not_found == stateTextId) {
                    mHandler.sendEmptyMessageDelayed(MSG_SESS_TO_CS, delay);
                }else {
                    mHandler.sendEmptyMessageDelayed(MSG_SESS_TERM, delay);
                }
            }
        }
    }
    public  void rever(){
        String PACKAGE_NAME = "com.tcl.walleve";
        String SERVICE_NAME = "com.tcl.soundbox.SoundBoxMsgService";//com.tiidian.seed.service.SeedService
        Intent i = new Intent();
        i.setComponent(new ComponentName(PACKAGE_NAME, SERVICE_NAME));
        MyApplication.getContext().getApplicationContext().startService(i);
    }





    private void createVideoViews() {
        Log.i(TAG, "createVideoViews: ");
        RcsDebug.log(TAG, " mLocalView:" + mLocalView + " mRemoteView:" + mRemoteView);
        if (mLocalView != null || mRemoteView != null){
            return;
        }

        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;// 屏幕宽度（像素）
        int height= dm.heightPixels; // 屏幕高度（像素）

        FrameLayout.LayoutParams lflp = new FrameLayout.LayoutParams(
                width/4, height/4);

//        flp.gravity = Gravity.CENTER;
        FrameLayout.LayoutParams rflp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mLocalView =  RcsCallManager.getInstance().createSurfaceView(getBaseContext());
        lflp.leftMargin=1400;
        lflp.topMargin=700;
        lflp.gravity=0;

        mRemoteView = RcsCallManager.getInstance().createSurfaceView(getBaseContext());
        mRemoteView.setLayoutParams(rflp);
        mOneCallView.addView(mRemoteView,0);

        mLocalView.setLayoutParams(lflp);
        slocalView.addView(mLocalView, 0);
        mLocalView.setZOrderMediaOverlay(true);
        mLocalView.setVisibility(View.INVISIBLE);
        RcsDebug.log(TAG, " mLocalView:" + mLocalView + " mRemoteView:" + mRemoteView);
    }

//    private void createVideoViews() {
//        Log.i(TAG, "createVideoViews: ");
//        RcsDebug.log(TAG, " mLocalView:" + mLocalView + " mRemoteView:" + mRemoteView);
//        if (mLocalView != null || mRemoteView != null)
//            return;
//        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////        FrameLayout.LayoutParams flp = new FrameLayout.LayoutParams(
////                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
////        flp.gravity = Gravity.CENTER;
//        mRemoteView = RcsCallManager.getInstance().createSurfaceView(getBaseContext());
//        mRemoteView.setLayoutParams(flp);
//        mOneCallView.addView(mRemoteView, 0);
//        mLocalView =  RcsCallManager.getInstance().createSurfaceView(getBaseContext());
//        mLocalView.setLayoutParams(flp);
////        mOneCallView.addView(mLocalView, 0);
//
//        mOneCallView.addView(mLocalView, 1);
//        mLocalView.setZOrderMediaOverlay(true);
//
//
//        mLocalView.setVisibility(View.GONE);
//        RcsDebug.log(TAG, " mLocalView:" + mLocalView + " mRemoteView:" + mRemoteView);
//    }

    @SuppressLint("ResourceAsColor")
    private void startPreviewView() throws CameraAccessException{
        Log.i(TAG, "startPreviewView: ");
        RcsDebug.log(TAG, "startPreviewView");
        mLocalViewIsRemove = true;
        if (mCurSession == null)
            return;
//
        boolean flag = checkCameraFace();
        if(flag){
            Log.i(TAG,"VIDEO_CAMERA_FRONT");
            mCurSession.getVideoCallProvider().setCameraType(RcsCallDefines.VIDEO_CAMERA_FRONT);
            mCurSession.getVideoCallProvider().setPreviewSurfaceView(mLocalView);


        }
        else{
            Log.i(TAG,"VIDEO_CAMERA_BACK");

            mCurSession.getVideoCallProvider().setCameraType(RcsCallDefines.VIDEO_CAMERA_BACK);
            mCurSession.getVideoCallProvider().setPreviewSurfaceView(mLocalView);
            JusCallDelegate.addMirror(mLocalView,RcsCallDefines.VIDEO_CAMERA_BACK);
            TvCameraApi.getInstance().getStatus(CAMERA_FACING_BACK);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mLocalViewIsRemove){
                    Log.i(TAG,"mLocalView可见");
                    mLocalView.setVisibility(View.VISIBLE);
                }
            }
        },4000);

        mRemoteView.setVisibility(View.GONE);


    }

    @SuppressLint("ResourceAsColor")
    private void shrinkPreview() {
        Log.i(TAG, "shrinkPreview: ");
        //mOneCallView.addView(mRemoteView, 2);
        mCurSession.getVideoCallProvider().shrinkView(this,mLocalView, mOneCallView,4, 4,1400,700);
        //mCurSession.getVideoCallProvider().shrinkPreviewSurfaceView(this,mLocalView, mOneCallView);
//        mCurSession.getVideoCallProvider().changeVideo(mLocalView, mRemoteView);
        //mCurSession.getVideoCallProvider().shrinkPreviewSurfaceView(mRemoteView, mOneCallView);
//        mRemoteView.setBackgroundColor(R.color.white);


    }





    boolean checkCameraFace() throws CameraAccessException{
        int nembersOfCamaras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo= new CameraInfo();

        for( int i = 0; i < nembersOfCamaras;i++) {
            Camera.getCameraInfo(i,cameraInfo);
        }

        if(cameraInfo.facing == CAMERA_FACING_FRONT)
        {
            return true;
        }else {
            return false;
        }
    }



    private void startVideo(RcsCallSession rcsCallSession) {

        Log.i(TAG, "startVideo: ");
        Log.i(TAG,"打开摄像头");
        changeCameraStateOpen();
        mRemoteView.setVisibility(View.VISIBLE);
        RcsDebug.log(TAG, "mtcCallDelegateStartVideo callId = " + rcsCallSession.getCallId());
        if (rcsCallSession == null || !rcsCallSession.isVideo())
            return;

        updateViews(rcsCallSession);
//        setSpeakerphoneOn(false, true);

        if (mOrientationListener == null) {
            mOrientationListener = new JusOrientationListener(getApplicationContext(), mHandler);
            mOrientationListener.setCallback(this);
        }
        int orientation = getWindowManager().getDefaultDisplay().getRotation();
        mOrientationListener.enable(orientation);



        rcsCallSession.getVideoCallProvider().setDisplaySurfaceView(mRemoteView);

//        mEnd.setFocusable(true);
//        mEnd.requestFocus();
    }

    private void stopVideo(RcsCallSession rcsCallSession) {
        Log.i(TAG, "stopVideo: ");
        RcsDebug.log(TAG, "mtcCallDelegateStopVideo callId = " + rcsCallSession.getCallId());
        mLocalViewIsRemove = false;
        if (rcsCallSession == null)
            return;



        if (rcsCallSession.isInCall()) {
            Log.i(TAG, "stopVideo and isInCall and updateView");
            updateViews(rcsCallSession);
        }
        JusProximityUtils.start(getBaseContext());
//        setSpeakerphoneOn(false, false);

        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }

        mOneCallView.setOnClickListener(null);
        if (mLocalView != null) {
            Log.i(TAG,"remove mLocalView");
            rcsCallSession.getVideoCallProvider().removePreviewSurfaceView(mLocalView);
            mLocalView.setOnTouchListener(null);
            slocalView.removeView(mLocalView);
            mLocalView = null;
        }
        if (mRemoteView != null) {
            Log.i(TAG,"remove mRemoteView");
            rcsCallSession.getVideoCallProvider().removeDisplaySurfaceView(mRemoteView);
            mOneCallView.removeView(mRemoteView);
            mRemoteView = null;
        }
    }

    private void releaseVideo(RcsCallSession rcsCallSession){
        Log.i(TAG, "releaseVideo: ");
        mLocalViewIsRemove=false;
        if (rcsCallSession == null)
            return;
        JusProximityUtils.start(getBaseContext());
//        setSpeakerphoneOn(false, false);

        if (mOrientationListener != null) {
            mOrientationListener.disable();
        }

        mOneCallView.setOnClickListener(null);
        if (mLocalView != null) {
            Log.i(TAG,"remove mLocalView");
            rcsCallSession.getVideoCallProvider().removePreviewSurfaceView(mLocalView);
            mLocalView.setOnTouchListener(null);
            slocalView.removeView(mLocalView);
            mLocalView = null;
        }
        if (mRemoteView != null) {
            Log.i(TAG,"remove mRemoteView");
            rcsCallSession.getVideoCallProvider().removeDisplaySurfaceView(mRemoteView);
            mOneCallView.removeView(mRemoteView);
            mRemoteView = null;
        }
    }

    private void updateVideo(RcsCallSession session) {
        Log.i(TAG, "updateVideo: ");
        switch (session.getCallType()) {
            case RcsCallDefines.CALL_TYPE_ONE_VOICE:
                stopVideo(session);
                break;
            case RcsCallDefines.CALL_TYPE_ONE_VIDEO:
                startVideo(session);
                break;
        }
    }






    private void postNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String id = "MyService";
        String description = "my-service";
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(id, description, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
        Log.i(TAG, "postNotification: ");
        Context context = getApplicationContext();
        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context,id);
        }
        String title = mOneCallView.getDisplayName();
        builder.setContentTitle(title);
        builder.setSmallIcon(R.drawable.ic_notify);
        builder.setOngoing(true);

        if (mCurSession.isInCall()) {
            builder.setWhen(mNotificationBase);
            builder.setUsesChronometer(true);
            builder.setContentText(getResources().getString(R.string.talking));
            builder.setTicker(getResources().getString(R.string.talking));
        } else {
            String state = mOneCallView.getStateText();
            builder.setTicker(state);
            builder.setContentText(state);
        }

        Intent intent = new Intent(context, TclCallActivity.class);
        intent.putExtra(JUS_INTENT_FROM_NOTIFICATION, true);
        PendingIntent pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pending);


        notificationManager.notify(JUS_NOTIFICATION_ID, builder.build());
        mHasShowNotification = true;
    }

    private void removeNotification() {
        Log.i(TAG, "removeNotification: ");
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(JUS_NOTIFICATION_ID);
        mHasShowNotification = false;
    }



    private void ring() {
        Log.i(TAG, "ring: ");
        if (mRingRawId > 0) {
            if (!JusRingUtils.startRing(getBaseContext(), mRingRawId)) {
                RcsDebug.log(TAG, "Ring File Raw Id is wrong");
            }
        } else if (!TextUtils.isEmpty(mRingFilePath)) {
            if (!JusRingUtils.startRing(getBaseContext(), mRingFilePath)) {
                RcsDebug.log(TAG, "Ring File isn't existed");
            }
        }
    }

    private void ringStop() {
        Log.i(TAG, "ringStop: ");
        JusRingUtils.stop();
    }

    private void ringAlert(boolean looping, int timeout) {
        Log.i(TAG, "ringAlert: ");
//        MtcRing.Mtc_RingSetCtmName(MtcRingConstants.EN_MTC_RING_RING, "Term.wav");
//        if (looping) {
//            MtcRing.Mtc_RingPlay(MtcRingConstants.EN_MTC_RING_RING | MtcRingConstants.MTC_RING_ASSET_MASK, timeout);
//        } else {
//            MtcRing.Mtc_RingPlayNoLoop(MtcRingConstants.EN_MTC_RING_RING | MtcRingConstants.MTC_RING_ASSET_MASK);
//        }
    }

    private void ringAlertStop() {
        Log.i(TAG, "ringAlertStop: ");
        MtcRing.Mtc_RingStop(MtcRingConstants.EN_MTC_RING_RING_BACK);
    }

    private void showVideoReq() {
        Log.i(TAG, "showVideoReq: ");
//        TCLThemeUtils.setTheme(this);
        View view = LayoutInflater.from(this).inflate(R.layout.select_update_call_mode,null);
        final AlertDialog dialog = new AlertDialog.Builder(this,R.style.Dialog).setView(view).create();
        String show = mOneCallView.getDisplayName()+" "+ "邀请你加入视频聊天";
        ((TextView) view.findViewById(R.id.text)).setText(show);
        ((TCLButton) view.findViewById(R.id.bingo)).requestFocus();

        ((TCLButton) view.findViewById(R.id.bingo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                updateViews(mCurSession);
                createVideoViews();
                try {
                    startPreviewView();
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                mCurSession.responseUpdate(RcsCallDefines.CALL_TYPE_ONE_VIDEO);
            }
        });

        ((TCLButton) view.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                mCurSession.responseUpdate(RcsCallDefines.CALL_TYPE_ONE_VOICE);
            }
        });

//        b.setMessage(String.format(getString(R.string.video_req_message), mOneCallView.getDisplayName()));
//        b.setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                updateViews(mCurSession);
//                createVideoViews();
//                try {
//                    startPreviewView();
//                } catch (CameraAccessException e) {
//                    e.printStackTrace();
//                }
//                mCurSession.responseUpdate(RcsCallDefines.CALL_TYPE_ONE_VIDEO);
//            }
//        });
//        b.setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                mCurSession.responseUpdate(RcsCallDefines.CALL_TYPE_ONE_VOICE);
//
//            }
//        });
        dialog.show();

    }

    private void showMediaErrorDialog(String title, String message) {
        Log.i(TAG, "showMediaErrorDialog: ");
        if ((mMediaErrorDialog != null && mMediaErrorDialog.isShowing()) || isMediaErrorKeepCall) {
            return;
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(title);
        b.setMessage(message);
        b.setPositiveButton("结束通话", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                CallReasonInfo reasonInfo = new CallReasonInfo(CallReasonInfo.CODE_LOCAL_CALL_TERM,
                        RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
                if (mAnotherSession != null) {
                    mAnotherSession.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
                    term(mAnotherSession, reasonInfo);
                }
                mCurSession.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
                term(mCurSession, reasonInfo);
                dialog.dismiss();
            }
        });
        b.setNegativeButton("继续", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isMediaErrorKeepCall = true;
                dialog.dismiss();
            }
        });
        mMediaErrorDialog = b.create();
        mMediaErrorDialog.show();
    }

    private boolean isEmergencyCall() {
        Log.i(TAG, "isEmergencyCall: ");
        String[] emergencyCallArray = {"110", "112", "119", "120"};
        for (String s : emergencyCallArray) {
            if (s.equals(mCurSession.getCallNumber())) {
                return true;
            }
        }
        return false;
    }

    private static final int MSG_SESS_TO_CS = 1;
    private static final int MSG_SESS_TERM = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case MSG_SESS_TERM:
                    finish();
                    break;
                case MSG_SESS_TO_CS:
                    mTelephonyManager.listen(null, PhoneStateListener.LISTEN_CALL_STATE);
                    if(!TextUtils.isEmpty(peerNumber)) {
                        AlertDialog.Builder b = new AlertDialog.Builder(TclCallActivity.this);
                        b.setTitle("");
                        b.setMessage("当前拨号失败,是否转为CS电话");
                        b.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @SuppressLint("MissingPermission")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + peerNumber));
                                startActivity(intent);
                                dialog.dismiss();
                                finish();
                            }
                        });
                        b.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        }).create().show();
                    }
                    break;
            }
        }
    };

    public void onVideoAnswer(View v) throws CameraAccessException {
        Log.i(TAG, "onVideoAnswer: ");
        ringStop();
        isCallIncomingVol = false;
        changeCameraStateOpen();

//        JusCallDelegate.setRxVol(mCurSession.getCallId(),90);

        createVideoViews();
        startPreviewView();
//        shrinkPreview();
        int callType = RcsCallDefines.CALL_TYPE_ONE_VIDEO;
        mCurSession.accept(callType);

        if (!mCurSession.isVideo()) {
            Log.i(TAG, "onAnswer: !mCurSession.isVideo()");
            JusProximityUtils.start(getApplicationContext());
        }
//        setTvAudio(callSystemAudio,true);
        updateViews(mCurSession);
    }

    public void onAudioAnswer(View v) {
        Log.i(TAG, "onAudioAnswer: ");
        ringStop();
        isCallIncomingVol = false;
        mCurSession.accept(RcsCallDefines.CALL_TYPE_ONE_VOICE);
        JusProximityUtils.start(getApplicationContext());
//        setTvAudio(callSystemAudio,true);
        updateViews(mCurSession);
    }

    public void onDecline(View v) {
        Log.i(TAG,"ringStop by reject");
        ringStop();
        isCallIncomingVol = false;
        Log.i(TAG, "onDecline: ");
        mCurSession.reject(MtcCallConstants.EN_MTC_CALL_TERM_REASON_DECLINE);
        term(mCurSession, new CallReasonInfo(MtcCallConstants.EN_MTC_CALL_TERM_REASON_DECLINE));

    }

    public void onEnd(View v) {
        Log.i(TAG, "onEnd: ");
        if (mCurSession.getState() == RcsCallSession.State.IDLE) {
            Log.i(TAG, "onEnd: return" + (mCurSession.getState() == RcsCallSession.State.IDLE));
            finish();
            return;
        }

        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        callSession.terminate(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL);
        term(callSession, new CallReasonInfo(RcsCallDefines.EN_MTC_CALL_TERM_REASON_NORMAL));

//        if(isServiceRunning(this,"com.test.tclphone.service.TvTclCallService")){
//            stopService(new Intent(getBaseContext(), TvTclCallService.class));
//            Log.i(TAG, "onDestroy: stopService");
//        }else  Log.i(TAG, "onDestroy: TvTclCallService is not on");
    }

    public void onMute(View v) {
        Log.i(TAG, "onMute: ");
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        Log.i(TAG,"onMute:2");
        boolean muted;
        if (callSession.isConference()) {
            muted = mMultiCallView.selectMute();
        } else {
            Log.i(TAG,"onMute:3");
            muted = mOneCallView.selectMute();
        }
        Log.i(TAG,"onMute:4");
        callSession.setMute(muted);
        if(muted)
        {
            mOneCallView.mBtnMute.setBackground(ContextCompat.getDrawable(this,R.drawable.ic_call_slient_focus));
        }else {
            mOneCallView.mBtnMute.setBackground(ContextCompat.getDrawable(this,R.drawable.ic_call_slient_nor));
        }
        isMute = muted;

        Log.i(TAG,"onMute:5");
    }


    @Override
    public void callSessionRtpConnect(RcsCallSession session) {
        if (!session.isInComing()) {
            ringAlertStop();
        }
    }

    public void onSpeaker(View v) {
        Log.i(TAG, "onSpeaker: ");
        mMtcBluetoothHelper.start();
        if(mMtcBluetoothHelper.getCount() > 0){
            selectAudio();
            return;
        }
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        boolean isSpeakerphoneOn;
        if (callSession.isConference()) {
            isSpeakerphoneOn = mMultiCallView.selectSpeaker();
        } else {
            isSpeakerphoneOn = mOneCallView.selectSpeaker();
        }
        if(isSpeakerphoneOn){
            mAudio = AUDIO_SPEAKER;
        }else {
            mAudio = mHeadsetPlugReceiver.mPlugged?AUDIO_HEADSET:AUDIO_RECEIVER;
        }
//        setSpeakerphoneOn(callSession.isConference(), isSpeakerphoneOn);
    }

//    private void setSpeakerphoneOn(boolean isConference, boolean isSpeakerphoneOn) {
//        Log.i(TAG, "setSpeakerphoneOn: ");
//        if (isConference) {
//            mMultiCallView.mBtnSpeaker.setSelected(isSpeakerphoneOn);
//        } else {
//            if(mOneCallView.mBtnSpeaker == null){
//                return;
//            }
//            mOneCallView.mBtnSpeaker.setSelected(isSpeakerphoneOn);
//        }
//        mAudioManager.setSpeakerphoneOn(isSpeakerphoneOn);
//    }

    public void onKeypad(View v) {
        Log.i(TAG, "onKeypad: ");
        mOneCallView.showOrHideKeypad();
    }

    public void onDtmf(View v) {
        Log.i(TAG, "onDtmf: ");
        String dtmfStr = v.getTag().toString();
        mOneCallView.mTextViewDtm.append(dtmfStr);
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        callSession.startDtmf(dtmfStr.charAt(0));
    }

    public void onSwitch(View v) {
        Log.i(TAG, "onSwitch: ");
        if (mCurSession == null)
            return;

        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        RcsVideoCallProvider rcsVideoCallProvider = callSession.getVideoCallProvider();
        rcsVideoCallProvider.switchCamera(mLocalView);
    }

    public void onAddVideo(View v) throws CameraAccessException {
        Log.i(TAG, "onAddVideo: ");
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        callSession.update(RcsCallDefines.CALL_TYPE_ONE_VIDEO);
        createVideoViews();
        startPreviewView();
        updateViews(callSession);
//        setSpeakerphoneOn(false, true);
    }

    public void onRemoveVideo(View v) {
        Log.i(TAG, "onRemoveVideo: ");
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        callSession.update(RcsCallDefines.CALL_TYPE_ONE_VOICE);
        stopVideo(callSession);
        cancel.setFocusable(true);
        cancel.requestFocus();
    }

    public void onHold(View v) {
        Log.i(TAG, "onHold: ");
        if (!mCurSession.isConference() && mCurSession.hasHeld())
            return;

        if (mCurSession.isConference()) {
            mMultiCallView.selectHold();
            mMultiCallView.mBtnHold.setEnabled(false);
        } else {
            mOneCallView.selectHold();
            mOneCallView.mBtnHold.setEnabled(false);
        }

        if (mAnotherSession != null && !mAnotherSession.hasHold()) {
            mAnotherSession.hold();
            return;
        }else if(mAnotherSession != null && !mCurSession.hasHold()){
            mAnotherSession.unhold();
            return;
        }

        if (mHasConferenceHold || mCurSession.hasHold()) {
            mCurSession.unhold();
            mHasConferenceHold = false;
        } else {
            mCurSession.hold();
            mHasConferenceHold = true;
        }
    }

    public void onStatistic(View v) {
        Log.i(TAG, "onStatistic: ");
        if (mStatistics == null) {
            mStatistics = new Statistics(getApplicationContext(), mCurSession.getCallId());
            mOneCallView.addView(mStatistics);
        }
        if (mStatistics.isShow()) {
            mStatistics.hideStat();
        } else {
            mStatistics.showStat();
        }
    }

    public void onTransfer(View v) {
        Log.i(TAG, "onTransfer: ");
        final EditText mTransferEditText = new EditText(getBaseContext());
        mTransferEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        mTransferEditText.setBackgroundColor(getResources().getColor(android.R.color.white));
        mTransferEditText.setTextColor(getResources().getColor(android.R.color.black));

        AlertDialog.Builder builder = new AlertDialog.Builder(TclCallActivity.this);
        builder.setTitle(R.string.transfer_title);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(mTransferEditText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String transferNumber = mTransferEditText.getText().toString();
                if (!TextUtils.isEmpty(transferNumber)) {
                    mCurSession.transfer(transferNumber);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    public void onRecord(View v) {
        Log.i(TAG, "onRecord: ");
        RcsCallSession callSession = mAnotherSession == null ? mCurSession : mAnotherSession;
        if (callSession.isRecording()) {
            callSession.recordStop();
        } else {
            callSession.recordStart(null);
        }
    }

    public void onAddCall(View v) {
        Log.i(TAG, "onAddCall: ");
        if (mMultiCallMemberList != null &&
                mMultiCallMemberList.size() >= JusCallDelegate.JUS_MULTI_CALL_MEMBERS_LIMIT) {
            TCLToast.makeText(getBaseContext(), "超过多方通话最大人数限制", TCLToast.LENGTH_SHORT).show();
            return;
        }

        RcsDebug.log(TAG, "onAddCall");
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_PICK_PHONE);
    }

    public void onMergeCall(View v) {
        Log.i(TAG, "onMergeCall: ");
        mCurSession.merge();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult: ");
        switch (requestCode) {
            case REQUEST_PICK_PHONE:
                if (resultCode == RESULT_OK) {
                    RcsDebug.log(TAG, "data:" + data);
                    Uri contactData = data.getData();
                    RcsDebug.log(TAG, "contactData:" + contactData.toString());
                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                    RcsCallMember callMember = getCallMemberWithCursor(cursor);

//                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(getContentResolver(), contactData, true);
//                    Bitmap thumbBitmap = BitmapFactory.decodeStream(input);
//                    callMember.setThumbBitmap(thumbBitmap);

                    RcsDebug.log(TAG, callMember.toString());
                    String[] partps = {callMember.getNumber()};
                    if (mCurSession.isConference()) {
                        mCurSession.inviteParticipants(partps);
                    } else if (mCurSession.hasHold()) {
                        try {
                            call(callMember.getNumber(), false);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        mCurSession.extendToConference(partps);
                    }
                }
                break;
//            case DoodleButtonsView.REQUEST_SHARE_IMAGE:
//                if(resultCode == RESULT_OK && data != null){
//                    String fileName = data.getStringExtra(MediaPickActivity.EXTRA_OUT_IMAGE_PATH);
//                    mDoodleButtonsView.sendImage(fileName);
//                }
            default:
                break;
        }
    }

    private RcsCallMember getCallMemberWithCursor(Cursor contactCursor) {
        Log.i(TAG, "getCallMemberWithCursor: ");
        if (contactCursor == null)
            return null;

        if (!contactCursor.moveToFirst())
            return null;

        int phoneColumn = contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER);
        int nameColumn = contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        int thumbColunm = contactCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI);
        int phoneNum = contactCursor.getInt(phoneColumn);
        String name = contactCursor.getString(nameColumn);
        String thumbUri = contactCursor.getString(thumbColunm);
        RcsCallMember result = null;
        if (phoneNum > 0) {
            // 获得联系人的ID号
            int idColumn = contactCursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactId = contactCursor.getString(idColumn);
            // 获得联系人电话的cursor
            Cursor phoneCursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "="
                            + contactId, null, null);
            if (phoneCursor.moveToFirst()) {
                for (; !phoneCursor.isAfterLast(); phoneCursor.moveToNext()) {
                    int index = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    int typeIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE);
                    int phoneType = phoneCursor.getInt(typeIndex);
                    String phoneNumber = phoneCursor.getString(index);
                    phoneNumber = phoneNumber.replace(" ", "").replace("-", "");
                    result = new RcsCallMember(phoneNumber, name);
//                  switch (phone_type) {//此处请看下方注释
//                  case 2:
//                      result = phoneNumber;
//                      break;
//
//                  default:
//                      break;
//                  }
                }
                if (!phoneCursor.isClosed()) {
                    phoneCursor.close();
                }
            }
        }
        contactCursor.close();
        return result;
    }

    private void showOtherIncomingDialog(final RcsCallSession anotherSession) {
        Log.i(TAG, "showOtherIncomingDialog: ");
        AlertDialog.Builder b = new AlertDialog.Builder(TclCallActivity.this);
        b.setMessage(String.format(getString(R.string.incoming_req_message), anotherSession.getCallNumber()));
        b.setPositiveButton(R.string.answer, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mCurSession.hasHold()) {
                    int callType = anotherSession.isVideo() ? RcsCallDefines.CALL_TYPE_ONE_VIDEO : RcsCallDefines.CALL_TYPE_ONE_VOICE;
                    anotherSession.accept(callType);
                } else {
                    mCurSession.hold();
                }
                ringStop();
            }
        });
        b.setNegativeButton(R.string.decline, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                anotherSession.reject(RcsCallDefines.EN_MTC_CALL_TERM_REASON_BUSY);
                CallReasonInfo reasonInfo = new CallReasonInfo(CallReasonInfo.CODE_LOCAL_CALL_TERM, RcsCallDefines.EN_MTC_CALL_TERM_REASON_BUSY);
                term(anotherSession, reasonInfo);
                dialog.dismiss();
            }
        });
        mWaitAlertDialog = b.create();
        mWaitAlertDialog.show();
    }

    @Override
    public void callSessionWaiting(RcsCallSession anotherSession) {
        Log.i(TAG, "callSessionWaiting: ");
        RcsDebug.log("PZR_TclCallActivity","callSessionWaiting: ");
        anotherSession.reject(RcsCallDefines.EN_MTC_CALL_TERM_REASON_BUSY);
    }

    @Override
    public void callSessionProgressing(RcsCallSession session) {
        Log.i(TAG, "callSessionProgressing: ");
        boolean isVideo = session.isVideo();
        Log.i(TAG,"callSessionProgressing isVideo" + isVideo);
        if (session.isConference()) {

        } else {
            if (!session.isInComing()) {
                if (session.getState() == RcsCallSession.State.OUTGOING) {
                    Log.i(TAG,"ring begin");
                    updateViews(session);
                } else if (session.getState() == RcsCallSession.State.ALERTED) {
                    updateViews(session);
//                    setSpeakerphoneOn(false, isVideo);
                }
            }


        }
    }

//    public void setTvAudio(int a,boolean b){
//        Log.i(TAG,"currentAudio : " +currentAudio);
//        Log.i(TAG,"callSystemAudio : " +callSystemAudio);
//        Log.i(TAG,"MaxSystemAudio : " +mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
//        if(b){
//            Log.i(TAG,"setTVAudio true!");
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,a,AudioManager.FLAG_PLAY_SOUND);
//        }else {
//            Log.i(TAG,"currentAudio");
//            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentAudio,AudioManager.FLAG_PLAY_SOUND);
//        }
//    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void callSessionStarted(RcsCallSession session) {
        Log.i(TAG, "callSessionStarted: ");

        ringAlertStop();

        Intent i = new Intent(PHONE_ACTION);
        this.sendBroadcast(i);
        PropertyUtils.set("com.tcl.android.phoneState", "CALL_STATE_OFFHOOK");
        PropertyUtils.set("com.tcl.phoneState", "1");
        RcsDebug.log(TAG, "callSessionStarted callId:" + session.getCallId());


        if(mMtcBluetoothHelper.getCount()>0){
            mMtcBluetoothHelper.link(mMtcBluetoothHelper.mAddressList.get(0));
        }

        if (session.isConference()) {
            mMultiCallView.setPanelEnabled(true);
            mMultiCallView.updateViewWithConnected(true);
            mMultiCallView.startSenderStatus();
        } else {
            // setStateText(0);
            if (mAnotherSession == null) {
                mStartTime = System.currentTimeMillis();
            } else {
                mAnotherStartTime = System.currentTimeMillis();
            }

            mOneCallView.mChrState.setVisibility(View.VISIBLE);
            mOneCallView.mChrState.setBase(SystemClock.elapsedRealtime());
            mOneCallView.mChrState.start();
            mNotificationBase = System.currentTimeMillis();
            mOneCallView.mChrState.setTextColor(this.getResources().getColor(R.color.white_90));

            Log.i(TAG,"设置音量完毕");
            if(isOutgoingCallVideo==session.isVideo()){
                if (session.isVideo()) {
                    Log.i(TAG,"callSessionStarted isVideo : true"  );
                    session.getVideoCallProvider().attachVideo();
//                    shrinkPreview();
                    startVideo(session);
                } else {
//                String s = MtcCall.Mtc_SessGetAudioStat(session.getCallId());
//                Log.d(TAG," MtcCall.Mtc_SessGetAudioStat(s) : "+s);
                    Log.i(TAG,"callSessionStarted isVideo : false"  );
                    stopVideo(session);
                }
            }else {
                TCLToast.makeText(this,"当前环境不支持视频通话，已转为语音通话",TCLToast.LENGTH_SHORT).show();
                stopVideo(session);
            }

        }
    }

    private boolean isServiceCall(String call_number){
        for (String serviceCall : RecordConstants.SERVICE_CALL)
        {
            if(serviceCall.equals(call_number))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public void callSessionStartFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionStartFailed: ");
        callSessionTerminated(session, reasonInfo);
        TCLToast.makeText(getBaseContext(), reasonInfo.getExtraMessage()+"", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionTerminated(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionTerminated: ");
        if (mCurSession.isConference() && mCurSession != session)
            return;

        term(session, reasonInfo);
    }

    @Override
    public void callSessionHoldOk(RcsCallSession session) {
        Log.i(TAG, "callSessionHoldOk: ");
        if (session.isConference()) {
            mHasConferenceHold = true;
            mMultiCallView.mBtnHold.setEnabled(true);
        } else {
            if (RcsCallManager.getInstance().hasMultiCall() && session != mAnotherSession) {
                //   setStateText(R.string.answering);
                mAnotherSession.accept(RcsCallDefines.CALL_TYPE_ONE_VOICE);
            } else {
                mOneCallView.mBtnHold.setEnabled(true);
                //   setStateText(R.string.holding);
            }

            if(mAnotherSession!=null && mAnotherSession.hasHold()){
                mCurSession.unhold();
                return;
            }

            if(mAnotherSession!=null && mCurSession.hasHold()){
                mOneCallView.mBtnHold.setEnabled(true);
                mOneCallView.mBtnHold.setSelected(true);
                long startTime = mAnotherStartTime;
                mOneCallView.setHoldUnselected();
                long duration = System.currentTimeMillis() - startTime;
                mOneCallView.mChrState.setBase(SystemClock.elapsedRealtime() - duration);
                mOneCallView.mChrState.start();
                return;
            }
            mOneCallView.mBtnAddVideo.setEnabled(false);
        }
    }

    @Override
    public void callSessionHoldFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionHoldFailed: ");
        TCLToast.makeText(this, R.string.hold_failed, TCLToast.LENGTH_SHORT).show();
//        if (session.isConference()) {
//            mMultiCallView.mBtnHold.setEnabled(true);
//            mMultiCallView.mBtnHold.setSelected(false);
//        } else {
//            mOneCallView.mBtnHold.setEnabled(true);
//            mOneCallView.mBtnHold.setSelected(false);
//        }
    }

    @Override
    public void callSessionHoldReceived(RcsCallSession session) {
        Log.i(TAG, "callSessionHoldReceived: ");
        //  setStateText(R.string.holding);
//        mOneCallView.mBtnHold.setSelected(true);
//        mOneCallView.mBtnAddCall.setEnabled(false);
//        mOneCallView.mBtnAddVideo.setEnabled(false);
    }

    @Override
    public void callSessionUnHoldOk(RcsCallSession session) {
        Log.i(TAG, "callSessionUnHoldOk: ");
//        if (session.isConference()) {
//            mMultiCallView.mBtnHold.setEnabled(true);
//        } else {
//            mOneCallView.mBtnHold.setEnabled(true);
//            mOneCallView.mBtnAddVideo.setEnabled(true);
//            if(mAnotherSession!=null && !mAnotherSession.hasHold()){
//                mOneCallView.setDisplayName(session.getCallMember().getNumber(),session.getCallMember().getDisplayName());
//                mOneCallView.changeTransText("线路二");
//                mOneCallView.setHoldUnselected();
//                mCurSession.hold();
//                return;
//            }
//            if(mAnotherSession != null && !mCurSession.hasHold()){
//                mOneCallView.setDisplayName(session.getCallMember().getNumber(),session.getCallMember().getDisplayName());
//                long startTime = mStartTime;
//                mOneCallView.changeTransText("线路一");
//                mOneCallView.setHoldUnselected();
//                long duration = System.currentTimeMillis() - startTime;
//                mOneCallView.mChrState.setBase(SystemClock.elapsedRealtime() - duration);
//                mOneCallView.mChrState.start();
//                return;
//            }
//
//
//            long startTime = mAnotherSession == null ? mStartTime : mAnotherStartTime;
//            long duration = System.currentTimeMillis() - startTime;
//            mOneCallView.mChrState.setBase(SystemClock.elapsedRealtime() - duration);
//            mOneCallView.mChrState.start();
//            mOneCallView.mTxtName.setText(session.getCallNumber());
//        }
    }

    @Override
    public void callSessionUnHoldFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionUnHoldFailed: ");
        TCLToast.makeText(this, R.string.unhold_failed, TCLToast.LENGTH_SHORT).show();
//        if (session.isConference()) {
//            mMultiCallView.mBtnHold.setEnabled(true);
//            mMultiCallView.mBtnHold.setSelected(true);
//        } else {
//            mOneCallView.mBtnHold.setEnabled(true);
//            mOneCallView.mBtnHold.setSelected(true);
//        }
    }

    @Override
    public void callSessionUnHoldReceived(RcsCallSession session) {
        Log.i(TAG, "callSessionUnHoldReceived: ");
//        mOneCallView.mChrState.setBase(mOneCallView.mChrState.getBase());
//        mOneCallView.mChrState.start();
//        mOneCallView.mBtnHold.setSelected(false);
//        mOneCallView.mBtnAddCall.setEnabled(true);
//        mOneCallView.mBtnAddVideo.setEnabled(true);
    }

    private void convertToConference(boolean isSenderCall, RcsCallSession rcsCallSession) {
        Log.i(TAG, "convertToConference: ");
        rcsCallSession.setListener(this);
        mMultiCallView.setVisibility(View.VISIBLE);
        mOneCallView.setVisibility(View.GONE);

        mMultiCallMemberList = new ArrayList<>(rcsCallSession.getCallMembers());
        mMultiCallGridAdapter = new MultiCallGridAdapter(getBaseContext());
        mMultiCallGridAdapter.setList(mMultiCallMemberList);
        mMultiCallView.getGridview().setAdapter(mMultiCallGridAdapter);
        mMultiCallView.getGridview().setOnItemClickListener(this);
        mMultiCallGridAdapter.notifyDataSetChanged();

        if (isSenderCall) {
            mMultiCallView.updateView(true, null);
        } else {
            mMultiCallView.updateView(false, rcsCallSession.getCallNumber());
            mMultiCallView.updateViewWithConnected(true);
        }
    }

    @Override
    public void callSessionMergeStarted(RcsCallSession session, RcsCallSession mergeSession) {
        Log.i(TAG, "callSessionMergeStarted: ");
        convertToConference(true, mergeSession);
        TCLToast.makeText(this, "开始合并两路通话", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionMergeComplete(RcsCallSession session) {
        Log.i(TAG, "callSessionMergeComplete: ");
        mIsConference = true;
        mCurSession = session;
        mAnotherSession = null;
        mMultiCallView.startSenderStatus();
        TCLToast.makeText(this, "合并成功", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionMergeFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionMergeFailed: ");
        TCLToast.makeText(this, "合并失败", TCLToast.LENGTH_SHORT).show();
        mMultiCallMemberList = null;
    }

    @Override
    public void callSessionUpdated(RcsCallSession session) {
        Log.i(TAG, "callSessionUpdated: ");
        if (session.getState() == RcsCallSession.State.TERMINATING) {
            TCLToast.makeText(getBaseContext(), R.string.transfer_success, TCLToast.LENGTH_SHORT).show();
        } else {
            updateVideo(session);
//            mDoodleButtonsView.setIsVideo(session.isVideo());
        }
    }

    @Override
    public void callSessionUpdateFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        Log.i(TAG, "callSessionUpdateFailed: ");
        switch (reasonInfo.getCode()) {
            case CallReasonInfo.CODE_LOCAL_CALL_UPDATE:
                updateVideo(session);
                break;
            case CallReasonInfo.CODE_LOCAL_CALL_TRANSFER:
                TCLToast.makeText(getBaseContext(), R.string.transfer_failed, TCLToast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void callSessionUpdateReceived(RcsCallSession session) {
        Log.i(TAG, "callSessionUpdateReceived: ");
        if (!session.isInCall() || session.isVideo())
            return;
        showVideoReq();
    }

    @Override
    public void callSessionConferenceExtendStarted(RcsCallSession session) {
        convertToConference(true, session);
        TCLToast.makeText(this, "开始转为多方通话", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionConferenceExtendComplete(RcsCallSession session) {
        mIsConference = true;
        mCurSession = session;
        mMultiCallView.startSenderStatus();
        TCLToast.makeText(this, "一对一转多方通话成功", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionConferenceExtendFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        TCLToast.makeText(this, "一对一转多方失败", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionConferenceExtendReceived(RcsCallSession session) {
        //都是成功 貌似这里也要加
        convertToConference(false, session);
        mIsConference = true;
        mCurSession = session;
        mMultiCallView.startSenderStatus();
        TCLToast.makeText(this, "一对一转多方通话成功", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionInviteParticipantsRequestDelivered(RcsCallSession session) {
        mMultiCallMemberList.clear();
        mMultiCallMemberList.addAll(session.getCallMembers());
        mMultiCallGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void callSessionInviteParticipantsRequestFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        TCLToast.makeText(getBaseContext(), "邀请成员失败", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionRemoveParticipantsRequestDelivered(RcsCallSession session) {
        mMultiCallMemberList.clear();
        mMultiCallMemberList.addAll(session.getCallMembers());
        mMultiCallGridAdapter.notifyDataSetChanged();
        mKickingPartpIndex = -1;
    }

    @Override
    public void callSessionRemoveParticipantsRequestFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        TCLToast.makeText(getBaseContext(), "删除成员失败", TCLToast.LENGTH_SHORT).show();
        mKickingPartpIndex = -1;
    }

    private void updatePartpStatus(RcsCallMember oldCallMember, RcsCallMember newCallMember) {
    }

    @Override
    public void callSessionConferenceStateUpdated(RcsCallSession session) {
        for (RcsCallMember oCallMember : mMultiCallMemberList) {
            String oNumber = oCallMember.getNumber();
            for (RcsCallMember nCallMember : session.getCallMembers()) {
                String nNumber = nCallMember.getNumber();
                if (oNumber.equals(nNumber)) {
                    updatePartpStatus(oCallMember, nCallMember);
                    break;
                }
            }
        }

        mMultiCallMemberList.clear();
        mMultiCallMemberList.addAll(session.getCallMembers());
        mMultiCallGridAdapter.notifyDataSetChanged();
    }

    @Override
    public void callSessionUssdMessageReceived(RcsCallSession session, int mode, String ussdMessage) {
    }

    @Override
    public void callSessionHandover(RcsCallSession session, int srcAccessTech, int targetAccessTech, CallReasonInfo reasonInfo) {

    }

    @Override
    public void callSessionHandoverFailed(RcsCallSession session, int srcAccessTech, int targetAccessTech, CallReasonInfo reasonInfo) {

    }

    @Override
    public void callSessionTtyModeReceived(RcsCallSession session, int mode) {

    }

    @Override
    public void callSessionMultipartyStateChanged(RcsCallSession session, boolean isMultiParty) {

    }

    private void showBadCallStatusDialog(boolean isVideo) {
        Log.i(TAG, "showBadCallStatusDialog: ");
        if (isNeverTipBadStatus || (mBadConnectDialog != null && mBadConnectDialog.isShowing()))
            return;

        String message = isVideo?"当前双方视频通话信号较差,建议切换为语音通话":"当前双方语音通话信号较差,通话可能稍后会被中断,请注意";

        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("提示");
        b.setMessage(message);
        b.setNegativeButton("不再提示", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                isNeverTipBadStatus = true;
            }
        });
        b.setPositiveButton("关闭", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBadConnectDialog = b.create();
        //mBadConnectDialog.show();
    }

    @Override
    public void callSessionStatusChanged(RcsCallSession session) {
        Log.i(TAG, "callSessionStatusChanged: "+ session.getStatus());
        RcsDebug.log(TAG, "callSessionStatusChanged status:" + session.getStatus());
        switch (session.getStatus()) {
            case MtcCallConstants.EN_MTC_NET_STATUS_DISCONNECTED:
                onEnd(null);  // SDK在10秒内没有收到对端的RTP媒体流会上报该通知
                break;
            case MtcCallConstants.EN_MTC_NET_STATUS_VERY_BAD:
                if (session.hasHold())
                    return;
//                mOneCallView.mLayoutRecord.setVisibility(View.INVISIBLE);
                //mOneCallView.setStatusText("信号非常差");
                //showBadCallStatusDialog(session.isVideo());
                break;
            default:
                if (mBadConnectDialog != null && mBadConnectDialog.isShowing()) {
                    mBadConnectDialog.dismiss();
                    mBadConnectDialog = null;
                }
//                mOneCallView.setStatusText("");
                if (session.isRecording()) {
//                    mOneCallView.mLayoutRecord.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public void onVideoCallRenderStarted(RcsCallSession session, SurfaceView surfaceView, int iSource, String iRender, int iWidth, int iHeight) {
        RcsDebug.log(TAG, "onVideoCallRenderStarted surfaceView:" + surfaceView);
//        if (surfaceView == mRemoteView) {
//            shrinkPreview();
//        }
    }

    @Override
    public void callSessionRecordStarted(RcsCallSession session) {
        mOneCallView.startRecord();
        TCLToast.makeText(getBaseContext(), "开始录音", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionRecordStartFailed(RcsCallSession session, CallReasonInfo reasonInfo) {
        TCLToast.makeText(getBaseContext(), "录音失败", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionRecordStoped(RcsCallSession session) {
        mOneCallView.stopRecord();
        TCLToast.makeText(getBaseContext(), "结束录音", TCLToast.LENGTH_SHORT).show();
    }

    @Override
    public void callSessionRecordStopFailed(RcsCallSession session, CallReasonInfo reasonInfo) {

    }

    @Override
    public void callSessionMediaErrorOccurred(int type, String reason) {
        RcsDebug.log(TAG, "callSessionMediaErrorOccurred type:" + type + " reason:" + reason);
        Log.i(TAG,"callSessionMediaErrorOccurred type:" + type + " reason:" + reason);
        switch (type) {
            case RcsAudioManager.MEDIA_ERROR_DEVICE:
            case RcsAudioManager.MEDIA_ERROR_AUDIO:
                String audioTitle = getString(R.string.media_error_audio_title);
                String audioMsg = getString(R.string.media_error_audio_msg);
                showMediaErrorDialog(audioTitle, audioMsg);
                break;
            case RcsAudioManager.MEDIA_ERROR_VIDEO:
                String videoTitle = getString(R.string.media_error_video_title);
                String videoMsg = getString(R.string.media_error_video_msg);
                showMediaErrorDialog(videoTitle, videoMsg);
                break;
        }
    }

    @Override
    public void CallSessionEnrichCallUpdate(RcsCallSession rcsCallSession, XmlPreCall.RcsCallData rcsCallData) {

    }

    @Override
    public void mtcOrientationChanged(int orientation, int previousOrientation) {
        if (mRemoteView != null)
            mCurSession.getVideoCallProvider().rotateSurfaceView(mRemoteView, orientation);
    }

    @Override
    public void onNetworkChanged(int net, int preNet) {

    }

    @Override
    public void onBeforeRegister() {

    }

    @Override
    public void onRegisterStateChanged(int state, int statCode,String statMsg) {
        if (state == RcsLoginManager.MTC_REG_STATE_IDLE) {
            onEnd(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mKickingPartpIndex == -1) {
            mKickingPartpIndex = position;
            final RcsCallMember callMember = mMultiCallMemberList.get(position);
            final String displayName = TextUtils.isEmpty(callMember.getDisplayName()) ? callMember.getNumber() : callMember.getDisplayName();
            AlertDialog.Builder b = new AlertDialog.Builder(this);
            b.setMessage(String.format(getString(R.string.kick_member_message), displayName));
            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String[] parts = {callMember.getNumber()};
                    mCurSession.removeParticipants(parts);
                }
            });
            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mKickingPartpIndex = -1;

                }
            });
            b.show();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            RcsCallSession callSession = mAnotherSession != null?mAnotherSession:mCurSession;
            if (callSession.isInCall()) {
                int direction = keyCode == KeyEvent.KEYCODE_VOLUME_UP ? AudioManager.ADJUST_RAISE : AudioManager.ADJUST_LOWER;
                int flags = AudioManager.FLAG_SHOW_UI;
                int streamType = AudioManager.STREAM_MUSIC;
                if (callSession.getState() > RcsCallSession.State.ALERTED) {
                    streamType = AudioManager.STREAM_MUSIC;
                }
                mAudioManager.adjustStreamVolume(streamType, direction, flags);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

//    @Override
//    public Bitmap getBackground() {
//        return null;
//    }
//
//    @Override
//    public void doodleViewDidShow() {
//        showOperationView(false);
//    }
//
//    @Override
//    public void doodleViewDidHide() {
//        showOperationView(true);
//    }
//
//    @Override
//    public void shareImageViewDidShow() {
//        showOperationView(false);
//    }
//
//    @Override
//    public void shareImageViewDidDismiss() {
//        showOperationView(true);
//    }

    @Override
    public void mtcBluetoothChanged() {
        int audio = AUDIO_BLUETOOTH;
        if (mMtcBluetoothHelper.getCount() == 0) {
            if (mAudio == AUDIO_BLUETOOTH) {
                mAudio = getDefaultAudio();
            } else {
                audio = mAudio;
            }
        }
    }

    @Override
    public void mtcHeadsetStateChanged(boolean plugged) {
        int audio = AUDIO_HEADSET;
        if (!plugged) {
            if (mAudio != AUDIO_HEADSET) return;
            audio = getDefaultAudio();
        }
        setAudio(audio);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    //电话监听Listener
    class InComingStateListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i(TAG, "onCallStateChanged: state"+state);
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if(mAnotherSession != null && mAnotherSession.hasHold()){
                        mAnotherSession.unhold();
                        return;
                    }
                    if (mCurSession != null && mCurSession.hasHold()) {
                        mCurSession.unhold();
                        if (mCurSession.isConference()) {
                            mMultiCallView.mBtnHold.setSelected(false);
                        } else {
                            mOneCallView.mBtnHold.setSelected(false);
                        }
                    }
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    if(mAnotherSession != null && !mAnotherSession.hasHold()){
                        mAnotherSession.hold();
                        return;
                    }
                    if (mCurSession != null && !mCurSession.hasHold()) {
                        mCurSession.hold();
                        if (mCurSession.isConference()) {
                            mMultiCallView.mBtnHold.setSelected(true);
                        } else {
                            mOneCallView.mBtnHold.setSelected(true);
                        }
                    }
                default:
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }

    }

    class CallAudioAdapter extends BaseAdapter {

        int mAudioArray[];

        CallAudioAdapter() {
            mAudioArray = new int[]{
                    mHeadsetPlugReceiver.mPlugged ? AUDIO_HEADSET : AUDIO_RECEIVER,
                    AUDIO_SPEAKER,
                    AUDIO_BLUETOOTH};
        }

        @Override
        public int getCount() {
            return mAudioArray.length;
        }

        @Override
        public Object getItem(int position) {
            return mAudioArray[position];
        }

        @Override
        public long getItemId(int position) {
            return mAudioArray[position];
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "getView: ");
            if (convertView == null) {
                convertView = LayoutInflater.from(TclCallActivity.this).inflate(android.R.layout.select_dialog_singlechoice, null);
            }
            CheckedTextView tv = (CheckedTextView) convertView.findViewById(android.R.id.text1);
            int audio = mAudioArray[position];
            if (audio == AUDIO_BLUETOOTH) {
                tv.setText(mMtcBluetoothHelper.mNameList.get(0));
            } else {
                tv.setText(AUDIO_STRINGS[audio]);
            }
            tv.setCompoundDrawablesWithIntrinsicBounds(AUDIO_DRAWABLES[audio], 0, 0, 0);
            tv.setCompoundDrawablePadding((int) (TclCallActivity.this.getResources().getDisplayMetrics().density * 10));
            tv.setChecked(audio == mAudio);
            return convertView;
        }
    }

    private int getDefaultAudio() {
        Log.i(TAG, "getDefaultAudio: ");
        if (mMtcBluetoothHelper.getCount() > 0)
            return AUDIO_BLUETOOTH;
        // Fix Bug 9138
//        if (mBtnAudio.isSelected())
//            return AUDIO_SPEAKER;

        if (mHeadsetPlugReceiver.mPlugged)
            return AUDIO_HEADSET;

        RcsCallSession session = mAnotherSession != null? mAnotherSession:mCurSession;
        if (session.isVideo()) {
            return AUDIO_SPEAKER;
        }
        return AUDIO_RECEIVER;
    }

    private void selectAudio() {
        final CallAudioAdapter audioAdapter = new CallAudioAdapter();
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAlertDialog = null;
                if (which < 0) return;
                int audio = audioAdapter.mAudioArray[which];
                setAudio(audio);
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setNegativeButton(R.string.canceled, listener);
        builder.setAdapter(audioAdapter, listener);
        mAlertDialog = builder.create();
        mAlertDialog.show();
    }

    private void setAudio(int audio) {
        switch (audio) {
            case AUDIO_RECEIVER:
            case AUDIO_HEADSET:
//                        mOperationLayer.setAudioImage(R.drawable.call_receiver_normal);
                mMtcBluetoothHelper.unlink(false);
                break;
            case AUDIO_SPEAKER:
//                        mOperationLayer.setAudioImage(R.drawable.call_speaker_normal);
                mMtcBluetoothHelper.unlink(true);
                break;
            case AUDIO_BLUETOOTH:
//                        mOperationLayer.setAudioImage(R.drawable.call_bluetooth_normal);
                mMtcBluetoothHelper.link(mMtcBluetoothHelper.mAddressList.get(0));
                break;
        }
        mAudio = audio;
    }

    public void onVideoChange(View v) {
        Log.i(TAG, "onVideoChange: ");
        Log.i(TAG, "onVideoChange: ");
        mCurSession.getVideoCallProvider().changeVideo(mLocalView, mRemoteView);
    }

    public static boolean isServiceRunning(Context context, String ServiceName) {
        Log.i(TAG, "isServiceRunning: ");
        if (TextUtils.isEmpty(ServiceName)) {
            return false;
        }
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }
    //0 移动 1 联通 2 电信
    /*
  中国电信号段：133、153、173、177、180、181、189、191、199
  中国联通号段:130、131、132、155、156、166、175、176、185、186
  中国移动号段:134(0-8)、135、136、137、138、139、147、150、151、
  152、157、158、159、178、182、183、184、187、188、198
     */
    private int checkOper(String number){
        Log.i(TAG, "checkOper: number " + number);
        String top = number.substring(0,3);
        Log.i(TAG, "checkOper: top"+top);
        if(top.equals("133") || top.equals("153") || top.equals("173") || top.equals("177") || top.equals("180")
                || top.equals("181") || top.equals("189") || top.equals("191") || top.equals("199")){
            return 2;
        }
        else if(top.equals("130") || top.equals("131") || top.equals("132") || top.equals("155") || top.equals("156")
                || top.equals("166") || top.equals("175") || top.equals("176") || top.equals("185") || top.equals("186")){
            return 1;
        }
        else if(top.equals("134") || top.equals("135") || top.equals("136") || top.equals("137") || top.equals("138")
                || top.equals("139") || top.equals("147") || top.equals("150") || top.equals("151") || top.equals("152")
                || top.equals("157") || top.equals("158") || top.equals("159") || top.equals("178") || top.equals("182")
                || top.equals("183") || top.equals("184") || top.equals("187") || top.equals("188") || top.equals("198")){
            return 0;
        }else return -1;
    }
    // public native int stoppthead();
    private void blow_up(View v) {
        float[] vaules = new float[] { 1.0f, 1.3f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(v, "scaleX", vaules),
                ObjectAnimator.ofFloat(v, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }
    //缩小按钮动画
    private void narrow(View v) {
        float[] vaules = new float[] { 1.3f,1.0f, };
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(v, "scaleX", vaules),
                ObjectAnimator.ofFloat(v, "scaleY", vaules));
        set.setDuration(150);
        set.start();
    }

}

