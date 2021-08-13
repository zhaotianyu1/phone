package com.example.tclphone.db;

import android.util.Log;


import com.example.tclphone.litepal.crud.DataSupport;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public class MessageHelper {
    private static final String TAG = MessageHelper.class.getSimpleName();
    //联系人的数据库操作类
    MyContactsHelper contactsHelper=new MyContactsHelper();


    /**
     *添加联系人数据接口
     * @param name 名字
     * @param phoneNumber 电话号码
     * @param type 发送/接收类型
     * @param time_operation 发送/接收时间
     * @return int 0-添加成功 1-电话号码不合法 2-存储类型不合法 3-头像类型不合法
     */
    public boolean addMessage(String phoneNumber,String name,int photo,int type,String content,String time_operation,int read){
        boolean flag = false;
        List<Person> contactsList=contactsHelper.loadContactByphone(phoneNumber);
        if (contactsList.isEmpty()) {
            name = phoneNumber;
            //添加数据
            Message message = new Message();
            message.setContent(content);
            message.setPhonenumber(phoneNumber);
            message.setPhoto(0);
            message.setName(name);
            message.setType(type);
            message.setTime_operation(time_operation);
            message.setRead(read);
            message.save();
            flag=true;
        } else {
            //添加数据
            name = contactsList.get(0).getName();
            photo = contactsList.get(0).getPhoto();
            Message message = new Message();
            message.setContent(content);
            message.setPhonenumber(phoneNumber);
            message.setName(name);
            message.setPhoto(photo);
            message.setType(type);
            message.setRead(read);
            message.setTime_operation(time_operation);
            message.save();
            flag=true;
        }
        Log.i(TAG, "insert Msg :" + flag + "-->");
        return flag;

    }

    /**
     *编辑短信数据接口
     * @retur
     */
    public int editMessage(int id,int read){

        //修改数据
        Message message = DataSupport.find(Message.class,id);
        message.setRead(read);
        message.save();
        return 0;
    }


    /**
     * 加载通话记录接口
     * @return 返回List,其中含有所有通话记录,且时间靠后的通话记录在前
     */
    public List<Message> loadMessage(){

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

            e.printStackTrace();
        }

        //方式1：电话拨打时间或者接通时间是绝对时间
        if((dateNow.getTime()-timeTarget) < 0){
            return DataSupport.order("id desc").find(Message.class);//按id降序排序，因为时间后的id大
        }


        //方式2：电话拨打时间或者接通时间是相对时间
        List<Message> list = DataSupport.order("id desc").find(Message.class);//按id降序排序，因为时间后的id大

        for(Message records:list){

            //对电话号码进行解密
//            records.setPhoneNumber(AesUtil.decrypt(key,records.getPhoneNumber()));

            try{
                Date date = simpleDateFormat.parse(records.getTime_operation());
                Long time = dateNow.getTime()-date.getTime();//时间差
                if(time < 0){
                    return null;
                }
                Long minute = time/(60*1000);//分
                Long  hour = minute/60;//时
                Long day = hour/24;//天

                //转换相对开始时间格式
                if(minute == 0) records.setTime_operation("刚刚");
                else if(hour == 0) records.setTime_operation(minute+"分钟前");
                else if(day == 0) records.setTime_operation(hour+"小时前");
                else if(day == 1) records.setTime_operation("昨天");
                else {
                    String dateNowStr = simpleDateFormatNew.format(dateNow);
                    String dateStr = simpleDateFormatNew.format(date);

                    //年份相同
                    if(dateNowStr.substring(0,4).equals(dateStr.substring(0,4))){
                        records.setTime_operation(dateStr.substring(5,dateStr.length()));//只要日期，例如9/4
                    }
                    else{//年份不同
                        records.setTime_operation(dateStr);//带年份，例如2020/9/4
                    }
                }
            }
            catch (ParseException e){
                e.printStackTrace();
            }
        }
        List<Message> newList = new ArrayList<>();
        list.stream().filter(distinctByKey(p -> p.getPhoneNumber())).forEach(newList::add);


        return newList;
    }
    static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {

        Map<Object,Boolean> seen = new ConcurrentHashMap<>();

        //putIfAbsent方法添加键值对，如果map集合中没有该key对应的值，则直接添加，并返回null，如果已经存在对应的值，则依旧为原来的值。

        //如果返回null表示添加数据成功(不重复)，不重复(null==null :TRUE)

        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;

    }



    /**
     * 根据id查询电话号码并删除
     * @param id
     * @return
     */
    public boolean findPhoneByid(int id){
        Message message = DataSupport.find(Message.class,id);
        Log.i(TAG, "iphonenumber : " + message.getPhoneNumber());
        DataSupport.deleteAll(Message.class,"phonenumber = ?",message.getPhoneNumber());
        return true;
    }

    /**
     * 根据用户phonenumber查询当前短信信息接口
     * @param
     * @return
     */
    public List<Message> loadMessageByphone(String  phoneNumber){
        List<Message> messages = DataSupport.where("phonenumber = ?",phoneNumber).find(Message.class);

        Date dateNow = new Date();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        SimpleDateFormat simpleDateFormatNew = new SimpleDateFormat("yyyy/M/d");
        simpleDateFormatNew.setTimeZone(TimeZone.getTimeZone("GMT+8"));

        for(Message records:messages){

            //对电话号码进行解密
//            records.setPhoneNumber(AesUtil.decrypt(key,records.getPhoneNumber()));

            try{
                Date date = simpleDateFormat.parse(records.getTime_operation());
                Long time = dateNow.getTime()-date.getTime();//时间差
                if(time < 0){
                    return null;
                }
                Long minute = time/(60*1000);//分
                Long  hour = minute/60;//时
                Long day = hour/24;//天

                //转换相对开始时间格式
                if(minute == 0) records.setTime_operation("刚刚");
                else if(hour == 0) records.setTime_operation(minute+"分钟前");
                else if(day == 0) records.setTime_operation(hour+"小时前");
                else if(day == 1) records.setTime_operation("昨天");
                else {
                    String dateNowStr = simpleDateFormatNew.format(dateNow);
                    String dateStr = simpleDateFormatNew.format(date);

                    //年份相同
                    if(dateNowStr.substring(0,4).equals(dateStr.substring(0,4))){
                        records.setTime_operation(dateStr.substring(5,dateStr.length()));//只要日期，例如9/4
                    }
                    else{//年份不同
                        records.setTime_operation(dateStr);//带年份，例如2020/9/4
                    }
                }
            }
            catch (ParseException e){
                e.printStackTrace();
            }
        }
        return messages;
    }


    /**
     * 加载所有信息的接口
     * @return
     */
    public List<Message> loadMessages(){
        return DataSupport.findAll(Message.class);
    }

}
