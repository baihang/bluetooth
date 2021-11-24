package com.example.healthy.ui.main;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.healthy.chart.MyLineChart;
import com.example.healthy.utils.NetWortUtil;
import com.example.healthy.utils.SharedPreferenceUtil;

public class SettingViewModel extends ViewModel {

    public static String LIMIT_TYPE = "limit_type";
    public static String LIMIT_MAX = "limit_max";
    public static String LIMIT_MIN = "limit_min";
    public static String SERVER_URL = "server_url";
    public static String X_MAX = "x_max";

    public static String LOGIN_STATUS = "isLogin";

    public MutableLiveData<Integer> limitType = new MutableLiveData<>();
    public MutableLiveData<String> serverUrl = new MutableLiveData<>();
    public MutableLiveData<Integer> limitMax = new MutableLiveData<>();
    public MutableLiveData<Integer> limitMin = new MutableLiveData<>();
    public MutableLiveData<Integer> xMax = new MutableLiveData<>();

    public SettingViewModel(Context context) {
        SharedPreferences preferences = SharedPreferenceUtil.Companion.getSharedPreference(context);
        limitType.setValue(preferences.getInt(LIMIT_TYPE, 0));
        limitMax.setValue(preferences.getInt(LIMIT_MAX, 0));
        limitMin.setValue(preferences.getInt(LIMIT_MIN, 0));
        serverUrl.setValue(preferences.getString(SERVER_URL, NetWortUtil.DEFAULT_BASE_URL));
        xMax.setValue(preferences.getInt(X_MAX, MyLineChart.MAX_X_LENGTH));
    }

    public void loginOut(Context context) {
        SharedPreferences.Editor editor = SharedPreferenceUtil.Companion.getEditor(context);
        assert editor != null;
        editor.putBoolean(LOGIN_STATUS, false);
        editor.apply();
    }

    public void visitor(Context context) {
        SharedPreferences.Editor editor = SharedPreferenceUtil.Companion.getEditor(context);
        assert editor != null;
        editor.putBoolean(LOGIN_STATUS, true);
        editor.apply();
    }

    public void setxMax(Context context, int value) {
        SharedPreferences.Editor editor = SharedPreferenceUtil.Companion.getEditor(context);
        int old = xMax.getValue() != null ? xMax.getValue() : 0;
        if(editor == null || value == old){
            return;
        }
        editor.putInt(X_MAX, value);
        editor.apply();
    }

}
