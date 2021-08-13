package com.example.tclphone.phonebook.copyadapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tclphone.R;
import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.utils.L;

import java.util.List;

/**
 * 复制界面本地用户列表适配器
 */
public class RecyclerAdapterPhone extends RecyclerView.Adapter<RecyclerAdapterPhone.PlateViewHolder> implements View.OnClickListener {

    private static final String TAG = "contactAdaptere Mem_CC";
    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();

    int []photos = new int[]{R.drawable.male01, R.drawable.female01,R.drawable.ic_popup_avatar_nor,R.drawable.ic_popup_avatar_focus,
            R.drawable.tongtong, R.drawable.xiesuo,
            R.drawable.duanguang, R.drawable.jinming, R.drawable.wenxin, R.drawable.kaiqi,
            R.drawable.leon, R.drawable.texin};
    private List<com.example.tclphone.phonebook.copyadapter.contact_copy> list;
    private List<Boolean> listCheck;
    private Context context;
    public  boolean isShow;
    public com.example.tclphone.phonebook.copyadapter.contact_copy contact_copy;
    private LayoutInflater mInflater;
    private boolean isSelect;
    private RecyclerView recyclerView;
    private PlateViewHolder holder;
    @Override
    public long getItemId(int position) {
        return list.get(position).hashCode();
    }
    public void addData(int pos, int id, String name, String phonenumber, int gender){

        L.i(TAG, "addData: ");
        list.add(pos,new contact_copy(id,name,phonenumber,gender));
        notifyItemInserted(pos);//通知演示插入动画
        notifyItemRangeChanged(pos,list.size()-pos);//通知数据与界面重新绑定

    }

    public RecyclerAdapterPhone(List<com.example.tclphone.phonebook.copyadapter.contact_copy> lists, Context context, RecyclerView recyclerView){
        this.list=lists;
        this.context=context;
        //遍历list设置序号
//        for(int i=0;i<list.size();i++){
//            String ids= String.valueOf(i+1);
//            list.get(i).setIds(ids);
//        }
        this.recyclerView = recyclerView;
        mInflater= LayoutInflater.from(context);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.tcl.tv5g.phonebook.copyadapter.RecyclerAdapterPhone");
        setHasStableIds(true);

    }



    @Override
    public PlateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.contact_list_item_layout_check,parent,false);
        holder=new PlateViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(PlateViewHolder holder, final int position) {

        //获取头像信息
        Drawable draPhoto= context.getResources().getDrawable(photos[list.get(position).getPhoto()]);
        holder.peo_photo_contact.setBackground(draPhoto);
        String name=null;
        String number=null;
        //获取联系人的信息
        name  = list.get(position).getName();
        number=list.get(position).getNumber();
        isShow=list.get(position).getIsshow();
        //展示联系人的信息详情
        holder.textView.setText(name);
        holder.phoneView.setText(number);
//        holder.copy_ids.setText(list.get(position).getIds());
        L.i(TAG, "onBindViewHolder--isShow : "+isShow);
        //如果true，显示背景颜色，否则不显示背景颜色
//        if(isShow){
//            holder.checkBox.setVisibility(View.VISIBLE);
//        }else {
//            holder.checkBox.setVisibility(View.GONE);
//
//        }
        //监听按键操作
        if(onItemClickListener!=null){
            //监听确认键操作
            holder.itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    onItemClickListener.setOnClick(position);
                }
            });
            holder.itemView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View view, int i, KeyEvent keyEvent) {

                    onItemClickListener.setKey(position,keyEvent);
                    return false;
                }
            });
            holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        holder.textView.setTextColor(Color.BLACK);
//                        holder.copy_ids.setTextColor(Color.BLACK);
                        holder.phoneView.setTextColor(Color.BLACK);
                       // ofFloatAnimator(holder.itemView, 1f, 1.02f);//放大
                        Drawable draPhoto= context.getResources().getDrawable(photos[3]);
                        holder.peo_photo_contact.setBackground(draPhoto);

                        int[] amount = getScrollAmount(recyclerView, view);//计算需要滑动的距离
                        recyclerView.smoothScrollBy(amount[0], amount[1]);
                    }else{
                        holder.textView.setTextColor(Color.WHITE);
//                        holder.copy_ids.setTextColor(Color.WHITE);
                        holder.phoneView.setTextColor(Color.WHITE);
                        Drawable draPhoto= context.getResources().getDrawable(photos[list.get(position).getPhoto()]);
                        holder.peo_photo_contact.setBackground(draPhoto);
                       // ofFloatAnimator(holder.itemView, 1.02f, 1f);
                    }
                }
            });
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
    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onClick(View v) {

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

    public class PlateViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener{
        //初始化控件的信息
        private TextView textView;
       // private CheckBox checkBox;
        private LinearLayout rootView;
        private TextView phoneView;
        private ImageButton peo_photo_contact;
        private int position;
//        private TextView copy_ids;
        public PlateViewHolder(View itemView) {
            super(itemView);
            rootView= (LinearLayout) itemView.findViewById(R.id.copy_contact);
            textView= (TextView) itemView.findViewById(R.id.con_list);
            phoneView=(TextView) itemView.findViewById(R.id.phone_copy);
           // checkBox= (CheckBox) itemView.findViewById(R.id.item_checkbox);
//            copy_ids=itemView.findViewById(R.id.copy_ids);
            peo_photo_contact=(ImageButton)itemView.findViewById(R.id.peo_photo_contact);
         //   checkBox.setOnCheckedChangeListener(this);
            rootView.setOnClickListener(this);
            rootView.setOnLongClickListener(this);

        }

        @Override
        public boolean onLongClick(View v) {
            if(onItemClickListener!=null){
                return  onItemClickListener.setOnItemLongClick(position);
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            if(onItemClickListener!=null){
//                if(checkBox.isChecked()){
//                    checkBox.setChecked(false);
//                    onItemClickListener.setOnItemClick(position,false);
//                }else {
//                    checkBox.setChecked(true);
//                    onItemClickListener.setOnItemClick(position,true);
//                }
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//            if(onItemClickListener!=null){
//                onItemClickListener.setOnItemCheckedChanged(position,isChecked);
//            }
        }
    }

    public interface OnItemClickListener {
        void setOnItemClick(int position, boolean isCheck);
        boolean setOnItemLongClick(int position);
//        void setOnItemCheckedChanged(int position, boolean isCheck);
        void setOnClick(int position);
        void setKey(int position,KeyEvent keyEvent);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener=onItemClickListener;
    }

}

