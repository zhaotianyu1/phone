package com.example.tclphone.db;

//import android.util.Log;

//import com.tcl.tv5g.phonebook.util.AesUtil;

import android.renderscript.Sampler;
import android.util.Log;

import com.example.tclphone.litepal.crud.DataSupport;
import com.example.tclphone.phonebook.contactadapter.Contact;
import com.example.tclphone.utils.L;




import java.security.Key;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class MyRecordsHelper {

    //定义常量：
    private static final String TAG = "PZR_AL";
    //通话方式mode
    public static final int VOICE_DIAL = 0;
    public static final int VOICE_ANSWER = 1;
    public static final int VIDEO_DIAL = 2;
    public static final int VIDEO_ANSWER = 3;

    //通话是否成功status
    public static final int FAILED = 0;
    public static final int SUCCEEDED = 1;

    String receive_phoneNumber;


//    String key = "tcl.tv5g";//加密唯一标识

    /**
     * 添加通话记录接口
     * @param phoneNumber 电话号码
     * @param timeStart 拨打时间 暂定格式：yyyy-MM-dd HH:mm:ss
     * @param timeEnd 挂断时间 暂定格式：yyyy-MM-dd HH:mm:ss
     * @param mode 通话方式
     * @param status 通话是否成功 0-未接通 1-接通
     * @return 返回该条通话记录的id，若插入不成功返回-1
     */
    public int communicate(String phoneNumber,String timeStart,String timeEnd,int mode,int status){

        //前端拨号盘自动约束了电话号码
        //约束mode
        if(mode!=VOICE_DIAL && mode!=VOICE_ANSWER && mode!=VIDEO_DIAL && mode!=VIDEO_ANSWER){
            L.i("MyRecordsHelper","产生通话记录失败。参数mode = "+mode+"不合法！");
            return -1;
        }
        //约束status
        if(status!=FAILED && status!=SUCCEEDED){
            L.i("MyRecordsHelper","产生通话记录失败。参数status = "+status+"不合法！");
            return -1;
        }

        Map<String ,Object> map = loadRecords();
        List<Records> recordsList= (List<Records>) map.get("lists");
        if(recordsList==null)
        {
            L.i("MyRecordsHelper","recordList is null");
        }else {
            L.i("MyRecordsHelper","records-----:"+recordsList.size());
        }


        try {
            if (recordsList.size() > 300) {
                deleteRecords(recordsList.get(recordsList.size()-1).getId());
                L.i("MyRecordsHelper","delete_record--success");
            }
        }catch (Exception ex){
            L.i("MyRecordsHelper","delete_record--fail");
            ex.printStackTrace();
        }

        //计算时间差
        long day = -1;
        long hour = -1;
        long minute = -1;
        long second = -1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        try{
            Date dateStart = simpleDateFormat.parse(timeStart);
            Date dateEnd = simpleDateFormat.parse(timeEnd);
            long time = dateEnd.getTime()-dateStart.getTime();//时间差

            //若拨打时间后于挂断时间，打印日志，退出添加通话记录
            if(time < 0){
                L.i("MyRecordsHelper","产生通话记录失败。传入时间错误,拨打时间或接通时间后于挂断时间。");
                L.i("MyRecordsHelper","timeStart："+timeStart);
                L.i("MyRecordsHelper","timeEnd："+timeEnd);
                return -1;
            }
            second = time/1000;//秒
            minute = second/60;//分
            hour = minute/60;//时
            day = hour/24;//天
        }
        catch (ParseException e){
            e.printStackTrace();
            L.i("MyRecordsHelper","产生通话记录失败。解析时间异常，请检查传入时间字符串是否符合格式。");
            return -1;
        }

        //转换持续时间的格式
        String duration;
        if(second == 0) duration = "1秒";//未满1秒算1秒
        else if(minute == 0) duration = second+"秒";
        else if(hour == 0) duration = minute+"分"+second%60+"秒";
        else if(day == 0) duration = hour+"小时"+minute%60+"分"+(second%(60*60))%60+"秒";
        else duration = day+"天"+hour%24+"小时"+(minute%(60*24))%60+"分"+((second%(60*60*24))%(60*60))%60+"秒";

        if(status == 0){//通话未成功
            if(mode==0 || mode==2){//拨出未成功
                duration = "未接通";
            }
            else{//接听未成功
                duration = "响铃"+duration;
            }
        }
        //对电话号码加密
//        String phoneNumbers= AesUtil.encrypt(key,phoneNumber);

        //查看联系人数据库中是否有该号码
        List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
        Log.i(TAG, "查看联系人数据库中是否有该号码list"+list.size());
//        Log.i(TAG, "查看联系人数据库中是否有该号码sim_name"+sim_name.length());
//        Log.i(TAG, "查看联系人数据库中是否有该号码sim_photo"+sim_photo);
        //如果联系人中没有该号码
        if(list.size() == 0){
            Records records = new Records();
            records.setName("");
            records.setPhoneNumber(phoneNumber);
            records.setTimeStart(timeStart);
            records.setDuration(duration);//持续时间
            records.setMode(mode);
            records.setStatus(status);
            records.setPhoto(2);//2为未知头像
            records.save();
            L.i("MyRecordsHelper","产生通话记录成功，该电话号码为陌生号码。");
            return records.getId();
        }else{//如果联系人中有该号码
            Person contacts = list.get(0);//用该号码的第一个联系人信息
            Records records = new Records();
            records.setName(contacts.getName());
            records.setPhoneNumber(phoneNumber);
            records.setTimeStart(timeStart);
            records.setDuration(duration);//持续时间
            records.setMode(mode);
            records.setStatus(status);
            records.setPhoto(contacts.getPhoto());//联系人头像
            records.save();
            Log.i(TAG, "产生通话记录成功，该电话号码为已有联系人");
            return records.getId();
        }
    }



    /**
     * 复制通话记录方法，由导入数据库时调用，不作为接口使用
     * @param phoneNumber
     * @param timeStart
     * @param duration
     * @param mode
     * @param status
     */
    public void copyRecords(String phoneNumber,String timeStart,String duration,int mode,int status){
        //查看联系人数据库中是否有该号码
        List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);

        //如果联系人中没有该号码
        if(list.size() == 0){
            Records records = new Records();
            records.setName("");
            records.setPhoneNumber(phoneNumber);
            records.setTimeStart(timeStart);
            records.setDuration(duration);//持续时间
            records.setMode(mode);
            records.setStatus(status);
            records.setPhoto(2);//2为未知头像
            records.save();
            return;
        }

        //如果联系人中有该号码
        Person contacts = list.get(0);//用该号码的第一个联系人信息
        Records records = new Records();
        records.setName(contacts.getName());
        records.setPhoneNumber(phoneNumber);
        records.setTimeStart(timeStart);
        records.setDuration(duration);//持续时间
        records.setMode(mode);
        records.setStatus(status);
        records.setPhoto(contacts.getPhoto());//联系人头像
        records.save();
    }

    /**
     * 删除通话记录接口
     * @param id 需要删除的数据id
     * @return 是否删除成功 true-删除成功 false-删除失败
     */
    public boolean deleteRecords(int id){
        //判断数据库中是否有输入的id
        List<Records> list = DataSupport.where("id = ?",""+id).find(Records.class);
        if(list.size() == 0){
            L.i("MyRecordsHelper","删除通话记录失败，id = "+id+"不在数据库中。");
            return false;//若数据库中没有输入的id，返回false，表示删除失败
        }

        DataSupport.delete(Records.class,id);//否则，删除id为输入值的那条数据
        L.i("MyRecordsHelper","删除id = "+id+"通话记录成功。");
        return true;//返回true，表示删除成功
    }

    /**
     * 修改通话记录接口
     * @param id 需要修改的通话记录id
     * @param timeStart 拨打时间 暂定格式：yyyy-MM-dd HH:mm:ss
     * @param timeEnd 挂断时间 暂定格式：yyyy-MM-dd HH:mm:ss
     * @param status 通话是否成功 0-未接通 1-接通
     * @return true-修改成功 false-修改失败
     */
    public boolean editRecords(int id,String timeStart,String timeEnd,int status){
        //约束status
        if(status!=FAILED && status!=SUCCEEDED){
            L.i("MyRecordsHelper","修改通话记录失败。参数status = "+status+"不合法！");
            return false;
        }

        Records records = DataSupport.find(Records.class,id);
        if(records == null){
            L.i("MyRecordsHelper","修改通话记录失败，id = "+id+"不在数据库中。");
            return false;//若没有该条通话记录，修改失败，返回false
        }

        //计算时间差
        long day = -1;
        long hour = -1;
        long minute = -1;
        long second = -1;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        try{
            Date dateStart = simpleDateFormat.parse(timeStart);
            Date dateEnd = simpleDateFormat.parse(timeEnd);
            long time = dateEnd.getTime()-dateStart.getTime();//时间差

            //若拨打时间后于挂断时间，打印日志，退出添加通话记录
            if(time < 0){
                L.i("MyRecordsHelper","修改通话记录失败。传入时间错误,拨打时间或接通时间后于挂断时间。");
                L.i("MyRecordsHelper","timeStart："+timeStart);
                L.i("MyRecordsHelper","timeEnd："+timeEnd);
                time = 1;

            }
            second = time/1000;//秒
            minute = second/60;//分
            hour = minute/60;//时
            day = hour/24;//天
        }
        catch (ParseException e){
            e.printStackTrace();
            L.i("MyRecordsHelper","修改通话记录失败。解析时间异常，请检查传入时间字符串是否符合格式。");
            return false;
        }

        //转换持续时间的格式
        String duration;
        if(second == 0) duration = "1秒";//未满1秒算1秒
        else if(minute == 0) duration = second+"秒";
        else if(hour == 0) duration = minute+"分"+second%60+"秒";
        else if(day == 0) duration = hour+"小时"+minute%60+"分"+(second%(60*60))%60+"秒";
        else duration = day+"天"+hour%24+"小时"+(minute%(60*24))%60+"分"+((second%(60*60*24))%(60*60))%60+"秒";

        if(status == 0){//通话未成功
            if(records.getMode()==0 || records.getMode()==2){//拨出未成功
                duration = "未接通";
            }
            else{//接听未成功
                duration = "响铃"+duration;
            }
        }

        //修改通话记录
        records.setTimeStart(timeStart);
        records.setDuration(duration);//持续时间
        records.setStatus(status);
        records.save();
        L.i("MyRecordsHelper","修改id = "+id+"通话记录成功。");
        return true;
    }

    /**
     * 加载通话记录接口
     * @return 返回List,其中含有所有通话记录,且时间靠后的通话记录在前
     */
    public Map<String,Object> loadRecords(){

        boolean is_flag=true;
        HashMap<String,Object> map=new HashMap();
        //获取一个当前时间作为标准，勿写在循环内获取多个时间影响效率
        Date dateNow = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat simpleDateFormatNew = new SimpleDateFormat("yyyy/M/d");
        simpleDateFormatNew.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        /*
        为了避免设备未连网时，获取错误且跨度很大的当前时间，以2020-01-01 00:00:00为参考时间
        若获取了参考时间之前的时间作为当前时间，则认为获取当前时间错误，返回绝对时间的拨打时间或者接通时间
        若获取了参考时间之后的时间作为当前时间，则认为获取当前时间正确，返回相对时间的拨打时间或者接通时间
         */
        long timeTarget = 0;
        try{
            timeTarget = simpleDateFormat.parse("2020-01-01 00:00:00").getTime();//参考时间戳
        }catch (ParseException e){
            L.i("MyRecordsHelper","加载通话记录时，解析参考时间失败，请检查传入参考时间字符串是否符合格式。");
            e.printStackTrace();
        }

        //方式1：电话拨打时间或者接通时间是绝对时间
        if((dateNow.getTime()-timeTarget) < 0){
            map.put("is_flag",is_flag);
            L.i("MyRecordsHelper","获取当前时间失败，timeStart返回绝对时间");
            List<Records> lists= DataSupport.order("id desc").find(Records.class);//按id降序排序，因为时间后的id大
            map.put("lists",lists);
            return map;
        }


        //方式2：电话拨打时间或者接通时间是相对时间
        List<Records> list = DataSupport.order("id desc").find(Records.class);//按id降序排序，因为时间后的id大
        is_flag=false;
        for(Records records:list){

            //对电话号码进行解密
//            records.setPhoneNumber(AesUtil.decrypt(key,records.getPhoneNumber()));

            try{
                Date date = simpleDateFormat.parse(records.getTimeStart());
                Long time = dateNow.getTime()-date.getTime();//时间差
                if(time < 0){
                    L.i("MyRecordsHelper","加载联系人失败。拨打时间或者接通时间有误，超过了当前时间");
                    L.i("MyRecordsHelper","timeStart："+records.getTimeStart());
                    L.i("MyRecordsHelper","当前时间："+simpleDateFormat.format(dateNow));
                    continue;
                }
                Long minute = time/(60*1000);//分
                Long  hour = minute/60;//时
                Long day = hour/24;//天

                //转换相对开始时间格式
                if(minute == 0) records.setTimeStart("刚刚");
                else if(hour == 0) records.setTimeStart(minute+"分钟前");
                else if(day == 0) records.setTimeStart(hour+"小时前");
                else if(day == 1) records.setTimeStart("昨天");
                else {
                    String dateNowStr = simpleDateFormatNew.format(dateNow);
                    String dateStr = simpleDateFormatNew.format(date);

                    //年份相同
                    if(dateNowStr.substring(0,4).equals(dateStr.substring(0,4))){
                        records.setTimeStart(dateStr.substring(5,dateStr.length()));//只要日期，例如9/4
                    }
                    else{//年份不同
                        records.setTimeStart(dateStr);//带年份，例如2020/9/4
                    }
                }
            }
            catch (ParseException e){
                e.printStackTrace();
            }
        }
        map.put("is_flag",is_flag);
        map.put("lists",list);
        L.i("MyRecordsHelper","获取当前时间成功，timeStart返回相对时间");
        return map;
    }

    /**
     * 判断该电话号码是否已存为联系人的接口
     * @param phoneNumber 输入的电话号码
     * @return true-已存为联系人 false-未存为联系人
     */
    public boolean hasContacts(String phoneNumber){

        //电话号码加密，加密之后查找数据库
//        String phoneNumbers = AesUtil.encrypt(key,phoneNumber);

        List<Person> list = DataSupport.where("phonenumber = ?",phoneNumber).find(Person.class);
        if(list.size() == 0) return false;
        else return true;
    }

    /**
     * 通话电话号码获取联系人信息的接口（含该联系人的id）
     * @param phoneNumber 输入的电话号码
     * @return 返回联系人对象，若该电话没存未联系人返回NULL
     */
    public Person loadContactsByPhoneNumber(String phoneNumber){

        //电话号码加密，加密之后查找数据库
//        String phoneNumbers = AesUtil.encrypt(key,phoneNumber);

        List<Person> list = DataSupport.where("phonenumber = ?",phoneNumber).find(Person.class);
        if(list.size() == 0) return null;
        else return list.get(0);
    }

}


