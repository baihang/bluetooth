package com.example.healthy.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CustomClassLoader extends ClassLoader {

    private static final String TAG = "CustomClassLoader";

    private Context context;

    public CustomClassLoader(Context context) {
        super(CustomClassLoader.class.getClassLoader());
        this.context = context;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        AssetManager ass = context.getAssets();
        try {
            InputStream in = ass.open("TestClass.class");
            byte[] bytes = new byte[1024 * 4];
            ByteArrayOutputStream data = new ByteArrayOutputStream();
            int len = 0;
            while ((len = in.read(bytes)) > 0) {
                data.write(bytes, 0, len);
            }
            byte[] classes = data.toByteArray();
            Log.e(TAG, "class length = " + classes.length);
            //无法执行，直接抛异常,ClassLoader::defineClass中无逻辑
            Class clazz = defineClass("TestClass.java", classes, 0, classes.length);

            //会死循环
//            Class clazz = Class.forName("TestClass.java", false, this);
            if (clazz != null) {
                Object ob = clazz.newInstance();
                Log.e(TAG, "ob = " + ob);
            }
            return clazz;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return super.findClass(name);
    }


}
