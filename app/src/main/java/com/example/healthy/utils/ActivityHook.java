package com.example.healthy.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 通过Hook Activity 的 mInstrumentation 字段，代理startActivity过程
 */

public class ActivityHook {
    private static final String TAG = "ActivityHook";

    /**
     * Hook activity 的  mInstrumentation
     */
    public static void replaceInstrumentation(Activity activity) {
        try {
            Log.e(TAG, "replaceInstrumentation");
            Field field = Activity.class.getDeclaredField("mInstrumentation");
            field.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) field.get(activity);
            ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(instrumentation);
            field.set(activity, proxyInstrumentation);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * getApplicationContext().startActivity()
     * ContextImpl.startActivity()
     * <p>
     * Hook mMainThread.getInstrumentation().execStartActivity 位置
     */
    @SuppressLint("PrivateApi")
    public static void replaceFullIns() {
        try {
            Class<?> atClass = Class.forName("android.app.ActivityThread");
            Field currentThreadField = atClass.getDeclaredField("sCurrentActivityThread");
            currentThreadField.setAccessible(true);
            Object currentThread = currentThreadField.get(null);
            Log.e(TAG, "current thread is " + currentThread);

            Field instrument = atClass.getDeclaredField("mInstrumentation");
            instrument.setAccessible(true);
            Instrumentation insOb = (Instrumentation) instrument.get(currentThread);
            Log.e(TAG, "current  instru is " + insOb);

            ProxyInstrumentation proxyInstrumentation = new ProxyInstrumentation(insOb, "full");
            instrument.set(currentThread, proxyInstrumentation);

            //android P 之后无法获得隐藏的field
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P){
                Method forName = Class.class.getDeclaredMethod("forName", String.class);
                Method getDeclared = Class.class.getDeclaredMethod("getDeclaredMethod",
                        String.class, Class[].class);
                Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");
                Method getRuntime = (Method) getDeclared.invoke(vmRuntimeClass, "getRuntime", null);
                Method setHidden = (Method) getDeclared.invoke(vmRuntimeClass,
                        "setHiddenApiExemptions", new Class[]{String[].class});
                Object svm = getRuntime.invoke(null);
                setHidden.invoke(svm, new Object[]{new String[]{"L"}});
            }
            Field thread = Instrumentation.class.getDeclaredField("mThread");
            thread.setAccessible(true);
//            thread.set(insOb, currentThread);
            thread.set(proxyInstrumentation, currentThread);

            Log.e(TAG, "succeed hook");
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static class ProxyInstrumentation extends Instrumentation {
        private static final String TAG = "HookInstrumentation";
        private final Instrumentation instrumentation;
        private String type = "normal";

        public ProxyInstrumentation(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        public ProxyInstrumentation(Instrumentation instrumentation, String type){
            this.instrumentation = instrumentation;
            this.type = type;
        }

        public Instrumentation.ActivityResult execStartActivity(
                Context who, IBinder contextThread, IBinder token, Activity target,
                Intent intent, int requestCode, Bundle options) {
            Log.e(TAG, type + "hook start activity " + who);
            Class<? extends Instrumentation> ob = Instrumentation.class;
            try {
                @SuppressLint("DiscouragedPrivateApi") Method exec = ob.getDeclaredMethod("execStartActivity",
                        Context.class, IBinder.class, IBinder.class,
                        Activity.class, Intent.class, int.class, Bundle.class);
                exec.setAccessible(true);
                return (Instrumentation.ActivityResult) exec.invoke(instrumentation, who, contextThread, token, target,
                        intent, requestCode, options);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
