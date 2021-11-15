package com.example.healthy.ui.main.ui.login

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.bean.UserSetting
import com.example.healthy.ui.main.SettingViewModel
import com.example.healthy.utils.JsonUtil
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.SharedPreferenceUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_login_sms.*

class LoginBySmsFragment : Fragment() {

    companion object {
        const val TAG = "LoginBySmsFragment"
    }

    private val viewModel by lazy {
        ViewModelProvider(requireActivity().viewModelStore, LoginViewModelFactory()).get(
            LoginViewModel::class.java
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_sms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sms_get.setOnClickListener {
            viewModel.postGetSms(mobile_et.text.toString())
        }

        login.setOnClickListener {
            val mobile = mobile_et.text.toString().trim()
            val st = SharedPreferenceUtil.getItem(context, mobile)
            val user = JsonUtil.jsonStr2Object(st, UserSetting::class.java)
            if (user == null) {
                Snackbar.make(this.requireView(), "请重新获取验证码！", Snackbar.LENGTH_LONG).show()
            } else {
                viewModel.loginBySms(mobile, sms_et.text.toString(), user.vk)
            }
        }

//        if (Build.VERSION.SDK_INT > 29) {
//            val cutout: MutableList<Rect> =
//                WindowInsets.Builder().build().displayCutout?.boundingRects ?: return
//            for (r in cutout) {
//                Log.e(TAG, "cut out = $r")
//            }
//        }

        mobile_et.setText(viewModel.userName)

        viewModel.smsStatus.observe(viewLifecycleOwner, Observer { sms ->
            if (sms?.message != null && sms.message.startsWith("测试环境")) {
                val code = Regex("[0-9]+").find(sms.message)
                sms_et.setText(code?.value.toString())
            }
            val userSetting = SharedPreferenceUtil.getUserSetting(context, viewModel.userName)
            if (userSetting.vk != sms.result.vk) {
                userSetting.vk = sms.result.vk
                val editor = SharedPreferenceUtil.getEditor(context)
                editor?.putString(viewModel.userName, JsonUtil.object2String(userSetting))
                editor?.apply()
            }
        })

        viewModel.loginStatus.observe(viewLifecycleOwner, Observer { it ->
            val userSetting = SharedPreferenceUtil.getUserSetting(context, viewModel.userName)
            val editor = SharedPreferenceUtil.getEditor(context)
            if (userSetting.pk != it.result.pk) {
                userSetting.pk = it.result.pk
                userSetting.userId = it.result.userid.toString()
                editor?.putString(viewModel.userName, JsonUtil.object2String(userSetting))
            }
            editor?.putBoolean(SettingViewModel.LOGIN_STATUS, true)
            editor?.putString(SharedPreferenceUtil.CURRENT_USER, viewModel.userName)
            editor?.apply()

            findNavController().navigate(R.id.MainFragment)
        })
    }



}