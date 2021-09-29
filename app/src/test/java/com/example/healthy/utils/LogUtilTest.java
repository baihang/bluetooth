package com.example.healthy.utils;

import static org.junit.Assert.*;

import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class LogUtilTest {

    @Test
    public void write(){
        try {
            LogUtil logUtil = LogUtil.getInstance(new File("log_text.file"));
            for(int i = 0; i< 10000; i++){
                logUtil.log(Log.ERROR, "test", "test log " + i + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}