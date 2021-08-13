package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class SIM extends DataSupport {

    private int id;


    private String iccid;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIccid() {
        return iccid;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }
}
