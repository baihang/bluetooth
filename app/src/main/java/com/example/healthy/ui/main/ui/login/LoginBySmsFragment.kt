package com.example.healthy.ui.main.ui.login

import android.content.res.ColorStateList
import android.graphics.Color
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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.bean.UserSetting
import com.example.healthy.databinding.FragmentLoginSmsBinding
import com.example.healthy.ui.main.SettingViewModel
import com.example.healthy.utils.JsonUtil
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.SharedPreferenceUtil
import com.google.android.material.snackbar.Snackbar

class LoginBySmsFragment : Fragment() {

    companion object {
        const val TAG = "LoginBySmsFragment"
    }

    private val viewModel by lazy {
        ViewModelProvider(requireActivity().viewModelStore, LoginViewModelFactory()).get(
            LoginViewModel::class.java
        )
    }

    private var binding: FragmentLoginSmsBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_sms, container, false)
        binding = FragmentLoginSmsBinding.bind(view)
        return view
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.smsGet?.setOnClickListener {
            viewModel.postGetSms(binding?.mobileEt?.text.toString())
        }

        binding?.smsEt?.addTextChangedListener { text ->
            refreshButton(binding?.mobileEt?.text?.toString(), text?.toString())
        }

        binding?.mobileEt?.addTextChangedListener { text ->
            refreshButton(text?.toString(), binding?.smsEt?.text?.toString())
        }

        binding?.login?.setOnClickListener {
            val mobile = binding?.mobileEt?.text?.toString()?.trim()
//            val st = SharedPreferenceUtil.getItem(context, mobile)
//            val user = JsonUtil.jsonStr2Object(st, UserSetting::class.java)
//            if (user == null) {
//                Snackbar.make(this.requireView(), "请重新获取验证码！", Snackbar.LENGTH_LONG).show()
//            } else {
            val code = binding?.smsEt?.text?.toString()?.trim()
            if (mobile.isNullOrEmpty() || code.isNullOrEmpty()) return@setOnClickListener
            viewModel.loginBySms(mobile, code)
//            }
        }

//        if (Build.VERSION.SDK_INT > 29) {
//            val cutout: MutableList<Rect> =
//                WindowInsets.Builder().build().displayCutout?.boundingRects ?: return
//            for (r in cutout) {
//                Log.e(TAG, "cut out = $r")
//            }
//        }

        binding?.mobileEt?.setText(viewModel.userName)

        viewModel.smsStatus.observe(viewLifecycleOwner, Observer { sms ->
            if (sms?.message != null && sms.message.startsWith("测试环境")) {
                val code = Regex("[0-9]+").find(sms.message)
                binding?.smsEt?.setText(code?.value.toString())
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
            if (userSetting.pk != it.token) {
                userSetting.pk = it.token
                userSetting.userId = it.id.toString()
                editor?.putString(viewModel.userName, JsonUtil.object2String(userSetting))
            }
            editor?.putBoolean(SettingViewModel.LOGIN_STATUS, true)
            editor?.putString(SharedPreferenceUtil.CURRENT_USER, viewModel.userName)
            editor?.apply()

            findNavController().navigate(R.id.MainFragment)
        })
    }

    private fun refreshButton(mobile: String?, code: String?) {
        if (mobile.isNullOrEmpty() || code.isNullOrEmpty()) {
            binding?.login?.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#c2c2c2"))
        } else {
            binding?.login?.backgroundTintList = null
        }
    }


}