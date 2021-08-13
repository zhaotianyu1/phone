package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class Records extends DataSupport {
    private int id;

  //  @Column(nullable = true)
    private String name;

   // @Column(nullable = false)
    private String phoneNumber;

    //@Column(nullable = false)
    private String timeStart;

   // @Column(nullable = false)
    private String duration;

   // @Column(nullable = false)
    private int mode;

   // @Column(nullable = false)
    private int status;

   // @Column(nullable = false)
    private int photo;

    //无参构造方法
    public Records(){}

    //含参构造方法
    public Records(int id, String name, String phoneNumber, String timeStart, String duration, int mode, int status, int photo){
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.timeStart = timeStart;
        this.duration = duration;
        this.mode = mode;
        this.status = status;
        this.photo = photo;
    }

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

    public String getTimeStart() {
        return timeStart;
    }

    public void setTimeStart(String timeStart) {
        this.timeStart = timeStart;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
        this.photo = photo;
    }

}
