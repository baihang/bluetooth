package com.example.healthy.utils;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.json.JSONTokener;

import java.lang.reflect.Field;
import java.lang.reflect.ReflectPermission;

public class JsonUtil {

    private static final Gson gson = new Gson();

    public static Object strToObject(Class clazz, String jsonStr) {
        if (clazz == null || jsonStr == null || jsonStr.isEmpty())
            return null;
        try {
            JSONObject json = new JSONObject(jsonStr);
            Field[] fields = clazz.getFields();
            Object object = clazz.newInstance();
            for(Field f : fields){
                Object ob = json.get(f.getName());
                f.set(object, ob);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T jsonStr2Object(String str, Class<T> t){
        return gson.fromJson(str, t);
    }

    public static String object2String(Object ob){
        return gson.toJson(ob);
    }

}
