package com.example.healthy.ui.main.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.core.text.isDigitsOnly
import com.example.healthy.R
import com.example.healthy.bean.AbstractLoadBean
import com.example.healthy.bean.LoginBean
import com.example.healthy.bean.NetworkBean
import com.example.healthy.bean.SmsBean
import com.example.healthy.ui.main.data.LoginRepository
import com.example.healthy.ui.main.data.Result
import com.example.healthy.utils.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject
import retrofit2.converter.gson.GsonConverterFactory


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private final val TAG = "LoginViewModel"
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    val VISITOR = ""

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    val registerResult = MutableLiveData<LoginResult>()
    val smsStatus = MutableLiveData<SmsBean>()
    val loginStatus = MutableLiveData<LoginBean>()

    var userName = ""
    private var password = ""
    private var smsCode = ""
    private var vk = ""

    companion object {
        private const val TYPE_LOGIN_PASSWORD = 1
        private const val TYPE_LOGIN_GET_SMS = 2
        private const val TYPE_LOGIN_SMS_LOGIN = 3
        private const val TYPE_REFRESH_TOKEN = 4
    }


    private val utilManagerListener = object : RxManagerUtil.ManagerListener {
        override fun loadPre(tag: Int) {

        }

        override fun load(tag: Int): AbstractLoadBean<*> {
            val result: NetworkBean<String> = if (tag == TYPE_LOGIN_PASSWORD) {
                NetWortUtil.login(userName, password)
            } else if (tag == TYPE_LOGIN_SMS_LOGIN) {
                smsLogin(userName, smsCode, vk)
            } else if (tag == TYPE_LOGIN_GET_SMS) {
                getSms(userName)
            } else if (tag == TYPE_REFRESH_TOKEN) {
                TokenRefreshUtil.getInstance().refreshToken()
                NetworkBean()
            } else {
                NetworkBean<String>()
            }
            result.tag = tag
            Log.e(TAG, "result = $result")
            return result
        }

        override fun loadSucceed(bean: AbstractLoadBean<*>?) {
            if (bean == null) return
            if (bean.tag == TYPE_LOGIN_GET_SMS) {
                getSmsSuccess(bean.data.toString())
                return
            } else if (bean.tag == TYPE_LOGIN_SMS_LOGIN) {
                smsLoginSuccess(bean.data.toString())
            }

        }

        override fun loadFailed(bean: AbstractLoadBean<*>) {
            _loginResult.value = LoginResult(
                error = "error code = ${(bean as NetworkBean).err_code}"
            )
            Log.e(TAG, "load failed")
        }

    }

    fun login(username: String, password: String) {
        if (username == VISITOR && password.isNullOrEmpty()) {
            _loginResult.value = LoginResult(
                success = LoggedInUserView(displayName = VISITOR)
            )
            return
        }
        userName = username
        this.password = password

        RxManagerUtil.getInstance().load(utilManagerListener, TYPE_LOGIN_PASSWORD)
    }

    fun loginBySms(phone: String, code: String, vk: String) {
        if (isPhoneValid(phone) && code.length > 4) {
            this.userName = phone
            this.smsCode = code
            this.vk = vk
            RxManagerUtil.getInstance().load(utilManagerListener, TYPE_LOGIN_SMS_LOGIN)
        }
    }

    fun postGetSms(phone: String) {
        userName = phone
        RxManagerUtil.getInstance().load(utilManagerListener, TYPE_LOGIN_GET_SMS)
    }

    //{"success":true,"message":"操作成功！","code":200,"result":{"pk":"ulvvdcmlbxhv499c","userid":1698596},"timestamp":1635233991333}
    private fun smsLoginSuccess(result: String) {
        val bean = JsonUtil.jsonStr2Object(result, LoginBean::class.java)
        loginStatus.postValue(bean)
        _loginResult.value = LoginResult(
            success = LoggedInUserView(displayName = bean?.result?.userid.toString())
        )
        RxManagerUtil.getInstance().load(utilManagerListener, TYPE_REFRESH_TOKEN)
    }

    private fun smsLogin(userName: String, smsCode: String, vk: String): NetworkBean<String> {
        val param = JSONObject();
        param.put("mobile", userName)
        param.put("captcha", smsCode)
        param.put("vk", vk)
        return NetWortUtil.post("/login", param.toString())
    }

    private fun getSms(phone: String): NetworkBean<String> {
        if (isPhoneValid(phone)) {
            val param = JSONObject()
            param.put("mobile", phone)
            return NetWortUtil.post("/sms", param.toString())
        }
        return NetworkBean(-1, "手机号无效！")
    }

    private fun getSmsSuccess(result: String) {
        val smsBean = JsonUtil.jsonStr2Object(result, SmsBean::class.java)
        if (smsBean != null && smsBean.success) {
            smsStatus.value = smsBean
        } else {
            _loginForm.value = LoginFormState(usernameError = R.string.sms_get_fail)
        }
    }

    fun register(nick: String, name: String, mobile: String, password: String) {

    }

    fun loginDataChanged(username: String, password: String) {
        this.userName = username
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun loginDataChangeSms(phone: String, code: String) {
        this.userName = phone

        if (!isPhoneValid(phone)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_mobile)
        } else if (code.isEmpty()) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_code)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }

    }

    // A placeholder username validation check
    private fun isUserNameValid(username: String): Boolean {
        return if (username.contains("@")) {
            Patterns.EMAIL_ADDRESS.matcher(username).matches()
        } else {
            username.isNotBlank()
        }
    }

    // A placeholder password validation check
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    private fun isPhoneValid(phone: String): Boolean {
        return phone.length == 11 && phone.isDigitsOnly()
    }

}