package com.example.tclphone.callrecord;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Interpolator;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.R;
import com.example.tclphone.constants.RecordConstants;
import com.example.tclphone.db.MyRecordsHelper;
import com.example.tclphone.db.Records;
import com.example.tclphone.utils.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private static final String TAG = "RecordAdapter";
    private int positions=0;
    private LayoutInflater mInflater;
    private Context mContext;
    private List<Records> mData ;
    boolean flag;

    private RecyclerView recyclerView;

    int []photo = new int[]{R.drawable.male01, R.drawable.female01, R.drawable.ic_popup_avatar_nor, R.drawable.ic_popup_avatar_focus,R.drawable.user1,
            R.drawable.tongtong, R.drawable.xiesuo,
            R.drawable.duanguang, R.drawable.jinming, R.drawable.wenxin, R.drawable.kaiqi,
            R.drawable.leon, R.drawable.texin};
    int []mode = new int[]{R.drawable.ic_call_out_nor, R.drawable.ic_call_in_nor, R.drawable.ic_video_out_nor, R.drawable.ic_video_in_nor};
    int []mode_press=new int[]{R.drawable.ic_call_out_focus, R.drawable.ic_call_in_focus, R.drawable.ic_video_out_focus ,R.drawable.ic_video_in_focus};


    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onItemLeftClick(int keyCode, View view, int position);
        void onFocusLiterner(View view, boolean b, int position);

    }


    private static OnItemClickListener onItemClickListener;

    public static void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public MyAdapter(Context context, List<Records> Data, RecyclerView recyclerView) throws IOException, JSONException {
        L.i(TAG, "MyAdapter: ");
        this.mContext=context;

        mInflater=LayoutInflater.from(context);
        mData = new ArrayList<>();
        mData = Data;

        getRecord();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.tclphone.callrecord.adapter.MyAdapter");
        this.recyclerView=recyclerView;

        setHasStableIds(true);
    }

//    @Override
//    public long getItemId(int position) {
//        return position;
//
//    }

    public  boolean getFlag(){
        return flag;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {


        View view=mInflater.inflate(R.layout.record_list_item_layout,viewGroup,false);
        MyViewHolder viewHolder=new MyViewHolder(view);

        if(onItemClickListener!=null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){
                    onItemClickListener.onItemClick(viewHolder.itemView,position);
                }
            });


            viewHolder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {

                        keyCode = KeyEvent.KEYCODE_DPAD_LEFT;
//                        L.i(TAG, "onFocusChange: keyCode = " + keyCode);
                        onItemClickListener.onItemLeftClick(keyCode, viewHolder.itemView, position);
                     }
                    else if(is_tag && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && position==0) {
//                        L.i(TAG, "event: is_tag =2::: " + is_tag);
                        if(event.getKeyCode()== KeyEvent.KEYCODE_DPAD_LEFT){
                            onItemClickListener.onItemLongClick(viewHolder.itemView, position);
                            is_cc=0;
                            is_tag=false;
//                            L.i(TAG, "event: is_tag =2:::1 " );
                        }else if(is_cc==1){
                            onItemClickListener.onItemLongClick(viewHolder.itemView, position);
                            is_cc=0;
                            is_tag=false;
//                            L.i(TAG, "event: is_tag =2::: 2" );
                        }else{
                            is_cc=1;
//                            L.i(TAG, "event: is_tag =2::: 3" );
                        }
                        return false;
                    }else if(is_tag && event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN && position==0){
                        is_cc=1;
                    }
