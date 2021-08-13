package com.example.tclphone.db;

//import android.util.Log;

//import com.tcl.tv5g.phonebook.util.AesUtil;

import com.example.tclphone.litepal.crud.DataSupport;
import com.example.tclphone.utils.L;

import net.sourceforge.pinyin4j.PinyinHelper;




import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import static android.content.ContentValues.TAG;

public class MyContactsHelper {

    //定义常量：

    //存储方式storage
    public static final int LOCALITY = 0;
    public static final int SIM = 1;
    public static final int LOCALITY_AND_SIM = 2;

    //照片photo
    public static final int MALE = 0;
    public static final int FEMALE = 1;
    public static final int STRANGER = 2;

    //返回状态
    public static final int NOT_FOUND = -1;
    public static final int OK = 0;
    public static final int INVALID_PHONE_NUMBER = 1;
    public static final int INVALID_STORAGE = 2;
    public static final int INVALID_PHOTO = 3;


//    String key = "tcl.tv5g";//加密唯一标识
    /**
     * 私有方法，用于判断字符串内是否含有中文
     * @param str 输入的字符串
     * @return true-含有中文 false-不含中文
     */
    private boolean isContainChinese(String str){
        Pattern pattern = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher matcher = pattern.matcher(str);
        if(matcher.find()) return true;
        return false;
    }

    /**
     * 私有方法，用于判断电话号码是否合法
     * @param str
     * @return
     */
    private boolean isPhoneNumber(String str){
        //含有中文就false
        if(isContainChinese(str)){
            L.i("MyContactsHelper","电话号码含有中文！");
            return false;
        }

        //含有英文就false
        if(str.matches(".*[a-zA-z].*")){
            L.i("MyContactsHelper","电话号码含有英文！");
            return false;
        }

        return true;
    }

    /**
     * 私有方法，用于更新通话记录表。该方法会将电话号码为phoneNumber的通话记录的信息去参照序号为id的这个联系人的信息做更新操作
     * @param phoneNumbers 所需修改记录的电话号码(加密后的电话号码！)
     * @param id 联系人id，所需修改的记录参照该联系人id。当id在联系人表找不到时，代表该联系人已被删除，请将他的相关通话记录设为陌生
     */
    private void updateRecords(String phoneNumbers,int id){
        Person referContacts = DataSupport.find(Person.class,id);
        Records recordsToUpdate = new Records();

        //如果该联系人已被删除
        if(referContacts == null){
            recordsToUpdate.setName("");
            recordsToUpdate.setPhoto(STRANGER);
        }
        else{//如果该联系人未被删除
            recordsToUpdate.setName(referContacts.getName());
            if(referContacts.getPhoto() == 0){//在使用updateAll（）方法更新数据时，恢复零值不可用set
                recordsToUpdate.setToDefault("photo");
            }
            else{
                recordsToUpdate.setPhoto(referContacts.getPhoto());
            }
        }

        recordsToUpdate.updateAll("phoneNumber = ?",phoneNumbers);
        L.i("MyContactsHelper","id = "+id+"联系人信息改变，通话记录已更新。");
    }



