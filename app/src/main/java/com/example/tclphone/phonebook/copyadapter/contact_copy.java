package com.example.tclphone.phonebook.copyadapter;


/**
 * 电话簿复制实体类
 */

public class contact_copy {


    private int id;
    private int storage;
    private String name;
    private  int photo;
    private String phonenumber;
    private boolean isshow=false;

    private String ids;

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public void setIsshow(boolean isshow) {
        this.isshow = isshow;
    }
    public boolean getIsshow() {
        return isshow;
    }
    public String getPhonenumber() {
        return phonenumber;
    }

    public contact_copy(){

    }
    public contact_copy(int id, String name, String phonenumber , int storage, int photo ){
        this.name = name;
        this.photo = photo;
        this.phonenumber = phonenumber;
        this.id=id;
        this.storage=storage;
    }
    public contact_copy(int id, String name, String phonenumber, int photo){
        this.id=id;
        this.name = name;
        this.photo = photo;
        this.phonenumber = phonenumber;

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
