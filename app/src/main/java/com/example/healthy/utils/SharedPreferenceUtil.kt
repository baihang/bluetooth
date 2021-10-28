package com.example.healthy.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.healthy.bean.UserSetting
import java.util.*

/**
 * SharedPreference 存储
 */
class SharedPreferenceUtil {

    companion object {

        const val CURRENT_USER = "CURRENT_USER"

        fun getEditor(context: Context?): SharedPreferences.Editor? {
            val sharedPreferences = context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
            return sharedPreferences?.edit()
        }

        fun getSharedPreference(context: Context?): SharedPreferences? {
            return context?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        }

        fun getItem(context: Context?, name: String): String? {
            return getSharedPreference(context)?.getString(name, null)
        }

        fun getUserSetting(context: Context?, userId : String? = ""): UserSetting {
            val setting = SharedPreferenceUtil.getSharedPreference(context)
            var userName = userId
            if(userName.isNullOrEmpty()){
                userName = setting?.getString(CURRENT_USER, "")
            }
            val user = setting?.getString(userName, "") ?: ""
            val userSetting =
                if (user.isNotEmpty()) {
                    JsonUtil.jsonStr2Object(user, UserSetting::class.java)
                } else {
                    UserSetting()
                }
            return userSetting
        }

    }

}