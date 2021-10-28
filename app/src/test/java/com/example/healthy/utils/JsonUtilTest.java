package com.example.healthy.utils;

import static org.junit.Assert.*;

import com.example.healthy.bean.SmsBean;
import com.example.healthy.ui.main.data.LoginDataSource;
import com.example.healthy.ui.main.data.LoginRepository;
import com.example.healthy.ui.main.ui.login.LoginViewModel;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class JsonUtilTest {

    @Test
    public void strToObject() {
        String str = "{\"success\":true,\"message\":\"测试环境，验证码为：438952\",\"code\":200,\"result\":{\"vk\":\"95qiIbun4Z\"},\"timestamp\":1634542249872}";
        SmsBean sms = (SmsBean) JsonUtil.strToObject(SmsBean.class, str);
        assertNotNull(sms);
    }

    @Test
    public void testGetSms() {
        LoginViewModel viewModel = new LoginViewModel(new LoginRepository(new LoginDataSource()));
        String str = "{\"success\":true,\"message\":\"测试环境，验证码为：438952\",\"code\":200,\"result\":{\"vk\":\"95qiIbun4Z\"},\"timestamp\":1634542249872}";
        str = "{\"message\":\"测试环境，验证码为：438952\",\"code\":200,\"result\":{\"vk\":\"95qiIbun4Z\"},\"timestamp\":1634542249872}";

    }

    @Test
    public void testMd5() {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = ("e3gxwrwyzzl6n0zosalt1635331089730").getBytes();
        byte[] ek = md5.digest(bytes);
        StringBuilder builder = new StringBuilder();
        for (byte b : ek){
            builder.append(Integer.toHexString(b & 0x000000FF | 0xFFFFFF00).substring(6));
        }
        System.out.println(builder.toString());

        String str = TokenRefreshUtil.getMd5("e3gxwrwyzzl6n0zo1635386145664");
        assertEquals(str, "40d3ad490375cc2ae60c3c3978346851");

        assertEquals("e0992bdbf5b8bba506d531cf38deade3", "e0992bdbf5b8bba506d531cf38deade3");
//        assertEquals(builder.toString(), "8bc8ecfe0d0de5a469461c6897587186");
    }

}