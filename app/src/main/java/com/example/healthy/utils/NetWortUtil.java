package com.example.healthy.utils;

import android.content.Context;
import android.text.TextUtils;

import com.bumptech.glide.Glide;
import com.example.healthy.bean.NetworkBean;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetWortUtil {

    public static final String TAG = "NetWortUtil";

    public static final String DEFAULT_BASE_URL = "http://www.vipmember.com.cn";

    private static String base_url = DEFAULT_BASE_URL;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final static OkHttpClient client = new OkHttpClient().newBuilder()
            .eventListenerFactory(NetWorkEventListener.Companion.getFACTORY())
            .build();

    public static void refreshUrl(Context context) {
        base_url = SharedPreferenceUtil.Companion.getSharedPreference(context).
                getString("url", DEFAULT_BASE_URL);
    }

    private static String getFullUrl(String url) {
        return DEFAULT_BASE_URL + url;
    }

    private static NetworkBean<String> post(String url, RequestBody body) {
        Request request = new Request.Builder()
                .url(getFullUrl(url))
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

    /**
     * 异步post
     */
    private static void asynchronousPost(String url, RequestBody body, Callback callback) {
        Request request = new Request.Builder()
                .url(getFullUrl(url))
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static NetworkBean<String> post(String url, Map<String, String> jsonParam) throws IOException {
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

    public static final MediaType MEDIA_TYPE_MARKDOWN
            = MediaType.parse("text/x-markdown; charset=utf-8");

    public static NetworkBean<String> uploadFile(String url, String path){
        File file = new File(path);
        if(file.exists() && file.length() > 0){
            return new NetworkBean<>();
        }

        RequestBody body = RequestBody.create(MEDIA_TYPE_MARKDOWN, file);
        return post(url, body);
    }

    public static NetworkBean<String> post(String url, String json) {
        RequestBody body = RequestBody.create(json, JSON);
        return post(url, body);
    }

    public static NetworkBean<String> upEcgData(String param) throws IOException {
        String url = "/msg/uploadEcg";
        RequestBody body = RequestBody.create(JSON, param);
        return post(url, body);
    }

    public static NetworkBean<String> login(String phone, String pwd) throws IOException {
        String url = "/user/login";
        Map<String, String> map = new HashMap<>();
        map.put("phonenum", phone);
        map.put("password", pwd);
        return post(url, map);
    }

}