//                    L.i(TAG, "---------------------::: 7");
                    return false;
                }

            });

            viewHolder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b){
                        L.i(TAG, "onFocusChange------------:"+position);
                        positions = position;
                        viewHolder.name.setTextColor(Color.BLACK);
                        viewHolder.duration.setTextColor(Color.BLACK);
                        type=mData.get(position).getMode();
                        viewHolder.mode.setImageResource(mode_press[type]);
                        if(getFlag()){
                            viewHolder.absolute.setTextColor(Color.BLACK);
                        }else{
                            viewHolder.date.setTextColor(Color.BLACK);
                        }
                        if(position==0) {
                            is_tag = true;
                            is_cc=0;
                        }

                        ofFloatAnimator(viewHolder.itemView, 1f, 1.02f);//放大
                        int[] amount = getScrollAmount(recyclerView, view);//计算需要滑动的距离
                        recyclerView.smoothScrollBy(amount[0], amount[1]);
                    }else{
                        type=mData.get(position).getMode();
                        viewHolder.mode.setImageResource(mode[type]);
                        viewHolder.duration.setTextColor(RecordConstants.WHITE_4D);
                        viewHolder.name.setTextColor(RecordConstants.WHITE_99);

                        if(getFlag()){
                            viewHolder.absolute.setTextColor(RecordConstants.WHITE_4D);
                        }else{
                            viewHolder.date.setTextColor(RecordConstants.WHITE_4D);
                        }
                        if(position==0){
//                            L.i(TAG, "---------------------::: 1");
                            is_cc=1;
                        }

                        ofFloatAnimator(viewHolder.itemView, 1.02f, 1f);
//                        L.i(TAG, "---------------------::: 2" + is_tag);
                    }
                }
            });
        }
        return viewHolder;
    }

    int is_cc=0;
    int type=0;
    boolean is_tag=false;
    @Override
    @SuppressLint("ResourceAsColor")
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, final int position) {

      //  L.i(TAG, "position------------:"+position);
        L.i(TAG, "mData.size------------:"+mData.size());
        L.i(TAG, "mData.get(position).getId()------------:"+mData.get(position).getId());
        if(mData.get(position).getId()==5001 && mData.size()>6) {
            L.i(TAG, "到底部了------------");
            myViewHolder.itemView.setVisibility(View.GONE);
            return ;
        }

        L.i(TAG, "onBindViewHolder: mData.get(i) " + mData.get(position));

        MyAdapterRecord myAdapterRecord=new MyAdapterRecord();
        if(myAdapterRecord.is_blank(mData.get(position).getName())){
            myViewHolder.name.setText(mData.get(position).getPhoneNumber());
        }else{
            myViewHolder.name.setText(mData.get(position).getName());
        }
        if(getFlag()){
            //绝对时间
            myViewHolder.absolute.setVisibility(View.VISIBLE);
            myViewHolder.absolute.setText(mData.get(position).getTimeStart());
            myViewHolder.date.setVisibility(View.GONE);
            myViewHolder.absolute.setTextColor(RecordConstants.WHITE_4D);
        }else{
            //相对时间
            myViewHolder.absolute.setVisibility(View.GONE);
            myViewHolder.date.setVisibility(View.VISIBLE);
            myViewHolder.date.setText(mData.get(position).getTimeStart());
            myViewHolder.date.setTextColor(RecordConstants.WHITE_4D);
        }
        myViewHolder.duration.setText(mData.get(position).getDuration());

        myViewHolder.mode.setImageResource(mode[mData.get(position).getMode()]);

        myViewHolder.duration.setTextColor(RecordConstants.WHITE_4D);
        myViewHolder.name.setTextColor(RecordConstants.WHITE_99);


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
            smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 200);//时间设置为700毫秒，
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
        return mData.size();
    }

    public void deleteData(int pos){
        mData.remove(pos);
        notifyItemRemoved(pos);
    }

    public void getRecord() throws IOException, JSONException {
        MyRecordsHelper myRecordsHelper = new MyRecordsHelper();

        Map<String ,Object> map=myRecordsHelper.loadRecords();
        this.flag= (boolean) map.get("is_flag");
        L.i(TAG,"flag-------------------"+flag);
     //   mData.addAll(recordsList);
//
//        L.i(TAG,"开始读取JSON数据");
//
//        StringBuilder stringBuilder = new StringBuilder();
//        InputStream is = mContext.getAssets().open("record.json");
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
//        String line;
//        while ((line=bufferedReader.readLine()) != null) {
//            stringBuilder.append(line);
//        }
//        L.i(TAG, "getRecord: stringBuilder"+stringBuilder.toString());
//        is.close();
//        bufferedReader.close();
//
//        JSONObject testJson = new JSONObject(stringBuilder.toString()); // 从builder中读取了json中的数据。
//        // 直接传入JSONObject来构造一个实例
//        JSONArray array = testJson.getJSONArray("record");
//        L.i("Record",array.toString());
//
//        for (int i = 0;i<array.length();i++){
//            JSONObject jsonObject = array.getJSONObject(i);
//            int id = jsonObject.getInt("id");
//            String name = jsonObject.getString("name");
//            String phoneNumber = jsonObject.getString("phoneNumber");
//            String time_start = jsonObject.getString("timeStart");
//            String duration = jsonObject.getString("duration");
//            int mode = jsonObject.getInt("mode");
//            int status = jsonObject.getInt("status");
//            int photo = jsonObject.getInt("photo");
//
//        }
    }


}



class MyViewHolder extends RecyclerView.ViewHolder {

    TextView name;
    TextView duration;
    TextView date;
    //ImageButton photo;
    ImageView mode;
    TextView absolute;


    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
       // photo = itemView.findViewById(R.id.record_photo);
        name = itemView.findViewById(R.id.record_name);
        date = itemView.findViewById(R.id.record_date);
        duration = itemView.findViewById(R.id.record_duration);
        mode = itemView.findViewById(R.id.record_mode);
        absolute=itemView.findViewById(R.id.absolute_data);
       // operator = itemView.findViewById(R.id.record_operator);
    }
}
