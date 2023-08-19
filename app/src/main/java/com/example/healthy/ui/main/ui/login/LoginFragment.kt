package com.example.healthy.ui.main.ui.login

import android.app.PendingIntent
import android.content.*
import android.net.Uri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.ui.main.SettingViewModel
import com.example.healthy.utils.*
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LoginFragment : Fragment() {

    private val TAG = "LoginFragment"
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel = ViewModelProvider(requireActivity().viewModelStore, LoginViewModelFactory())
            .get(LoginViewModel::class.java)

//        val breakpadTest = BreakpadTest()
//        val it = breakpadTest.testMethod()
//        Log.e(TAG, "break test return = $it")

        val usernameEditText = view.findViewById<EditText>(R.id.username)
        val passwordEditText = view.findViewById<EditText>(R.id.password)
        val loginButton = view.findViewById<Button>(R.id.login)
        val loadingProgressBar = view.findViewById<ProgressBar>(R.id.loading)
        val signUp = view.findViewById<Button>(R.id.sign_up)

//        view.setOnTouchListener(object :View.OnTouchListener{
//            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
//                Log.e(TAG, "touch event = " + event?.action + " x = " + event?.rawX)
//                if(event?.action!! >= 260){
//                    Log.e(TAG, "event = " + event?.toString())
//                }
//                if(event?.action == MotionEvent.ACTION_DOWN){
//                    return true
//                }
//                return false;
//            }
//        })


        loginViewModel.loginFormState.observe(viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                loginButton.isEnabled = loginFormState.isDataValid
                loginButton.text = getString(R.string.action_sign_in)
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        loginViewModel.loginResult.observe(viewLifecycleOwner,
            Observer { loginResult ->
                loadingProgressBar.visibility = View.GONE
                loginResult ?: return@Observer
                loginResult.error?.let {
                    showLoginFailed(it)
                }
                loginResult.success?.let {
                    updateUiWithUser(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
                loginViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
        }
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString()
                )
            }
            false
        }

        loginButton.setOnClickListener {
            loadingProgressBar.visibility = View.VISIBLE
            loginViewModel.login(
                usernameEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }

        signUp.setOnClickListener {
//            findNavController().navigate(R.id.RegisterFragment)
            val intent = Intent(context, TestMessengerService::class.java)
            context?.startService(intent)

//            socketUtil.connectSocket()
        }

        getView()?.findViewById<Button>(R.id.to_sms)?.setOnClickListener {
            findNavController().navigate(R.id.LoginBySmsFragment)
        }

        var socketUtil: TestSocketUtil? = null

        getView()?.findViewById<FloatingActionButton>(R.id.visitor)?.setOnClickListener {
//            testBinderService(context)
//            testMessenger(context)
            loginViewModel.login(loginViewModel.VISITOR, "")

            ///////socket 测试
//            if(socketUtil?.socketStatus == true){
//                socketUtil?.writeStr()
//            }else{
//                socketUtil = TestSocketUtil()
//            }
            //////socket 测试结束

//            var loader: ClassLoader? = this.javaClass.classLoader
//            while (loader != null){
//               Log.e(TAG, "loader = ${loader.javaClass.name}")
//                loader = loader.parent
//            }
//            CustomClassLoader(context?.applicationContext).findClass("")

//            val intent = Intent(activity, TestActivityA::class.java)
//            val intent = Intent(activity, TestImageLoad::class.java)
//            startActivity(intent)

//            val test = TestThread()
//            test.testThreadMax()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        activity?.registerReceiver(broadCastReceiver, IntentFilter("com.healthy.testService"))
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        activity?.unregisterReceiver(broadCastReceiver)
        super.onDestroy()
    }

    private val broadCastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.e(TAG, "broadCastReceiver" + intent?.extras)
        }

    }

    private val connect = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.e(TAG, "service connected $name")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.e(TAG, "service disconnected")
        }

    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome) + model.displayName
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()

        val editor = SharedPreferenceUtil.getEditor(appContext)
        editor?.putBoolean(SettingViewModel.LOGIN_STATUS, true)
        editor?.apply()

        findNavController().navigate(R.id.MainFragment)
    }

    private fun showLoginFailed(errorString: String) {
        Log.e(TAG, "showLoginFailed $errorString")
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }
}