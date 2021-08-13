//package com.example.tclphone.db.tools;
//
//import com.example.tclphone.db.MyContactsHelper;
//import com.example.tclphone.db.MyRecordsHelper;
//
//import net.sqlcipher.Cursor;
//import net.sqlcipher.database.SQLiteDatabase;
//
//public class ImportTool {
//
//    /**
//     * 导入数据库方法
//     * @param dbName 数据库名
//     * @param password 数据库的加密密码，无密码的数据库请传入空字符串""
//     */
//    public static void importDatabase(String dbName,String password){
//
//        //打开需导入的数据库
//        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.tcl.tv5g/databases/"+dbName,password,null);
////        SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase("/data/data/com.tcl.tv5g/databases/de_phone.db","",null);
//
//        //导入联系人
//        Cursor cursor = db.query("contacts",null,null,null,null,null,null);
//        MyContactsHelper myContactsHelper = new MyContactsHelper();
//        if(cursor.moveToFirst()){
//            do{
//                String name = cursor.getString(cursor.getColumnIndex("name"));
//                String phoneNumber = cursor.getString(cursor.getColumnIndex("phonenumber"));
//                int storage = cursor.getInt(cursor.getColumnIndex("storage"));
//                int photo = cursor.getInt(cursor.getColumnIndex("photo"));
//                myContactsHelper.addContacts(name,phoneNumber,storage,photo);
//            }while (cursor.moveToNext());
//        }
//        cursor.close();
//
//        //导入通话记录
//        Cursor cursor2 = db.query("records",null,null,null,null,null,null);
//        MyRecordsHelper myRecordsHelper = new MyRecordsHelper();
//        if(cursor2.moveToFirst()){
//            do{
//                String phoneNumber = cursor2.getString(cursor2.getColumnIndex("phonenumber"));
//                String timeStart = cursor2.getString(cursor2.getColumnIndex("timestart"));
//                String duration = cursor2.getString(cursor2.getColumnIndex("duration"));
//                int mode = cursor2.getInt(cursor2.getColumnIndex("mode"));
//                int status = cursor2.getInt(cursor2.getColumnIndex("status"));
//                myRecordsHelper.copyRecords(phoneNumber,timeStart,duration,mode,status);
//            }while (cursor2.moveToNext());
//        }
//        cursor2.close();
//
//        db.close();
//    }
//
//}
