package com.example.healthy.bean;

public class TokenBean {
    
    public Boolean success;
    public String message;
    public Integer code;
    public ResultBean result;
    public Long timestamp;
    
    public static class ResultBean {
        public Integer expires;
        public String token;
        public String apiserver;
    }
}
