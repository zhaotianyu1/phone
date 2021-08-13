package com.example.tclphone.phonebook.contactadapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Adapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;

import com.example.tclphone.R;
import com.example.tclphone.callrecord.MyAdapterRecord;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.Contacts;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.phonebook.util.CenterLayoutManager;
import com.example.tclphone.utils.L;
import com.tcl.ff.component.animer.glow.view.AllCellsGlowLayout;
import com.tcl.uicompat.TCLItemLarge;

import org.json.JSONException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.tcl.ff.component.animer.glow.view.utils.DensityUtil.dp2px;
import static com.example.tclphone.litepal.LitePalApplication.getContext;

//import com.example.tclphone.activity.editContactActivity;

/**
 * 主界面电话簿列表适配器
 */
public class ContactAdaptere extends RecyclerView.Adapter<ContactAdaptere.ContactViewHolder> implements RecyclerView.OnItemTouchListener {

    private static final String TAG = "contactAdaptere";

    private LayoutInflater mInflater;//布局服务
    private Context mContext;
    private List<Contact> mData;//联系人实体列表对象

    AllCellsGlowLayout glowLayout=new AllCellsGlowLayout(getContext());
    private int mCurPosition;//当前item的位置
    private RecyclerView recyclerView;

    int[] photos = new int[]{R.drawable.male01, R.drawable.female01, R.drawable.list_avatar_nor,R.drawable.list_avatar_focus,R.drawable.list_avatar_selected};//头像

    @Override
    public long getItemId(int position) {
        return mData.get(position).hashCode();
    }


    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    //定义监听接口
    public interface OnItemClickListener {

        void onItemClick(int position);
        void onFocusLiterner(View view, boolean b, int position);
        void onItemLeftClick(int keyCode, View view, int position);
        void onItemLongClick(View view, int position);

    }

    private static OnItemClickListener onItemClickListener;

    public static void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    private static OnItemRightClickListener onItemRightClickListener;

    public interface OnItemRightClickListener {

        void onKeyRight(int keyCode, View view, int position);

    }

    public static void setOnItemRightClickListener(OnItemRightClickListener listener) {
        onItemRightClickListener = listener;
    }

