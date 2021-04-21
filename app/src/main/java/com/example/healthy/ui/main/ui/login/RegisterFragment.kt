package com.example.healthy.ui.main.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import kotlinx.android.synthetic.main.fragment_sign_up.*

class RegisterFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sign_up?.setOnClickListener {
            loading.visibility = View.VISIBLE
            viewModel.register(
                nick.text.toString(),
                real_name.text.toString(),
                mobile.text.toString(),
                password.text.toString()
            )
        }

        viewModel.registerResult.observe(viewLifecycleOwner,
            Observer<LoginResult> {

            })

        to_login?.setOnClickListener {
            findNavController().navigate(R.id.LoginFragment)
        }
    }

}