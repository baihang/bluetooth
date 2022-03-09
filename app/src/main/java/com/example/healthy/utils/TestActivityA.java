package com.example.healthy.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.healthy.R;

public class TestActivityA extends AppCompatActivity {

    public static final String TAG = "TestActivity";
    public static final String TAG_A = "TestActivity_A ";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivityA.this, TestActivityB.class);
                startActivity(intent);
            }
        });
        Log.e(TAG, TAG_A + "onCreate");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, TAG_A + "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, TAG_A + "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e(TAG, TAG_A + "onRestart");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(TAG, TAG_A + "onNewIntent");
    }

    @Override
    protected void onPause() {
        Log.e(TAG, TAG_A + "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e(TAG, TAG_A + "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, TAG_A + "onDestroy");
    }
}
