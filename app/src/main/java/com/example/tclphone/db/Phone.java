package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class Phone extends DataSupport {

    private int id;

    private String Phone;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }
}
