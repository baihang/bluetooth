package com.example.healthy.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.healthy.R
import com.example.healthy.chart.MyLineChart
import com.example.healthy.databinding.FragmentSettingBinding
import com.example.healthy.utils.SharedPreferenceUtil

class SettingFragment : Fragment() {

    private val viewModel by lazy { SettingViewModel(context?.applicationContext) }

    private val TAG = "SettingFragment"

    private var binding:FragmentSettingBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_setting, container, false);
        binding = FragmentSettingBinding.bind(view)
        return view
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private val editor by lazy { SharedPreferenceUtil.getEditor(context) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.serverUrl.observe(viewLifecycleOwner, Observer { it ->
            binding?.serviceUrl?.setText(it)
        })

        viewModel.limitType.observe(viewLifecycleOwner, Observer { it ->
            if (it == 0){
                binding?.limitMin?.visibility = View.GONE
                binding?.limitMax?.visibility = View.GONE
            }else{
                binding?.limitMin?.visibility = View.VISIBLE
                binding?.limitMax?.visibility = View.VISIBLE
            }
            if(binding?.limitType?.selectedItemPosition != it){
                binding?.limitType?.setSelection(it)
            }
        })

        viewModel.limitMax.observe(viewLifecycleOwner, Observer { it ->
            binding?.limitMax?.setText(it.toString())
        })

        viewModel.limitMin.observe(viewLifecycleOwner, Observer { it ->
            binding?.limitMin?.setText(it.toString())
        })

        viewModel.xMax.observe(viewLifecycleOwner, Observer {
            binding?.xMax?.setText(it.toString())
        })

        binding?.limitType?.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                Log.e(TAG, "position = $position id = $id")
                if(viewModel.limitType.value != position){
                    viewModel.limitType.value = position
                    editor?.putInt(SettingViewModel.LIMIT_TYPE, position)
                    editor?.apply()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

        }

        binding?.xMax?.addTextChangedListener {
            if(it.toString().isEmpty()){
                return@addTextChangedListener
            }
            viewModel.setxMax(context, it?.toString()?.toInt() ?: MyLineChart.MAX_X_LENGTH)
        }

        binding?.limitMax?.addTextChangedListener(afterTextChangedListener)
        binding?.limitMin?.addTextChangedListener(afterTextChangedListener)
        binding?.serviceUrl?.addTextChangedListener(afterTextChangedListener)

        binding?.loginOut?.setOnClickListener {
            viewModel.loginOut(context)
        }

        binding?.visitor?.setOnClickListener {
            viewModel.visitor(context)
        }
    }

    private val afterTextChangedListener = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // ignore
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            // ignore
        }

        override fun afterTextChanged(s: Editable) {
            val editor = SharedPreferenceUtil.getEditor(context)
            var max = -1
            var min = -1
            if(binding?.limitMax?.text.toString().isNotEmpty()){
                max = binding?.limitMax?.text.toString().toInt()
            }
            if(binding?.limitMin?.text.toString().isNotEmpty()){
                min = binding?.limitMin?.text.toString().toInt()
            }
            editor?.putInt(SettingViewModel.LIMIT_MAX, max)
            editor?.putInt(SettingViewModel.LIMIT_MIN, min)

            editor?.putString(SettingViewModel.SERVER_URL, binding?.serviceUrl?.text.toString())
            editor?.apply()
        }
    }
}