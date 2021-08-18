package com.example.healthy

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.healthy.utils.ThreadUtil

import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreadUtilTest {
    private val TAG = "ThreadUtilTest"
    @Test
    fun addThread() {
        Log.e(TAG, "Test Add Thread start")
        val threadPool = ThreadUtil.getInstance()
        for(i in 0 until 30){
            threadPool?.addThread(Runnable {
                Log.e(TAG, "add thread $i")
                while(true){
                    Thread.sleep(1000)
                }
            })
        }
        while(threadPool?.isAlive() ?: 0 > 0){
            Thread.sleep(1000)
        }
    }
}