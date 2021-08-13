package com.example.tclphone.db;





import com.example.tclphone.litepal.crud.DataSupport;
import com.example.tclphone.utils.L;

import java.util.List;

public class SimHelper {


    /**
     * 将iccid添加到数据库中
     * @param iccid
     */
    public void addSim(String iccid){

        List<SIM> list2 = DataSupport.findAll(SIM.class);

        if(!list2.isEmpty()){
            for(int i=0;i<list2.size();i++) {
                DataSupport.delete(SIM.class, list2.get(i).getId());
            }
        }
        SIM sim=new SIM();
        sim.setIccid(iccid);
        sim.save();
    }

    /**
     * 加载iccid
     * @return
     */
    public List<SIM> loadSim(){

        List<SIM> lists=DataSupport.findAll(SIM.class);
        return lists;
    }

    /**
     * 删除本机号码
     * @param id
     * @return
     */
    public boolean delete(int id){
        //判断数据库中是否有输入的id
        List<SIM> list = DataSupport.where("id = ?",""+id).find(SIM.class);
        if(list.size() == 0){
            L.i("MyContactsHelper","删除iccid失败，id = "+id+"不在数据库中。");
            return false;
        }else{
            DataSupport.delete(Person.class,id);//否则，删除id为输入值的那条数据
        }
        return true;

    }
}
