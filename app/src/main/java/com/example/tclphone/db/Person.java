package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class Person extends DataSupport {

    private int id;

  //  @Column(nullable = false)
    private String name;

   // @Column(nullable = false)
    private String phoneNumber;

   // @Column(nullable = false)
    private int storage;

  //  @Column(nullable = false)
    private int photo;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
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
