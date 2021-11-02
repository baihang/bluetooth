package com.example.healthy.utils;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 参考FastPrintWriter 在写文件之前做一级缓存
 */
public class LogUtil {
    private static final String TAG = "LogUtil";
    private static LogUtil logUtil;

    public static LogUtil getInstance(File file) throws IOException {
        if (logUtil == null) {
            synchronized (LogUtil.class) {
                if (logUtil == null) {
                    logUtil = new LogUtil(file);
                }
            }
        }
        return logUtil;
    }

    private final class StringNode {
        String val;
        StringNode next;

        StringNode(String val) {
            this.val = val;
        }
    }

    private final int bufferLength = 8 * 1024;
    private final char[] buffer;
    private int pos;

    private final Object appendLock;
    private final FileWriter write;


    private OutputStream out;
    private StringNode head = null, tail = head;

    private LogUtil(File file) throws IOException {
        buffer = new char[bufferLength];
        appendLock = new Object();
        write = new FileWriter(file);
        new Thread(getWorkThread(), "Log-thread").start();
    }

    private int targetLevel = Log.INFO;

    public void log(int level, String tag, String val) {
        if(isStop) return;
        if (level > targetLevel) {
            synchronized (appendLock) {
                StringNode node = new StringNode(val);
                if (tail == null) {
                    head = tail = node;
                    appendLock.notify();
                } else {
                    tail.next = node;
                    tail = node;
                }
            }
        }
    }

    private volatile boolean isStop = false;

    private Runnable getWorkThread() {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    String toWrite = "";
                    while (true) {
                        synchronized (appendLock){
                            if (head == null) {
                                if (isStop) {
                                    return;
                                }
                                Log.e(TAG, "write thread is blocking");
                                appendLock.wait(1000);
                            }else{
                                toWrite = head.val;
                                head = head.next;
                            }
                        }
                        write(toWrite);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void stopWriteLog() {
        isStop = true;
    }

    private void write(String str) {
        if (str == null || str.length() == 0) {
            return;
        }
        int start = 0;
        while (pos + str.length() - start > bufferLength) {
            str.getChars(start, start + bufferLength, buffer, pos);
            flush();
            pos = 0;
            start += bufferLength;
        }
        str.getChars(start, str.length(), buffer, pos);
    }

    private void flush() {
        try {
            write.write(buffer);
            write.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
