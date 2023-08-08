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
import com.example.healthy.data.TemperatureData
import com.example.healthy.databinding.MainFragmentBinding
import com.example.healthy.utils.LocalFileUtil
import com.example.healthy.utils.NoticePopWindow
import com.example.healthy.utils.SharedPreferenceUtil
import com.example.healthy.utils.ThreadUtil
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.FileOutputStream

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

            //测试 Manager
//            RxManagerUtil.getInstance().load(loadListener, 1)
            val heart = TemperatureData()
            val k = (Math.random() * 10).toInt()
            for (i in heart.bodyData.indices) {
                heart.bodyData[i] = (Math.random() * 10).toInt()
//                heart.bodyData[i] = i + 1
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
            val patch = filePath ?: "/storage/emulated/0/Android/data/com.example.healthy/files"
            val file = File(patch)
            if (!file.exists()) {
                return@setOnClickListener
            }
            val uri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().applicationContext.packageName + ".provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.setDataAndType(uri, "*/*")
            Log.e(TAG, "to file patch $patch")
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Snackbar.make(binding!!.mainSaveBt, "打开文件夹失败", Snackbar.LENGTH_LONG).show()
            }
        }

        binding?.mainSaveBt?.setOnClickListener {
            if (timeInterval == -1) {
//                开始保存
                timeInterval = 0
                binding?.mainSaveBt?.setText("结束保存")
                ThreadUtil.getInstance()?.addTimeListener(timeListener)
            } else {
//                保存结束
                binding?.mainSaveBt?.setText("开始保存")
                timeInterval = -1
                ThreadUtil.getInstance()?.removeTimeListener(timeListener)
                saveToFile()
                outPutStream?.close()
                outPutStream = null
                stringBuilder.clear()

                Snackbar.make(binding!!.mainSaveBt, filePath ?: "", Snackbar.LENGTH_LONG)
                    .setAction("复制", View.OnClickListener {
                        //复制到剪切板
                        val clipboard =
                            context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val data =
                            ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, filePath)
                        clipboard.setPrimaryClip(data)
                    })
                    .show()
            }
//            binding.mainSaveTimeTv.text = LocalFileUtil.getDateStr()
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            if (data is TemperatureData) {
                binding?.mainFlow?.setText("温度： ${((data.bodyData[0] + data.bodyData[1] * 256) * 100 / 16)  / 100f} ℃")
            } else
                addValue(data)
        }

//        viewModel.timeStampLive.observe(viewLifecycleOwner, Observer {
//            val data = 10000 / it
//            binding?.mainFlow?.setText("流量： $data p/s")
//        })

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

    /**
     * 10s保存一次
     */
    private var timeInterval = -1

    private val timeListener = object : ThreadUtil.TimeListener {
        override fun onClock() {
            timeInterval++
            if (timeInterval >= 10) {
                timeInterval = 0
                saveToFile()
            }
        }
    }

    private val stringBuilder = StringBuilder()
    private var filePath: String? = null
    private var outPutStream: FileOutputStream? = null
    private fun saveToFile() {
        Log.e(TAG, "save to file")
        var value = ""
        synchronized(stringBuilder) {
            value = stringBuilder.toString()
            stringBuilder.clear()
        }
//        val result = "${LocalFileUtil.getDateStr()}\n$value\n"
        val result = "$value\n"
        if (outPutStream == null) {
            val file =
                LocalFileUtil.createFile(context, "heart", "${LocalFileUtil.getDateStr()}.txt")
            filePath = file?.absolutePath
            Log.e(TAG, "open file = ${file?.absolutePath}")
            outPutStream = FileOutputStream(file)
        }
        outPutStream?.write(result.toByteArray())
//        outPutStream?.flush()
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
        if (outPutStream != null) {
            outPutStream?.close()
        }
        ThreadUtil.getInstance()?.removeTimeListener(timeListener)
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

                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                    stringBuilder.append(dataArray[1][i]).append(" ")
                    stringBuilder.append(dataArray[2][i]).append(" ")
                }
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
                if (i != 0 && dataArray[0][i] == dataArray[0][i - 1]) {
                    continue
                }
                binding?.lineChart1?.addEntry(dataArray[0][i])
                binding?.lineChart2?.addEntry(dataArray[1][i])
                binding?.lineChart3?.addEntry(dataArray[2][i])
                binding?.lineChart4?.addEntry(dataArray[3][i])
                binding?.lineChart5?.addEntry(dataArray[4][i])
                binding?.lineChart6?.addEntry(dataArray[5][i])
                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                    stringBuilder.append(dataArray[1][i]).append(" ")
                    stringBuilder.append(dataArray[2][i]).append(" ")
                    stringBuilder.append(dataArray[3][i]).append(" ")
                    stringBuilder.append(dataArray[4][i]).append(" ")
                    stringBuilder.append(dataArray[5][i]).append(" ")
                }
            }
        } else if(dataArray.size == 2){
            if (binding?.lineChart3?.visibility == View.VISIBLE) {
                binding?.lineChart3?.visibility = View.GONE
            }

            for (i in dataArray[0].indices) {
                binding?.lineChart1?.addEntry(dataArray[0][i])
                binding?.lineChart2?.addEntry(dataArray[1][i])
                binding?.lineChart2?.visibility = View.VISIBLE
                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                    stringBuilder.append(dataArray[1][i]).append(" ")
                }
            }
        }
        else {
            if (binding?.lineChart2?.visibility == View.VISIBLE) {
                binding?.lineChart2?.visibility = View.GONE
                binding?.lineChart3?.visibility = View.GONE
            }

            for (i in dataArray[0].indices) {
                binding?.lineChart1?.addEntry(dataArray[0][i])
                if (timeInterval != -1) {
                    stringBuilder.append(dataArray[0][i]).append(" ")
                }
            }
        }
    }
}