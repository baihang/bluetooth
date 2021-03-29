package com.example.healthy.ui.main.ui.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.example.healthy.R
import com.example.healthy.bean.AbstractLoadBean
import com.example.healthy.bean.NetworkBean
import com.example.healthy.ui.main.data.LoginRepository
import com.example.healthy.ui.main.data.Result
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.RxManagerUtil


class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {
    private final val TAG = "LoginViewModel"
    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState> = _loginForm

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(username: String, password: String) {
        // can be launched in a separate asynchronous job
//        val result = loginRepository.login(username, password)
//
//        if (result is Result.Success) {
//        _loginResult.value =
//            LoginResult(success = LoggedInUserView(displayName = result.data.displayName))
//        } else {
//            _loginResult.value = LoginResult(error = R.string.login_failed)
//        }

        RxManagerUtil.getInstance().load(object : RxManagerUtil.ManagerListener {
            override fun loadPre(tag: Int) {

            }

            override fun load(tag: Int): AbstractLoadBean<*> {
                val result = NetWortUtil.login(username, password)
                Log.e(TAG, "result = $result")
                return result
            }

            override fun loadSucceed(bean: AbstractLoadBean<*>?) {
                _loginResult.value = LoginResult(
                    success = LoggedInUserView(displayName = (bean as NetworkBean).err_msg)
                )
                Log.e(TAG, "load succeed")
            }

            override fun loadFailed(bean: AbstractLoadBean<*>) {
                _loginResult.value = LoginResult(
                    error = "error code = ${(bean as NetworkBean).err_code}"
                )
                Log.e(TAG, "load failed")
            }

        }, 1)
    }

    fun loginDataChanged(username: String, password: String) {
        if (!isUserNameValid(username)) {
            _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
        } else if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
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
}