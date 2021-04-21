package com.example.healthy.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.healthy.R
import com.example.healthy.utils.SharedPreferenceUtil
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment() {

    private val viewModel by lazy { SettingViewModel(context?.applicationContext) }

    private val TAG = "SettingFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    private val editor by lazy { SharedPreferenceUtil.getEditor(context) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.serverUrl.observe(viewLifecycleOwner, Observer { it ->
            service_url.setText(it)
        })

        viewModel.limitType.observe(viewLifecycleOwner, Observer { it ->
            if (it == 0){
                limit_min?.visibility = View.GONE
                limit_max?.visibility = View.GONE
            }else{
                limit_min?.visibility = View.VISIBLE
                limit_max?.visibility = View.VISIBLE
            }
            if(limit_type.selectedItemPosition != it){
                limit_type.setSelection(it)
            }
        })

        viewModel.limitMax.observe(viewLifecycleOwner, Observer { it ->
            limit_max?.setText(it.toString())
        })

        viewModel.limitMin.observe(viewLifecycleOwner, Observer { it ->
            limit_min?.setText(it.toString())
        })

        limit_type?.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
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

        limit_max?.addTextChangedListener(afterTextChangedListener)
        limit_min?.addTextChangedListener(afterTextChangedListener)
        service_url?.addTextChangedListener(afterTextChangedListener)
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
            if(limit_max.text.toString().isNotEmpty()){
                max = limit_max.text.toString().toInt()
            }
            if(limit_min.text.toString().isNotEmpty()){
                min = limit_min.text.toString().toInt()
            }
            editor?.putInt(SettingViewModel.LIMIT_MAX, max)
            editor?.putInt(SettingViewModel.LIMIT_MIN, min)

            editor?.putString(SettingViewModel.SERVER_URL, service_url.text.toString())
            editor?.apply()
        }
    }
}