    /**
     *添加联系人数据接口
     * @param name 名字
     * @param phoneNumber 电话号码
     * @param storage 存储类型
     * @param photo 头像类型
     * @return int 0-添加成功 1-电话号码不合法 2-存储类型不合法 3-头像类型不合法
     */
    public int addContacts(String name,String phoneNumber,int storage,int photo){
        //做一些判断
        //约束phoneNumber
        if(!isPhoneNumber(phoneNumber)){
            L.i("MyContactsHelper","添加联系人失败，参数phoneNumber = "+phoneNumber+"不合法。");
            return INVALID_PHONE_NUMBER;
        }
        //约束storage
        if(storage!=LOCALITY && storage!=SIM && storage!=LOCALITY_AND_SIM){
            L.i("MyContactsHelper","添加联系人失败，参数storage = "+storage+"不合法。");
            return INVALID_STORAGE;
        }
        //约束photo
        if(photo!=MALE && photo!=FEMALE && photo!=STRANGER){
            L.i("MyContactsHelper","添加联系人失败，参数photo = "+photo+"不合法。");
            return INVALID_PHOTO;
        }

        //对电话号码加密（数据加密再存储，已弃用）
//        String phoneNumbers= AesUtil.encrypt(key,phoneNumber);
        //添加数据
        L.i("MyContactsHelper","storage----------联系人是否成功。"+storage);
        Person contacts = new Person();

        contacts.setName(name);
        contacts.setPhoneNumber(phoneNumber);
        contacts.setStorage(storage);
        contacts.setPhoto(photo);
        boolean isSave=contacts.save();
        L.i("MyContactsHelper","联系人是否成功。"+isSave);
        L.i("MyContactsHelper","联系人添加成功。");

        //更新该联系人的通话记录表
        List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
        if(contacts.getId() == list.get(0).getId()){//添加的是该号码第一个联系人
            updateRecords(phoneNumber,contacts.getId());
        }

        return OK;
    }
    /**
     * 加载联系人的接口
     * @return 返回List,其中含有所有联系人信息
     */
    public List<Person> loadContact(){
        return DataSupport.findAll(Person.class);
    }


    /**
     * 根据名称查询联系人的电话号码
     * @param name
     * @return
     */
    public List<Person> phoneByName(String name){
        List<Person> list = DataSupport.where("name = ?",name).find(Person.class);
        return list;

    }
    /**
     * 根据用户id查询当前用户信息接口
     * @param id
     * @return
     */
    public Person loadContactByid(int id){

        Person contacts = DataSupport.find(Person.class,id);
        return contacts;

    }
    /**
     * 根据用户phonenumber和storage查询当前用户信息id接口
     * @param
     * @return
     */
    public int loadContactByphone(String phoneNumber,int storage){
        List<Person> list2 = DataSupport.where("storage= "+storage+ ";"+"phoneNumber = "+phoneNumber).find(Person.class);
        if(list2.size()==0){
            return 0;
        }
        return list2.get(0).getId();
    }
    public List<Person> loadContactByphone(int  id){
        List<Person> list2 = DataSupport.where("id = ?",""+id).find(Person.class);
        return list2;
    }
    public List<Person> loadContactByphone(String  phoneNumber){
        List<Person> list2 = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
        return list2;
    }
    /**
     * 联系人删除接口
     * @param id 需要删除的数据id
     * @return 是否删除成功 true-删除成功 false-删除失败
     *
     * Sim卡上和本地的联系人数据都将被删除
     */
    public boolean deleteContacts(int id){
        //判断数据库中是否有输入的id
        List<Person> list = DataSupport.where("id = ?",""+id).find(Person.class);
        if(list.size() == 0){
            L.i("MyContactsHelper","删除联系人失败，id = "+id+"不在数据库中。");
            return false;//若数据库中没有输入的id，返回false，表示删除失败
        }

        //获取该联系人的电话号码
        String phoneNumbers = DataSupport.find(Person.class,id).getPhoneNumber();

        //删除该联系人
        DataSupport.delete(Person.class,id);//否则，删除id为输入值的那条数据
        L.i("MyContactsHelper","id = "+id+"联系人删除成功。");

        //更新该联系人的通话记录表
        List<Person> list2 = DataSupport.where("phoneNumber = ?",phoneNumbers).find(Person.class);
        if(list2.size() == 0){//删除了该号码唯一一个联系人
            updateRecords(phoneNumbers,id);
        }
        else if(id < list2.get(0).getId()){
            updateRecords(phoneNumbers,list2.get(0).getId());
        }

        return true;//返回true，表示删除成功
    }
    /**
     * 判断该电话号码是否已存为联系人的接口
     * @param phoneNumber 输入的电话号码
     * @return true-已存为联系人 false-未存为联系人
     */
    public boolean hasContacts(String phoneNumber){

        //电话号码加密，加密之后查找数据库
//        String phoneNumbers = AesUtil.encrypt(key,phoneNumber);
        List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
        if(list.size() == 0) return false;
        else return true;
    }
    /**
     * 判断该电话号码是否已存为联系人的SIM接口
     * @param phoneNumber 输入的电话号码
     * @return true-已存为联系人 false-未存为联系人
     */
    public boolean hasContacts_Sim(String phoneNumber){

        //电话号码加密，加密之后查找数据库
//        String phoneNumbers = AesUtil.encrypt(key,phoneNumber);
        List<Person> list = DataSupport.where("storage= "+1+ ";"+"phoneNumber = "+phoneNumber).find(Person.class);
        if(list.size() == 0) return false;

        else return true;
    }

