package com.example.healthy.utils;


import android.util.Log;

import com.example.healthy.bean.AbstractLoadBean;
import com.example.healthy.bean.NetworkBean;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.util.ConnectConsumer;
import io.reactivex.schedulers.Schedulers;

public class RxManagerUtil {

    private static final String TAG = "RxManagerUtil";

    private static RxManagerUtil managerUtil;

    public static synchronized RxManagerUtil getInstance() {
        if (managerUtil == null) {
            managerUtil = new RxManagerUtil();
        }
        return managerUtil;
    }

    private RxManagerUtil() {
    }

    public void load(ManagerListener listener, int tag) {
        Observable
                .create((ObservableOnSubscribe<NetworkBean>) emitter -> {
                    NetworkBean bean = (NetworkBean) listener.load(tag);
                    if (bean != null){
                        emitter.onNext(bean);
                        Log.d(TAG, "on next " + bean);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NetworkBean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull NetworkBean networkBean) {
                        if(networkBean.isSucceed){
                            listener.loadSucceed(networkBean);
                        }else{
                            listener.loadFailed(networkBean);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public interface ManagerListener {
        void loadPre(int tag);

        AbstractLoadBean load(int tag);

        void loadSucceed(AbstractLoadBean bean);

        void loadFailed(AbstractLoadBean bean);
    }

}
