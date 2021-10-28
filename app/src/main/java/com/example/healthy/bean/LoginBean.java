package com.example.healthy.bean;

public class LoginBean {
    
    public Boolean success;
    public String message;
    public Integer code;
    public ResultBean result;
    public Long timestamp;
    
    public static class ResultBean {
        public String pk;
        public Integer userid;
    }
}