    //构造函数，初始化实体列表
    public ContactAdaptere(Context context, List<Contact> Data, RecyclerView recyclerView) throws IOException, JSONException {


        L.i(TAG, "contactAdapterextends:");
        this.mContext = context;
        mInflater = LayoutInflater.from(context);
        mData = new ArrayList<>();
        //将读取到的数据添加到mData中
        mData = Data;


        //循环设置列表的序号
        for(int i=0;i<mData.size();i++){
            String ids= String.valueOf(i+1);
            mData.get(i).setIds(ids);
        }
        this.recyclerView=recyclerView;

        setHasStableIds(true);
        L.i(TAG, "contactAdapterextends: end");

    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {

        View view = mInflater.inflate(R.layout.contact_list_item_layout, viewGroup, false);
        ContactViewHolder viewHolder = new ContactViewHolder(view);
        if (onItemClickListener != null) {
//            viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//                @Override
//                public void onFocusChange(View view, boolean b) {
//
//                }
//            });
            //点击确定键后的操作逻辑
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    //  myViewHolder.itemView.setBackgroundColor(Color.WHITE);
                    onItemClickListener.onItemClick(position);
                }
            });
            //按键的操作逻辑
            viewHolder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    //监听左键操作
                    if (event.getAction() == KeyEvent.KEYCODE_DPAD_LEFT) {
                        keyCode = KeyEvent.KEYCODE_DPAD_LEFT;

                    }
                    //监听下键操作
                    if (event.getAction() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        keyCode = KeyEvent.KEYCODE_DPAD_DOWN;

                    }
                    //监听右键操作
                    if (event.getAction() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                        keyCode = KeyEvent.KEYCODE_DPAD_RIGHT;


                    }
                    L.i(TAG, "onBindViewHolder:keyCode " + keyCode);
                    onItemClickListener.onItemLeftClick(keyCode, viewHolder.itemView, position);
                    return false;
                }
            });

            //焦点聚焦的操作逻辑
            viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        L.i(TAG, "onFocusChange------------");
                        if(mData.get(position).getSign()!=1) {
                            viewHolder.tv.setTextColor(Color.BLACK);
                        }
                        if(mData.get(position).getPhoto() == 2){
                            //获取头像信息
                            Drawable draPhoto = mContext.getResources().getDrawable(photos[3]);
                            viewHolder.photo.setBackground(draPhoto);
                        }

                        viewHolder.right_log.setBackgroundResource(R.drawable.right_arrow_press);
                      //  onItemClickListener.onFocusLiterner(view,b,position);
                        int[] amount = getScrollAmount(recyclerView, view);//计算需要滑动的距离
                        recyclerView.smoothScrollBy(amount[0], amount[1]);
                        L.i(TAG, "计算需要滑动的距离:keyCode ");
                    }else {
                        Log.i(TAG,"没有聚焦");

                        viewHolder.right_log.setBackgroundResource(R.drawable.right_arrow);

                        if(mData.get(position).getSign()!=1){
                            Log.i(TAG,"sign:0");
                            Drawable draPhoto = mContext.getResources().getDrawable(photos[mData.get(position).getPhoto()]);
                            viewHolder.photo.setBackground(draPhoto);
                            viewHolder.tv.setTextColor(RecordConstants.WHITE_99);
                            viewHolder.itemView.setBackgroundColor(RecordConstants.BLACK_0F);
                        }
                        if(mData.get(position).getSign()==1){
                            Log.i(TAG,"sign:1");
                            viewHolder.itemView.setBackgroundColor(RecordConstants.BLACK_1F);
                            viewHolder.tv.setTextColor(RecordConstants.WHITE_E6);
                            //获取头像信息
                            Drawable draPhoto = mContext.getResources().getDrawable(photos[4]);
                            viewHolder.photo.setBackground(draPhoto);
                        }

                    }

                }
            });
        }
        return viewHolder;
    }

    private int childCount = 0;

    private int middlechild = 0;

    @Override
    public void onBindViewHolder(@NonNull final ContactViewHolder myViewHolder, final int position) {

        L.i(TAG, "position------------"+position);
        L.i(TAG, "position------------"+mData.size());
        if(position == mData.size()-1 && mData.size()>4) {
            L.i(TAG, "到底部了------------");
            myViewHolder.itemView.setVisibility(View.GONE);
            return ;
        }

        L.i(TAG, "onBindViewHolder: mData.get(i) " + mData.get(position));
        String name = null;

        //获取头像信息
        Drawable draPhoto = mContext.getResources().getDrawable(photos[mData.get(position).getPhoto()]);
        myViewHolder.photo.setBackground(draPhoto);
        L.i(TAG, "photo " + photos[mData.get(position).getPhoto()]);

        MyAdapterRecord myAdapterRecord=new MyAdapterRecord();

        //获取名称或号码字段值
        if (myAdapterRecord.is_blank(mData.get(position).getName())) {
            name = mData.get(position).getNumber();
        } else {
            name = mData.get(position).getName();
        }
        //将字段值展示在View中
        myViewHolder.tv.setText(name);
        myViewHolder.tv.setTextColor(RecordConstants.WHITE_99);
        myViewHolder.tv.setGravity(Gravity.CENTER_VERTICAL);

        if(mData.get(position).getSign()==1){
        //    myViewHolder.itemView.setBackgroundResource(R.drawable.blocks);
            glowLayout.setNeedFocus(true);
            glowLayout.setNeedGlowAnim(true);
        }



    }


    private void ofFloatAnimator(View view,float start,float end){
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);//动画时间
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", start, end);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", start, end);
        animatorSet.setInterpolator(new DecelerateInterpolator());//插值器
        animatorSet.play(scaleX).with(scaleY);//组合动画,同时基于x和y轴放大
        animatorSet.start();
    }

    private static int[] getScrollAmount(RecyclerView recyclerView, View view) {
        int[] out = new int[2];
        int position = recyclerView.getChildAdapterPosition(view);
        final int parentLeft = recyclerView.getPaddingLeft();
        final int parentTop = recyclerView.getPaddingTop();
        final int parentRight = recyclerView.getWidth() - recyclerView.getPaddingRight();
        final int parentBottom = recyclerView.getHeight() - recyclerView.getPaddingBottom();
        final int childLeft = view.getLeft() - view.getScrollX();
        final int childTop = view.getTop() - view.getScrollY();

        //item左边距减去Recyclerview不在屏幕内的部分，加当前Recyclerview一半的宽度就是居中
        final int dx = childLeft - parentLeft - ((parentRight - view.getWidth()) / 2);

        //同上
        final int dy = childTop - parentTop - (parentBottom - view.getHeight()) / 2;
        out[0] = dx;
        out[1] = dy;
        return out;


    }

    private void scrollToAmount(RecyclerView recyclerView, int dx, int dy) {
        //如果没有滑动速度等需求，可以直接调用这个方法，使用默认的速度
        // recyclerView.smoothScrollBy(dx,dy);

        //以下对滑动速度提出定制
        try {
            Class recClass = recyclerView.getClass();
            Method smoothMethod = recClass.getDeclaredMethod("smoothScrollBy", int.class, int.class, Interpolator.class, int.class);
            smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 100);//时间设置为700毫秒，
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }




    private int mInsideViewWidth = dp2px(getContext(), 70);
    private int mInsideViewHeight = dp2px(getContext(), 40);
    private int mLightDiffusionWidth = dp2px(getContext(), 20);  //扩散区宽度
    private int[] colors = {Color.parseColor("#9FB6FF"), Color.parseColor("#A7BAFE"), Color.parseColor("#F26882"), Color.parseColor("#F8DF57")};//变动颜色

    class ContactViewHolder extends  RecyclerView.ViewHolder {

        TextView tv;
        ImageView photo;
        TextView ids;
        ImageView right_log;


        public ContactViewHolder(@NonNull View itemView) {

            super(itemView);
            tv = itemView.findViewById(R.id.con_list);
            photo = itemView.findViewById(R.id.peo_photo_contact);
            right_log = itemView.findViewById(R.id.right_log);
            // ids=itemView.findViewById(R.id.ids);

        }
    }
}



