package com.example.tclphone.db;


import com.example.tclphone.litepal.crud.DataSupport;

public class Message extends DataSupport {

    private int id;

    //@Column(nullable = false)
    private String name;

   // @Column(nullable = false)
    private String phoneNumber;

   // @Column(nullable = false)
    private int photo;

   // @Column(nullable = false)
    private String time_operation;//发送或接收时间

  //  @Column(nullable = false)
    private String content;

   // @Column(nullable = false)
    private int type;//1接收，0发送
  //  @Column(nullable = false)
    private int read;//1已读，0未读


    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getPhoto() {
        return photo;
    }

    public void setPhoto(int photo) {
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

    public String getPhonenumber() {
        return phoneNumber;
    }

    public void setPhonenumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTime_operation() {
        return time_operation;
    }

    public void setTime_operation(String time_operation) {
        this.time_operation = time_operation;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
