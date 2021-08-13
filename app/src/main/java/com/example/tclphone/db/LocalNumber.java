package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class LocalNumber extends DataSupport {
    private int id;

    //@Column(nullable = false)
    private String phoneNumber;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}

