package com.example.healthy.ui.main

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.recyclerview.widget.SnapHelper
import com.example.healthy.R
import com.example.healthy.bean.HistoryFile
import com.example.healthy.bean.NetworkBean
import com.example.healthy.bean.Status
import com.example.healthy.bean.UploadFileResult
import com.example.healthy.chart.MyLineChart
import com.example.healthy.data.BaseData
import com.example.healthy.databinding.FragmentDataBinding
import com.example.healthy.db.AbstractAppDataBase
import com.example.healthy.db.dao.HistoryFileDao
import com.example.healthy.utils.JsonUtil
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.SharedPreferenceUtil
import com.example.healthy.utils.TokenRefreshUtil
import com.example.healthy.utils.loge
import com.example.healthy.utils.singleClick
import com.google.android.material.carousel.CarouselLayoutManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext

class DataFragment : Fragment() {

    private lateinit var binding: FragmentDataBinding
    private val viewModel: DevicesViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initView()
    }

    private fun initData() {
        val sp = SharedPreferenceUtil.getSharedPreference(context)
        val xMax =
            sp?.getInt(SettingViewModel.X_MAX, MyLineChart.MAX_X_LENGTH) ?: MyLineChart.MAX_X_LENGTH
        binding.lineChart.initDataSet(
            "心电 #1",
            context?.let { getColor(it, R.color.md_theme_light_tertiary) } ?: Color.RED,
            xMax
        )

        binding.userId.text =
            resources.getString(R.string.user_id, TokenRefreshUtil.getUserId(context))

        viewModel.dataStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                DevicesViewModel.DATA_STATUS.PAUSE -> {
                    binding.controlDevice.setText(R.string.go_on_device)
                }

                DevicesViewModel.DATA_STATUS.DATA -> {
                    binding.controlDevice.setText(R.string.stop_device)
                }

                else -> {
                    binding.controlDevice.setText(R.string.to_link_device)
                }
            }
        }

        viewModel.resultValue.observe(viewLifecycleOwner) { data ->
            showData(data)
        }

        viewModel.uploadNetResult.observe(viewLifecycleOwner){result ->
            binding.networkStatus.text = result
        }

        adapter.initData(viewModel.fileDao)
    }

    private fun initView() {
        binding.apply {
            controlDevice.setOnClickListener {
                when (viewModel.dataStatus.value) {
                    DevicesViewModel.DATA_STATUS.NO_DEVICE -> {
                        findNavController().navigate(R.id.DevicesFragment)

                    }

                    DevicesViewModel.DATA_STATUS.DATA -> {
                        viewModel.dataPause(true)
                    }

                    DevicesViewModel.DATA_STATUS.PAUSE -> {
                        viewModel.dataPause(false)
                    }

                    else -> {}
                }
            }

            save.singleClick {
                if (viewModel.fileOutputStream == null) {
                    val filePath = viewModel.openFileOutStream(context)
                    if (filePath.isNullOrEmpty()) return@singleClick
                    saveTips.setText(R.string.saving)
                    val item = HistoryFile(filePath = filePath, status = Status.SAVING)
                    adapter.updateList.add(0, item)
                    viewModel.fileDao.insert(item)
                    adapter.notifyItemInserted(0)
                    fileList.scrollToPosition(0)
                    save.imageTintList =
                        ColorStateList.valueOf(getColor(save.context, R.color.colorAccent))
                } else {
                    saveTips.setText(R.string.saved)
                    saveTips.postDelayed({ saveTips.text = "" }, 5000)
                    viewModel.closeFileOutStream()
//                    adapter.updateList.last().let { item ->
//                        if (item.status == Status.SAVING) {
//                            item.status = Status.SAVED
//                            item.upload {
//                                viewModel.fileDao.update(item)
//                                save.post { adapter.notifyItemChanged(adapter.updateList.size - 1) }
//                            }
//                        }
//                    }
                    save.imageTintList = null
                }
            }

            fileList.layoutManager = LinearLayoutManager(context)
            fileList.adapter = adapter

            chatList.layoutManager = CarouselLayoutManager()
            chatList.adapter = LineAdapter()
        }
    }

    private fun showData(data: BaseData) {
        for (i in data.getData()[0]) {
            binding.lineChart.addEntry(i)
        }
    }

    private val adapter = HistoryAdapter()

    class HistoryAdapter() : Adapter<HistoryViewHolder>() {

        val updateList = ArrayList<HistoryFile>()
        private var fileDao: HistoryFileDao? = null

        fun initData(dao: HistoryFileDao?) {
            fileDao = dao
            updateList.clear()
            dao?.getAll()?.let { updateList.addAll(it) }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            return HistoryViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_upload_file, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return updateList.size
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val item = updateList[position]
            holder.id.text = item.id.toString()
            holder.filePath.text = item.filePath
            holder.result.text = item.analysedMsg

            holder.upload.singleClick {
                item.upload {
                    loge("item id = ${item.id} $item")
                    fileDao?.update(item)
                    holder.upload.post { notifyItemChanged(position) }
                }
            }
            holder.getResult.singleClick {
                holder.getResult.isEnabled = false
                holder.getResult.text = "正在分析"
                item.analyzeResult2 {
                    holder.getResult.post {
                        Toast.makeText(holder.getResult.context, it, Toast.LENGTH_LONG).show()
                        holder.getResult.isEnabled = true
                        holder.getResult.setText(R.string.re_get_result)
                        holder.getResult.post { notifyItemChanged(position) }
                    }
                    fileDao?.update(item)
                }
            }
            holder.delete.singleClick {
                fileDao?.delete(item)
                initData(fileDao)
                holder.delete.post { notifyDataSetChanged() }
            }
        }

    }

    class HistoryViewHolder(itemView: View) : ViewHolder(itemView) {
        var id: TextView
        var filePath: TextView
        var result: TextView
        var upload: Button
        var getResult: Button
        var delete: Button

        init {
            id = itemView.findViewById(R.id.remote_id)
            filePath = itemView.findViewById(R.id.file_patch)
            result = itemView.findViewById(R.id.result)

            upload = itemView.findViewById(R.id.upload)
            getResult = itemView.findViewById(R.id.get_result)
            delete = itemView.findViewById(R.id.delete)
        }
    }

    class LineAdapter : Adapter<LineViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineViewHolder {
            return LineViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_chat_layout, parent, false)
            )
        }

        override fun getItemCount(): Int {
            return 4
        }

        override fun onBindViewHolder(holder: LineViewHolder, position: Int) {
        }

    }

    class LineViewHolder(itemView: View) : ViewHolder(itemView) {

    }


}