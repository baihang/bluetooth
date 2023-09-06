package com.example.healthy.utils

import android.view.View

var clickTime = 0L
fun View.singleClick(time: Long = 800, click: () -> Unit){
    setOnClickListener {
        if(System.currentTimeMillis() - clickTime > time){
            clickTime = System.currentTimeMillis()
            click.invoke()
        }
    }
}