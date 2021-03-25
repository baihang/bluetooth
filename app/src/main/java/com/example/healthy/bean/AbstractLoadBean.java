package com.example.healthy.bean;

import java.io.Serializable;

public abstract class AbstractLoadBean<T> implements Serializable {
    public T data;
    public int tag;
    public boolean isSucceed;
    protected final static String defaultError = "default error";

    @Override
    public String toString() {
        return "AbstractLoadBean{" +
                "data=" + data +
                ", tag=" + tag +
                ", isSucceed=" + isSucceed +
                '}';
    }
}