    /**
     * 判断该电话号码是否已存为联系人的S口
     * @param phoneNumber 输入的电话号码
     * @return true-已存为联系人 false-未存为联系人
     */
    public boolean hasContacts_local(String phoneNumber){

        //电话号码加密，加密之后查找数据库
//        String phoneNumbers = AesUtil.encrypt(key,phoneNumber);
        List<Person> list = DataSupport.where("storage= "+0+ ";"+"phoneNumber = "+phoneNumber).find(Person.class);

        if(list.size() == 0) return false;
        else return true;
    }


    /**
     *编辑联系人数据接口
     * @param  id 这条联系人数据的id
     * @param name 名字
     * @param phoneNumber 电话号码
     * @param storage 存储类型
     * @param photo 头像类型
     * @return int -1-id不在数据库中 0-修改成功 1-电话号码不合法 2-存储类型不合法 3-头像类型不合法
     */
    public int editContacts(int id,String name,String phoneNumber,int storage,int photo){
        //做一些判断
        //约束phoneNumber
        if(!isPhoneNumber(phoneNumber)){
            L.i("MyContactsHelper","编辑联系人失败，参数phoneNumber = "+phoneNumber+"不合法。");
            return INVALID_PHONE_NUMBER;
        }
        //约束storage
        if(storage!=LOCALITY && storage!=SIM && storage!=LOCALITY_AND_SIM){
            L.i("MyContactsHelper","编辑联系人失败，参数storage = "+storage+"不合法。");
            return INVALID_STORAGE;
        }
        //约束photo
        if(photo!=MALE && photo!=FEMALE && photo!=STRANGER){
            L.i("MyContactsHelper","编辑联系人失败，参数photo = "+photo+"不合法。");
            return INVALID_PHOTO;
        }

//        String phoneNumbers= AesUtil.encrypt(key,phoneNumber);

        //修改数据
        Person contacts = DataSupport.find(Person.class,id);
        if(contacts == null){
            L.i("MyContactsHelper","编辑联系人失败，id = "+id+"不在数据库中。");
            return NOT_FOUND;
        }

        String oldPhoneNumbers = contacts.getPhoneNumber();//获取旧电话号码
        contacts.setName(name);
        contacts.setPhoneNumber(phoneNumber);
        contacts.setStorage(storage);
        contacts.setPhoto(photo);
        contacts.save();
        L.i("MyContactsHelper","id = "+id+"联系人编辑成功。");

        //更新该联系人的通话记录表
        if(oldPhoneNumbers == phoneNumber){//电话号码没变
            List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
            if(id == list.get(0).getId()){//修改的是该号码第一个联系人
                updateRecords(phoneNumber,id);
            }
        }
        else{//电话号码变了

            //把这种更改电话号码的操作分解为删除原号码、添加新号码两种操作来处理

            //删除了原号码
            List<Person> list2 = DataSupport.where("phoneNumber = ?",oldPhoneNumbers).find(Person.class);
            if(list2.size() == 0){//删除了原号码唯一一个联系人
                updateRecords(oldPhoneNumbers,-1);//这里为了传入空联系人的id传入了-1
            }
            else if(id < list2.get(0).getId()){
                updateRecords(oldPhoneNumbers,list2.get(0).getId());
            }

            //添加了新号码
            List<Person> list = DataSupport.where("phoneNumber = ?",phoneNumber).find(Person.class);
            if(id == list.get(0).getId()){//添加的是该号码第一个联系人
                updateRecords(phoneNumber,contacts.getId());
            }
        }

        return OK;
    }

