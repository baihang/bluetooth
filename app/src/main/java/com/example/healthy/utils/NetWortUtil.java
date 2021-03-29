package com.example.healthy.utils;

import android.text.TextUtils;

import com.example.healthy.bean.NetworkBean;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetWortUtil {

    public static final String TAG = "NetWortUtil";

    public static final String BASE_URL = "http://www.vipmember.com.cn";

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final static OkHttpClient client = new OkHttpClient();

    private static NetworkBean<String> post(String url, RequestBody body) {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try {
            Response response = client.newCall(request).execute();
            return new NetworkBean<>(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new NetworkBean<>();
    }

    private static NetworkBean<String> post(String url, Map<String, String> jsonParam) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : jsonParam.entrySet()) {
            if (!TextUtils.isEmpty(entry.getValue())) {
                builder.add(entry.getKey(), entry.getValue());
            } else {
                builder.add(entry.getKey(), "");
            }
        }
        return post(url, builder.build());
    }



    public static NetworkBean<String> upEcgData(String param) throws IOException {
        String url = BASE_URL + "/msg/uploadEcg";
        RequestBody body = RequestBody.create(JSON, param);
        return post(url, body);
    }

    public static NetworkBean<String> login(String phone, String pwd) throws IOException {
        String url = BASE_URL + "/user/login";
        Map<String, String> map = new HashMap<>();
        map.put("phonenum", phone);
        map.put("password", pwd);
        return post(url, map);
    }

}
