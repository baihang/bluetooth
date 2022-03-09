package com.example.healthy.ui.main

import android.content.*
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.example.healthy.R
import com.example.healthy.chart.MyLineChart
import com.example.healthy.data.BaseData
import com.example.healthy.data.HeartSixData
import com.example.healthy.data.HeartThreeData
import com.example.healthy.databinding.MainFragmentBinding
import com.example.healthy.utils.*
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream
import java.lang.ref.PhantomReference

class MainFragment() : Fragment() {

    companion object {
        private val colors =
            arrayOf(Color.RED, Color.BLUE, Color.RED, Color.BLUE, Color.RED, Color.BLUE)
        private const val TAG = "MainFragment"
    }


    private val viewModel: DevicesViewModel by activityViewModels()
    private var binding: MainFragmentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initLineChart()

        binding?.mainSetting?.setOnClickListener {
//            anima()
            //测试跳转 Hook
//            ActivityHook.replaceInstrumentation(activity)
//            val intent = Intent(activity, ScrollingActivity::class.java);
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            context?.applicationContext?.startActivity(intent)

            val intent = Intent(activity, TestActivityA::class.java)
            startActivity(intent)
            return@setOnClickListener

            //测试 Manager
//            RxManagerUtil.getInstance().load(loadListener, 1)
            val heart = HeartSixData()
            val k = (Math.random() * 10).toInt()
            for (i in heart.bodyData.indices) {
//                heart.bodyData[i] = (Math.random() * 10).toInt()
                heart.bodyData[i] = i
            }
            viewModel.resultValue.postValue(heart)
        }

        binding?.mainSetting?.setOnLongClickListener {
            findNavController().navigate(R.id.SettingFragment)
            true
        }

        binding?.mainBluetooth?.setOnClickListener {
            findNavController().navigate(R.id.action_MainFragment_to_DevicesFragment)
        }

        binding?.mainBluetooth?.setOnLongClickListener {
            findNavController().navigate(R.id.SevenFragment)
            true
        }

        binding?.mainSaveTimeTv?.setOnClickListener {
            val patch = viewModel.filePath ?: "/storage/emulated/0/Android/data/com.example.healthy/files"
            val file = File(patch)
            if(!file.exists()){
                return@setOnClickListener
            }
            val uri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "*/*")
            Log.e(TAG, "to file patch $patch")
            try {
                startActivity(intent)
            }catch (e : ActivityNotFoundException){
                Snackbar.make(binding!!.mainSaveTimeTv, "打开文件夹失败", Snackbar.LENGTH_LONG).show()
            }
        }

        binding?.mainSaveTimeTv?.setOnLongClickListener {
            Snackbar.make(binding!!.mainSaveTimeTv, viewModel.filePath ?: "", Snackbar.LENGTH_LONG)
                .setAction("复制", View.OnClickListener {
                    //复制到剪切板
                    val clipboard =
                        context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val data =
                        ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, viewModel.filePath)
                    clipboard.setPrimaryClip(data)
                })
                .show()
            return@setOnLongClickListener true
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            addValue(data)
        }

        viewModel.timeStampLive.observe(viewLifecycleOwner, Observer {
            val data = 10000 / it
            binding?.mainFlow?.setText("流量： $data p/s")
        })

    }


    private fun initLineChart() {
        val sp = SharedPreferenceUtil.getSharedPreference(context)
        val xMax =
            sp?.getInt(SettingViewModel.X_MAX, MyLineChart.MAX_X_LENGTH) ?: MyLineChart.MAX_X_LENGTH
        binding?.lineChart1?.initDataSet("心电 #1", colors[0], xMax)
        binding?.lineChart2?.initDataSet("心电 #2", colors[1], xMax)
        binding?.lineChart3?.initDataSet("心电 #3", colors[2], xMax)
        binding?.lineChart4?.initDataSet("心电 #4", colors[3], xMax)
        binding?.lineChart5?.initDataSet("心电 #5", colors[4], xMax)
        binding?.lineChart6?.initDataSet("心电 #6", colors[5], xMax)
    }

    private fun resetConfig() {
        val sp = SharedPreferenceUtil.getSharedPreference(context)
        val max = sp?.getInt(SettingViewModel.LIMIT_MAX, -1) ?: -1
        val min = sp?.getInt(SettingViewModel.LIMIT_MIN, -1) ?: -1
        val type = sp?.getInt(SettingViewModel.LIMIT_TYPE, 0) ?: 0
        binding?.lineChart1?.setDataSetLimit(max, min, type)
        binding?.lineChart2?.setDataSetLimit(max, min, type)
        binding?.lineChart3?.setDataSetLimit(max, min, type)
        binding?.lineChart4?.setDataSetLimit(max, min, type)
        binding?.lineChart5?.setDataSetLimit(max, min, type)
        binding?.lineChart6?.setDataSetLimit(max, min, type)
    }

    override fun onResume() {
        super.onResume()
        resetConfig()
    }

    override fun onDetach() {
        super.onDetach()
        binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }


    private fun addValue(data: BaseData) {
        val dataArray = data.getData()
        if (dataArray.size == 3) {
            if (binding?.lineChart2?.visibility == View.GONE) {
                binding?.lineChart2?.visibility = View.VISIBLE
                binding?.lineChart3?.visibility = View.VISIBLE
            }
            for (i in dataArray[0].indices) {
                binding?.lineChart1?.addEntry(dataArray[0][i])
                binding?.lineChart2?.addEntry(dataArray[1][i])
                binding?.lineChart3?.addEntry(dataArray[2][i])
            }
        } else if (dataArray.size == 6) {
            if (binding?.lineChart2?.visibility == View.GONE) {
                binding?.lineChart2?.visibility = View.VISIBLE
                binding?.lineChart3?.visibility = View.VISIBLE
                binding?.lineChart4?.visibility = View.VISIBLE
                binding?.lineChart5?.visibility = View.VISIBLE
                binding?.lineChart6?.visibility = View.VISIBLE
            }
            for (i in dataArray[0].indices) {
                binding?.lineChart1?.addEntry(dataArray[0][i])
                binding?.lineChart2?.addEntry(dataArray[1][i])
                binding?.lineChart3?.addEntry(dataArray[2][i])
                binding?.lineChart4?.addEntry(dataArray[3][i])
                binding?.lineChart5?.addEntry(dataArray[4][i])
                binding?.lineChart6?.addEntry(dataArray[5][i])
            }
        } else {
            if (binding?.lineChart2?.visibility == View.VISIBLE) {
                binding?.lineChart2?.visibility = View.GONE
                binding?.lineChart3?.visibility = View.GONE
            }

            for (i in dataArray[0].indices) {
                binding?.lineChart1?.addEntry(dataArray[0][i])
            }
        }
    }
}