    /**
     * 复制联系人到本地的接口
     * @param list 输入List，包含需要复制的联系人的id
     * @return 返回是否复制成功 true-复制成功 false-复制失败
     */
    public boolean copyToLocality(List<Integer> list){
        Person contacts = new Person();
        contacts.setStorage(LOCALITY_AND_SIM);
        for(int id:list){
            contacts.update(id);
        }
        L.i("MyContactsHelper","复制成功。");
        return true;
    }

    /**
     * 复制联系人到Sim卡的接口
     * @param list 输入List，包含需要复制的联系人的id
     * @return 返回是否复制成功 true-复制成功 false-复制失败
     */
    public boolean copyToSim(List<Integer> list){
        return copyToLocality(list);
    }

    /**
     * 加载联系人的接口
     * @return 返回List,其中含有所有联系人信息 排序规则：按联系人姓名排列，中文根据拼音排列，支持中英混排
     */
    public List<Person> loadContacts(){

        try {
            List<Person> list = DataSupport.findAll(Person.class);
            L.i("MyContactsHelper", "加载指定联系人+++++++++++" + list.size());
            Collections.sort(list, new ChineseCharComp());
            return list;
        }catch (Exception ex){
            L.i("MyContactsHelper", "加载指定联系人+++++++++++失败");
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 根据联系人id查询联系人信息接口
     * @param id 联系人id
     * @return 序号为id的联系人对象，若id不在数据库中返回null
     */
    public Person loadContactsById(int id){
        Person contacts = DataSupport.find(Person.class,id);
        if(contacts == null){
            L.i("MyContactsHelper","加载指定联系人失败，id = "+id+"不在数据库中。");
            return null;
        }
        L.i("MyContactsHelper", "加载指定联系人"+contacts.getPhoneNumber());
        return contacts;
    }

    /**
     * 加载（仅存）本地联系人的接口
     * @return 返回List,其中含有本地联系人信息
     */
    public List<Person> loadLocalityContacts(){
        List<Person> list = DataSupport.where("storage = ?","0").find(Person.class);
        Collections.sort(list,new ChineseCharComp());
        return list;
    }

    /**
     * 加载（仅存）Sim卡联系人的接口
     * @return 返回List,其中含有Sim卡联系人信息
     */
    public List<Person> loadSimContacts(){
        List<Person> list = DataSupport.where("storage = ?","1").find(Person.class);
        Collections.sort(list,new ChineseCharComp());
        return list;
    }

    /**
     * 加载本地所有联系人的接口
     * @return 返回List,其中含有本地所有联系人信息
     */
    public List<Person> loadAllLocalityContacts(){
        try {
            List<Person> list = DataSupport.where("storage = ?", "0").find(Person.class);
            Collections.sort(list, new ChineseCharComp());
            L.i("MyContactsHelper", "loadAllLocalityContacts:list.size----:" + list.size());
            return list;
        }catch (Exception e){
            e.printStackTrace();
            List<Person> list=new ArrayList<>();
            return list;
        }
    }

    /**
     * 加载Sim卡所有联系人的接口
     * @return 返回List,其中含有Sim卡所有联系人信息
     */
    public List<Person> loadAllSimContacts(){
        try {
            List<Person> list = DataSupport.where("storage = ?", "1").find(Person.class);
            Collections.sort(list, new ChineseCharComp());
            L.i("MyContactsHelper", "loadAllSimContacts:list.size----:" + list.size());
            return list;
        }catch (Exception e){
            e.printStackTrace();
            List<Person> list=new ArrayList<>();
            return list;
        }
    }

    /**
     * 搜索联系人接口，通过输入的字符串匹配联系人
     * @param str 输入的字符串
     * @return 与字符串匹配的联系人List
     */
    public List<Person> searchContacts(String str){
        List<Person> list = DataSupport.where("name like ? or phonenumber like ?","%"+str+"%","%"+str+"%").find(Person.class);
      //  Collections.sort(list,new ChineseCharComp());
        return list;
    }


    //--------以下是对本机号码处理的接口--------

    /**
     * 插入本机号码接口
     * @param phoneNumber 要插入的本机号码
     * @return true-插入成功 false-插入失败
     */
    public boolean addLocalNumber(String phoneNumber){
        //入参检查
        if(!isPhoneNumber(phoneNumber)){
            L.i("MyContactsHelper","本机号码插入失败，参数phoneNumber = "+phoneNumber+"不合法。");
            return false;
        }

        LocalNumber localNumber = new LocalNumber();
        localNumber.setPhoneNumber(phoneNumber);
        localNumber.save();//添加本机号码不影响通话记录。
        L.i("MyContactsHelper","本机号码插入成功。");
        return true;
    }

    /**
     * 删除所有本机号码接口，模拟SIM卡拔出操作。
     */
    public void deleteAllLocalNumber(){
        DataSupport.deleteAll(LocalNumber.class);
        L.i("MyContactsHelper","本机号码已清空。");
    }

    /**
     * 加载本机号码接口
     * @return 返回本地号码，若数据库中没有本地号码则返回空字符串 ""
     */
    public String loadLocalNumber(){
        List<LocalNumber> list = DataSupport.findAll(LocalNumber.class);
        if(list.size() == 0){
            L.i("MyContactsHelper","加载本机号码失败，数据库中未插入本机号码。");
            return "";
        }
        LocalNumber localNumber = list.get(list.size()-1);//取最新的本机号码。防止拔掉SIM卡未删除本机号码时，无法获取新插入的本机号码。
        return localNumber.getPhoneNumber();
    }


}


/**
 * 比较类，实现了Comparator接口
 * 根据联系人姓名实现中英混排
 */
class ChineseCharComp implements Comparator{
    @Override
    public int compare(Object o1, Object o2) {//重写比较方法
        Person c1 = (Person) o1;//向下转型
        Person c2 = (Person) o2;//向下转型
//        Collator collator = Collator.getInstance(Locale.CHINESE);//按中文排序，但不能中英混排
//        return collator.compare(c1.getName(),c2.getName());
        String str1 = convertToPingYin(c1.getName());//含中文字符串转换为含拼音字符串
        String str2 = convertToPingYin(c2.getName());//含中文字符串转换为含拼音字符串
        return str1.compareTo(str2);//含拼音字符串（不含中文）比较
    }

    /**
     * 类内私有方法，将含中文字符串转换为含拼音字符串
     * @param str 传入的字符串 可含中文，也可不含中文
     * @return 返回字符串 中文转换为拼音，其他字符不转换，例如： cheng明 -> chengming
     */
    private String convertToPingYin(String str){
        StringBuilder sb = new StringBuilder();//StringBuilder衔接字符串效率高
        String[] strArrays;//声明字符串数组

        for(int i=0;i<str.length();i++){
            strArrays = PinyinHelper.toHanyuPinyinStringArray(str.charAt(i));//调用第三方包内PinyinHelper的toHanyuPinyinStringArray方法
            if(strArrays!=null && strArrays.length>0){//该字符是中文，且转换成功，append该字符的拼音
                for(String s:strArrays){
                    sb.append(s);
                }
            }else{//该字符不是中文，直接append该字符
                sb.append(str.charAt(i));
            }
        }

        return sb.toString();
    }
}

