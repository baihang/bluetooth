package com.example.healthy.utils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Debug;
import android.os.Trace;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.healthy.R;

public class TestImageLoad extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_image_load);
    }

    @Override
    protected void onResume() {
        super.onResume();

        ImageView image = findViewById(R.id.test_image);
        Debug.startMethodTracing();
        Glide.with(getApplicationContext())
                .load("https://res.shiqichuban.com/v1/image/get/oTlp2YkpNWIXmp-TI7Nt4Jm8U6BwwPa9ln4wJvxP2ZIoqtm2r0nnlIkbXTemVHo3ajFKbUK-Hpycz2LNJAD8jg")
                .into(image);
        Debug.stopMethodTracing();
    }
}