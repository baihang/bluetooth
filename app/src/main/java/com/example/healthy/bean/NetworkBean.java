package com.example.healthy.bean;

import java.io.IOException;

import okhttp3.Response;

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

}
