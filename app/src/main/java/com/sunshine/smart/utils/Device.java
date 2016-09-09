package com.sunshine.smart.utils;

/**
 * Created by mac on 16/8/10.
 */
public class Device{
    private String address;//MAC
    private String name;//名称
    private String password;//密码
    private Integer type;//类型  根据名称定义的类型

    public Device(String address, String name,String password,Integer type) {
        this.address = address;
        this.type = type;
        this.name = name;
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Device() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}