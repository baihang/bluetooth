package com.example.healthy.utils;

import dalvik.system.PathClassLoader;

public class CustomDexClassLoader extends PathClassLoader {

    public CustomDexClassLoader( String path) {
        super(path, CustomClassLoader.class.getClassLoader());
    }

}
