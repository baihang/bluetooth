package com.example.healthy.bean;


public class SmsBean{

    public Boolean success;
    public String message;
    public String code;
    public ResultBean result;
    public Long timestamp;

    public static class ResultBean {
        public String vk;
    }
}
