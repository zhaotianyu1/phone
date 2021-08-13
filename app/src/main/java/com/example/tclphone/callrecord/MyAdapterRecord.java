package com.example.tclphone.callrecord;

import com.example.tclphone.utils.L;

/**
 * 通话记录工具
 */
public class MyAdapterRecord {

    private static final String TAG = "MyAdapterRecord";

    /**
     * 判定字符串是否为空
     * @param name
     * @return
     */
    public boolean is_blank(String name){
        if(name.equals("") || name.trim().length()==0){
            return true;
        }else{
            return false;
        }
    }




}
