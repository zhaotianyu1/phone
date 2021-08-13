//package com.example.tclphone.phonebook.contactadapter;
//
//import android.graphics.Interpolator;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.tclphone.R;
//
//import java.lang.reflect.Method;
//
//
///**
// * RecyclerView控件初始化
// */
//public class ContactViewHolder extends  RecyclerView.ViewHolder {
//
//    TextView tv;
//    ImageButton photo;
//    TextView ids;
//    ImageView right_log;
//
//    public ContactViewHolder(@NonNull View itemView) {
//
//        super(itemView);
//        tv = itemView.findViewById(R.id.con_list);
//        photo = itemView.findViewById(R.id.peo_photo_contact);
//        right_log=itemView.findViewById(R.id.right_log);
//       // ids=itemView.findViewById(R.id.ids);
//        itemView.setFocusable(true);
//        itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(b){
//                    int[] amount = getScrollAmount(recyclerView, view);//计算需要滑动的距离
//                    //滑动到指定距离
//                    scrollToAmount(recyclerView, amount[0], amount[1]);
//
//                    itemView.setTranslationZ(20);//阴影
//                    ofFloatAnimator(itemView,1f,1.3f);//放大
//                }else {
//                    itemView.setTranslationZ(0);
//                    ofFloatAnimator(itemView,1.3f,1f);
//                }
//            }
//
//        });
//    }
//    //根据坐标滑动到指定距离
//    private void scrollToAmount(RecyclerView recyclerView, int dx, int dy) {
//        //如果没有滑动速度等需求，可以直接调用这个方法，使用默认的速度
////                recyclerView.smoothScrollBy(dx,dy);
//
//        //以下对滑动速度提出定制
//        try {
//            Class recClass = recyclerView.getClass();
//            Method smoothMethod = recClass.getDeclaredMethod("smoothScrollBy", int.class, int.class, Interpolator.class, int.class);
//            smoothMethod.invoke(recyclerView, dx, dy, new AccelerateDecelerateInterpolator(), 700);//时间设置为700毫秒，
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    /**
//     * 计算需要滑动的距离,使焦点在滑动中始终居中
//     * @param recyclerView
//     * @param view
//     */
//    private int[] getScrollAmount(RecyclerView recyclerView, View view) {
//        int[] out = new int[2];
//        final int parentLeft = recyclerView.getPaddingLeft();
//        final int parentTop = recyclerView.getPaddingTop();
//        final int parentRight = recyclerView.getWidth() - recyclerView.getPaddingRight();
//        final int childLeft = view.getLeft() + 0 - view.getScrollX();
//        final int childTop = view.getTop() + 0 - view.getScrollY();
//
//        final int dx =childLeft - parentLeft - ((parentRight - view.getWidth()) / 2);//item左边距减去Recyclerview不在屏幕内的部分，加当前Recyclerview一半的宽度就是居中
//
//        final int dy = childTop - parentTop - (parentTop - view.getHeight()) / 2;//同上
//        out[0] = dx;
//        out[1] = dy;
//        return out;
//
//    }
//}
//
//
