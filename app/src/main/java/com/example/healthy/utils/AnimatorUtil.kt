package com.example.healthy.utils

import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation

fun testFade(view: View?, alphaValue:Boolean){
    view?.apply {
        if(alphaValue){
            alpha = 1f
            animate().setDuration(1000L).alpha(0F)
        }else {
            alpha = 0f
            animate().setDuration(1000L).alpha(1F)
        }
    }
}

fun testFling(view: View?){
    FlingAnimation(view, DynamicAnimation.ROTATION_X).apply {
        setStartVelocity(200f)
        setFriction(1f)
        start()
    }
}

fun testClassLoader(){

}