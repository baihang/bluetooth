package com.example.healthy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.example.healthy.bean.NetworkBean;
import com.example.healthy.bean.TokenBean;
import com.example.healthy.bean.UserSetting;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetWortUtil {

    public static final String TAG = "NetWortUtil";
    public static final String URL = "url";

    private static volatile boolean tokenGetting = false;

    public static final String DEFAULT_BASE_URL = "http://www.vipmember.com.cn";

    public static final String BASE_URL_KER = "https://api.yurui1021.com";

    private static String base_url = BASE_URL_KER;

    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");

    private final static OkHttpClient client = new OkHttpClient().newBuilder()
            .eventListenerFactory(NetWorkEventListener.Companion.getFACTORY())
            .build();

    public static void refreshUrl(Context context) {
        base_url = SharedPreferenceUtil.Companion.getSharedPreference(context)
                .getString("url", DEFAULT_BASE_URL);
    }

    private static String getFullUrl(String url) {
        return base_url + url;
    }

    private static Headers getHeaders(){
        Headers.Builder builder = new Headers.Builder();
        builder.add("token", TokenRefreshUtil.getInstance().getToken());
        return builder.build();
    }

    private static NetworkBean<String> post(String url, RequestBody body) {
        return postFullUrl(getFullUrl(url), body);
    }

    private static NetworkBean<String> postFullUrl(String url, RequestBody body) {
        Request request = new Request.Builder()
                .url(url)
                .headers(getHeaders())
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

    public static NetworkBean<String> uploadFile(String url, String path) {
        File file = new File(path);
        if (file.exists() && file.length() > 0) {
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
//        String url = "/msg/uploadEcg";
        String url = "/uptxt/all";
        RequestBody body = RequestBody.create(param, MEDIA_TYPE_MARKDOWN);
        return post(url, body);
    }

    public static NetworkBean<String> login(String phone, String pwd) throws IOException {
        String url = "/user/login";
        Map<String, String> map = new HashMap<>();
        map.put("phonenum", phone);
        map.put("password", pwd);
        return post(url, map);
    }

    public static synchronized UserSetting getToken(Context context){
        if(tokenGetting){
            return null;
        }
        String url = "/token";
        JSONObject json = new JSONObject();
        UserSetting userSetting = SharedPreferenceUtil.Companion.getUserSetting(context, null);
        if(userSetting.pk == null || userSetting.pk.isEmpty()){
            return null;
        }
        try {
            String salt = String.valueOf(System.currentTimeMillis());
            String ek = TokenRefreshUtil.getMd5(userSetting.pk + salt);
            Log.e(TAG, "ek = " + userSetting.pk + salt);
            json.put("salt", salt);
            json.put("ek", ek);
            json.put("userid", userSetting.userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        tokenGetting = true;
        NetworkBean<String> result = post(url, json.toString());
        tokenGetting = false;

        if(result.isSucceed){
            Log.d(TAG, "token result = " + result + " request = " + json.toString());
            TokenBean token = JsonUtil.jsonStr2Object(result.data, TokenBean.class);
            if(!token.success){
                return null;
            }
            if(userSetting.token == null || !userSetting.token.equals(token.result.token)){
                userSetting.token = token.result.token;
                userSetting.tokenTime = token.result.expires + (System.currentTimeMillis() / 1000);
                Log.e(TAG, "token expires = " + token.result.expires + " token time = " + userSetting.tokenTime);
                userSetting.apiServer = token.result.apiserver;
            }
            SharedPreferences shared = SharedPreferenceUtil.Companion.getSharedPreference(context);
            SharedPreferences.Editor editor = shared.edit();
            String user_name = shared.getString(SharedPreferenceUtil.CURRENT_USER, userSetting.userName);
            editor.putString(user_name, JsonUtil.object2String(userSetting));
            if(userSetting.apiServer.equals(shared.getString(URL, ""))){
                base_url = userSetting.apiServer;
                editor.putString(URL, userSetting.apiServer);
            }
            if(userSetting.userName != null && userSetting.userName.length() != 0 &&
                    !shared.getString(SharedPreferenceUtil.CURRENT_USER, "").equals(userSetting.userName)){
                editor.putString(SharedPreferenceUtil.CURRENT_USER, userSetting.userName);
            }
            editor.apply();
            Log.e(TAG, "user setting = " + JsonUtil.object2String(userSetting));
            return userSetting;
        }
        return null;
    }

}
