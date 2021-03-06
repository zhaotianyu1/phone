package com.example.tclphone.utils;

import android.text.TextUtils;
import android.util.Log;


public class LogUtils {

    /** Log输出的控制开关 */
    public static boolean isShowLog = true;
    /** 自己定义*/
    public static final String selfFlag = "com.tcl.tv5g---------";
    public static void i(Object objTag, String msg) {
        if (!isShowLog) {
            return;
        }
        String tag;

        // 如果objTag是String，则直接使用
        // 如果objTag不是String，则使用它的类名
        // 如果在匿名内部类，写this的话是识别不了该类，所以获取当前对象全类名来分隔
        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getName();
            String[] split = tag.split("\\.");
            tag=split[split.length-1].split("\\$")[0];
        }

        if (TextUtils.isEmpty(msg)) {
            Log.i(selfFlag.concat(tag), "该log输出信息为空");
        } else {
            Log.i(selfFlag.concat(tag), msg);
        }
    }

    /**
     * 错误调试信息
     * @param objTag
     * @param msg
     */
    public static void e(Object objTag, String msg) {
        if (!isShowLog) {
            return;
        }
        String tag;

        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getName();
            String[] split = tag.split("\\.");
            tag=split[split.length-1].split("\\$")[0];
        }

        if (TextUtils.isEmpty(msg)) {
            Log.e(selfFlag.concat(tag), "该log输出信息为空");
        } else {
            Log.e(selfFlag.concat(tag), msg);
        }
    }

    /**
     * 详细输出调试
     * @param objTag
     * @param msg
     */
    public static void v(Object objTag, String msg) {
        if (!isShowLog) {
            return;
        }
        String tag;

        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getName();
            String[] split = tag.split("\\.");
            tag=split[split.length-1].split("\\$")[0];
        }

        if (TextUtils.isEmpty(msg)) {
            Log.v(selfFlag.concat(tag), "该log输出信息为空");
        } else {
            Log.v(selfFlag.concat(tag), msg);
        }
    }

    /**
     * 警告的调试信息
     * @param objTag
     * @param msg
     */
    public static void w(Object objTag, String msg) {
        if (!isShowLog) {
            return;
        }
        String tag;
        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getName();
            String[] split = tag.split("\\.");
            tag=split[split.length-1].split("\\$")[0];
        }

        if (TextUtils.isEmpty(msg)) {
            Log.w(selfFlag.concat(tag), "该log输出信息为空");
        } else {
            Log.w(selfFlag.concat(tag), msg);
        }
    }

    /**
     * debug输出调试
     * @param objTag
     * @param msg
     */
    public static void d(Object objTag, String msg) {
        if (!isShowLog) {
            return;
        }
        String tag;
        if (objTag instanceof String) {
            tag = (String) objTag;
        } else if (objTag instanceof Class) {
            tag = ((Class) objTag).getSimpleName();
        } else {
            tag = objTag.getClass().getName();
            String[] split = tag.split("\\.");
            tag=split[split.length-1].split("\\$")[0];
        }
        if (TextUtils.isEmpty(msg)) {
            Log.d(selfFlag.concat(tag), "该log输出信息为空");
        } else {
            Log.d(selfFlag.concat(tag), msg);
        }
    }
}
