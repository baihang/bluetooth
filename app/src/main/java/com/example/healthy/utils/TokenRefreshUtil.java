package com.example.healthy.utils;

import android.content.Context;
import android.util.Log;

import com.example.healthy.bean.UserSetting;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TokenRefreshUtil {

    private static TokenRefreshUtil instance;
    private final Context context;

    public static TokenRefreshUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (TokenRefreshUtil.class) {
                if (instance == null) {
                    instance = new TokenRefreshUtil(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static TokenRefreshUtil getInstance(){
        return instance;
    }

    public static void destroy() {
        instance = null;
    }

    private TokenRefreshUtil(Context context) {
        this.context = context;
    }

    public UserSetting refreshToken() {
        return NetWortUtil.getToken(context);
    }

    public static String getMd5(String str){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] ek = md5.digest(str.getBytes());
            StringBuilder builder = new StringBuilder();
            for (byte b : ek){
                builder.append(Integer.toHexString(b & 0x000000FF | 0xFFFFFF00).substring(6));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private static volatile int retry = 0;

    public String getToken(){
        UserSetting userSetting = SharedPreferenceUtil.Companion.getUserSetting(context, "");
        if(true){
            return userSetting.pk;
        }
        if(userSetting.token == null || userSetting.tokenTime < (System.currentTimeMillis() / 1000)){
            Log.e("getToken", " token " + userSetting.tokenTime + " time = " + (System.currentTimeMillis() / 1000) );
            userSetting = refreshToken();
        }
        if(userSetting == null || userSetting.token == null){
            retry++;
            if(retry >= 3){

            }
            return "";
        }
        retry = 0;
        return userSetting.token;
    }

    public static String getUserId(Context context){
        UserSetting userSetting = SharedPreferenceUtil.Companion.getUserSetting(context, "");
        return userSetting.userId;
    }
}
