package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class Contacts extends DataSupport {
    private int id;

   // @Column(nullable = false)
    private String name;

   // @Column(nullable = false)
    private String phoneNumber;

   // @Column(nullable = false)
    private int storage;

    //Column(nullable = false)
    private int photo;

    public Contacts(){}
    public Contacts(int id, String name, String phoneNumber, int storage, int gender) {
        this.id=id;
        this.name=name;
        this.phoneNumber=phoneNumber;
        this.storage=storage;
        this.photo=gender;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }
}
