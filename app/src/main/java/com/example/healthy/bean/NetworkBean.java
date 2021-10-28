package com.example.healthy.bean;

import java.io.IOException;

import okhttp3.Response;
import okio.Okio;

public class NetworkBean<T> extends AbstractLoadBean<T> {

    public int err_code = 0;
    public String err_msg = "";

    public NetworkBean(Response response) throws IOException {
        if(response != null){
            err_code = response.code();
            isSucceed = err_code == 200;
            err_msg = response.message();
            data = (T)response.body().string();
        }
        err_msg = defaultError;
        err_code = -1;
    }

    public NetworkBean(){
        err_code = -1;
        err_msg = "访问失败";
        isSucceed = false;
    }

    public NetworkBean(int code, String msg){
        err_code = code;
        err_msg = msg;
    }

}
