package com.example.healthy.utils;

import android.util.Log;

public class TestThread {

    private static String TAG = "TestThread";
    volatile int count = 0;
    public void testThreadMax(){
        while (true){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true){}
                }
            });
            thread.start();
            count++;
            Log.e(TAG, "active count = " + Thread.activeCount() + " count = " + count);
        }
    }
}
