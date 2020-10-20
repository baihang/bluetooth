package com.example.healthy.utils

import android.content.Context
import android.content.SharedPreferences
import java.util.*

/**
 * SharedPreference 存储
 */
class SharedPreferenceUtil {

    companion object {

        fun getEditor(context: Context?): SharedPreferences.Editor? {
            val sharedPreferences = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return sharedPreferences?.edit()
        }

        fun getSharedPreference(context: Context?): SharedPreferences? {
            return context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }

    }

}