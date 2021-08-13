package com.example.tclphone.phonebook.contactadapter;

import java.io.Serializable;

/**
 * 联系人前端实体类
 */
public class Contact implements Serializable {

    private int id;
    private int storage;
    private String name;
    private  int photo;
    private String phonenumber;
    private int sign;   //标识（1代表有背景颜色，0代表无背景颜色）
    private String ids;//列表的序号

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public int getSign() {
        return sign;
    }

    public void setSign(int sign) {
        this.sign = sign;
    }

    public Contact(){

    }

    public Contact(int id, String name, String phonenumber , int storage, int photo ){

        this.name = name;
        this.photo = photo;
        this.phonenumber = phonenumber;
        this.id=id;
        this.storage=storage;

    }
    public Contact(String name, String phonenumber, int photo){
        this.name = name;
        this.photo = photo;
        this.phonenumber = phonenumber;

    }

    public Contact(int id, String name, String phoneNumber, int gender) {
        this.name = name;
        this.id=id;
        this.photo = gender;
        this.phonenumber = phoneNumber;

    }

    public String getName(){
        return name;
    }
    public int getPhoto() {
        return photo;
    }
    public String getNumber() {
        return phonenumber;
    }

    public  void setName(String name){
        this.name = name;
    }
    public void setPhoto(int photo) {
        this.photo = photo;
    }
    public void setPhonenumber( String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }
}
