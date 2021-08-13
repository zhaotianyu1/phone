package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;
import com.example.tclphone.utils.L;


import java.util.ArrayList;
import java.util.List;

/**
 * 本机号码数据库
 */
public class PhoneHelper {





    /**
     * 将手机号添加到数据库中
     * @param phone
     */
    public void addPhones(String phone){

        List<Phone> list2 = DataSupport.findAll(Phone.class);
        if(!list2.isEmpty()){
            for(int i=0;i<list2.size();i++) {
                DataSupport.delete(Phone.class, list2.get(i).getId());
            }
            L.i("PhoneHelper","删除成功");
        }
        L.i("PhoneHelper","phone"+phone);
        L.i("PhoneHelper","phoneload"+list2.size());
        Phone phones=new Phone();
        phones.setPhone(phone);
        phones.save();

    }

    /**
     * 加载手机号码
     * @return
     */
    public List<Phone> loadPhones(){

        List<Phone> phoneList=new ArrayList<>();

        try{
            return DataSupport.findAll(Phone.class);
        }catch (Exception e){
            e.printStackTrace();
            return phoneList;
        }
    }

    /**
     * 删除本机号码
     * @param id
     * @return
     */
    public boolean delete(int id){
        //判断数据库中是否有输入的id
        List<Phone> list = DataSupport.where("id = ?",""+id).find(Phone.class);
        if(list.size() == 0){
            L.i("PhoneHelper","删除联系人失败，id = "+id+"不在数据库中。");
            return false;
        }else{
            DataSupport.delete(Person.class,id);//否则，删除id为输入值的那条数据
            L.i("PhoneHelper","phone_id: "+id);

        }
        return true;

    }
}
