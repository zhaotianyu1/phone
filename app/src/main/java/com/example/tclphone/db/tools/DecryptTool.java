//package com.example.tclphone.db.tools;
//
//import net.sqlcipher.database.SQLiteDatabase;
//
//import org.litepal.LitePal;
//
//import java.io.File;
//
//public class DecryptTool {
//
//    public static void decrypt(){
//        SQLiteDatabase db = LitePal.getDatabase();
//
//        //删除旧的解密文件
//        File file = new File("/data/data/com.tcl.tv5g/databases/de_phone.db");
//        file.delete();
//
//        db.rawExecSQL("ATTACH DATABASE '/data/data/com.tcl.tv5g/databases/de_phone.db' as de_phone KEY '';");
//        db.rawExecSQL("SELECT sqlcipher_export('de_phone');");
//        db.rawExecSQL("DETACH DATABASE de_phone;");
//    }
//
//}
//
