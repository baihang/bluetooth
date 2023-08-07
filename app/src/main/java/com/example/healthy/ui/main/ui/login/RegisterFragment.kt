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
import com.example.healthy.databinding.FragmentSignUpBinding

class RegisterFragment : Fragment() {

    private val viewModel by lazy {
        ViewModelProvider(this, LoginViewModelFactory())
            .get(LoginViewModel::class.java)
    }
    private var binding:FragmentSignUpBinding? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)
        binding = FragmentSignUpBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.signUp?.setOnClickListener {
            binding?.loading?.visibility = View.VISIBLE
            viewModel.register(
                binding?.nick?.text.toString(),
                binding?.realName?.text.toString(),
                binding?.mobile?.text.toString(),
                binding?.password?.text.toString()
            )
        }

        viewModel.registerResult.observe(viewLifecycleOwner,
            Observer<LoginResult> {

            })

        binding?.toLogin?.setOnClickListener {
            findNavController().navigate(R.id.LoginFragment)
        }
    }

}