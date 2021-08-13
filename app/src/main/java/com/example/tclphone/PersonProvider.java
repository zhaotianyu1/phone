package com.example.tclphone;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tclphone.db.MyContactsHelper;
import com.example.tclphone.db.Person;
import com.juphoon.rcs.call.module.JusCallDelegate;

import com.example.tclphone.litepal.LitePal;

import java.util.List;

/**
 * 对外数据库接口提供
 */
public class PersonProvider  extends ContentProvider {

    private static String tag="ZTY_PersonProvider";
    //创建一个路径识别器
    //常量UriMatcher.NO_MATCH表示不匹配任何路径的返回码,也就是说如果找不到匹配的类型,返回-1
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int ALL_PERSON = 1;
    private static final int PERSON = 2;
    private static final int INSERT = 3;
    private static final int DELETE = 4;
    private static final int UPDATE = 5;
    private static final int OTHER = 6;


    static{

        uriMatcher.addURI("com.example.tclphone.PersonProvider","persons",ALL_PERSON);
        uriMatcher.addURI("com.example.tclphone.PersonProvider","person/#",PERSON);
        uriMatcher.addURI("com.example.tclphone.PersonProvider","other",OTHER);
        uriMatcher.addURI("com.example.tclphone.PersonProvider", "insert", INSERT);
        uriMatcher.addURI("com.example.tclphone.PersonProvider", "delete", DELETE);
        uriMatcher.addURI("com.example.tclphone.PersonProvider", "update", UPDATE);

    }


    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1, @Nullable String s1) {
        int result = uriMatcher.match(uri);
        switch(result){

            case ALL_PERSON:
                MyContactsHelper dao = new MyContactsHelper();
                Log.i(tag,"匹配规则1--------------!");
                List<Person> personList=dao.loadContact();
                Log.i(tag,"personList-----------------!:"+personList.size());
                SQLiteDatabase db = LitePal.getDatabase();
                Cursor cursor=db.query("person",null,null,null,null,null,null);
                return cursor;
            case OTHER:
                Log.i(tag,"其他匹配规则2--------------!");

//                JusCallDelegate.call(number, false);
                break;
            default:
                throw new RuntimeException("出错了!!");

        }
        return null;
    }


    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s, @Nullable String[] strings) {
        return 0;
    }
}
