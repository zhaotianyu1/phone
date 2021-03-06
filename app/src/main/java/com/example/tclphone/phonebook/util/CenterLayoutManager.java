package com.example.tclphone.phonebook.util;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by iblade.Wang on 2019/5/22 17:08
 */
public class CenterLayoutManager extends LinearLayoutManager {
    public CenterLayoutManager(Context context) {
        super(context);
    }

    public CenterLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public CenterLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        RecyclerView.SmoothScroller smoothScroller = new CenterSmoothScroller(recyclerView.getContext());
        smoothScroller.setTargetPosition(position);
        startSmoothScroll(smoothScroller);
    }
    private boolean isScrollEnabled = true;
    private static class CenterSmoothScroller extends LinearSmoothScroller {

        public CenterSmoothScroller(Context context) {
            super(context);
        }

        @Override
        public int calculateDtToFit(int viewStart, int viewEnd, int boxStart, int boxEnd, int snapPreference) {
            return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2);
        }

        @Override
        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return 100f / displayMetrics.densityDpi;
        }
    }
    public void setScrollEnabled(boolean flag) {
        this.isScrollEnabled = flag;
    }

    @Override
    public boolean canScrollVertically() {
        return isScrollEnabled && super.canScrollVertically();
    }

//    private boolean isUp = true;
//    private boolean isDown = true;
//    private long mLastKeyDownTime = 0;
//    private boolean isLeft = false;
//    private boolean isRight = false;
//    private int orientation = VERTICAL;
//
//    @Override
//    public View onInterceptFocusSearch(View focused, int direction) {
//
//        long current = System.currentTimeMillis();
//        if (current - mLastKeyDownTime < 100) {//??????????????????
//            return focused;
//        } else {
//            mLastKeyDownTime = current;
//            if (getFocusedChild() != null) {
//                int pos = getPosition(getFocusedChild());//????????????????????????
////            int pos = getPosition(focused);//????????????????????????
//                int count = getItemCount();
//                int lastVisiblePosition = findLastVisibleItemPosition();
//                switch (direction) {
//                    case View.FOCUS_LEFT:
//                        if (isLeft) {
//                            if (orientation == VERTICAL) {
//                                return focused;
//                            } else {
//                                if (pos == 0) {
//                                    return focused;
//                                }
//                            }
//                        }
//                        break;
//                    case View.FOCUS_RIGHT:
//                        if (isRight) {
//                            if (orientation == VERTICAL) {
//                                return focused;
//                            } else {
//                                if (pos == count - 1) {
//                                    return focused;
//                                }
//                            }
//                        }
//                        break;
//                    case View.FOCUS_UP:
//                        if (isUp) {
//                            if (orientation == VERTICAL) {
//                                if (pos == 0) {
//                                    return focused;
//                                }
//                            } else {
//                                return focused;
//                            }
//                        }
//                        break;
//                    case View.FOCUS_DOWN:
//                        if (isDown) {
//                            if (orientation == VERTICAL) {
//                                if (pos == count - 1) {
//                                    return focused;
//                                }
//                            } else {
//                                return focused;
//                            }
//                        }
//                        break;
//                }
//                if (pos > getItemCount() - 1) {
//                    return focused;
//                } else if (pos > lastVisiblePosition) {
//                    scrollToPosition(pos);
//                }
//            }
//            return super.onInterceptFocusSearch(focused, direction);
//        }
//    }
//    @Override
//    public View onInterceptFocusSearch(View focused, int direction) {
//        int count = getItemCount();//??????item?????????
//        int fromPos = getPosition(focused);//?????????????????????
//        int lastVisibleItemPos = findLastVisibleItemPosition();//?????????????????????Item?????????
//        switch (direction) {//????????????????????????position
//            case View.FOCUS_RIGHT:
//                fromPos++;
//                break;
//            case View.FOCUS_LEFT:
//                fromPos--;
//                break;
//        }
//
//        Log.i("zzz", "onInterceptFocusSearch , fromPos = " + fromPos + " , count = " + count+" , lastVisibleItemPos = "+lastVisibleItemPos);
//        if(fromPos < 0 || fromPos >= count ) {
//            //?????????????????????<0,????????????item??????????????????????????????View??????????????????
//            return focused;
//        } else {
//            //????????????????????????????????????????????????item????????????????????????View?????????????????????????????????????????????????????????????????????????????????
//            if (fromPos > lastVisibleItemPos) {
//                scrollToPosition(fromPos);
//            }
//        }
//        return super.onInterceptFocusSearch(focused, direction);
//    }
}
