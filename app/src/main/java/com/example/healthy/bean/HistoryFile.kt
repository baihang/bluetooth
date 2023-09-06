package com.example.healthy.bean

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.healthy.utils.JsonUtil
import com.example.healthy.utils.NetWortUtil
import com.example.healthy.utils.TokenRefreshUtil
import com.example.healthy.utils.loge
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@Entity(tableName = "history_file")
data class HistoryFile(
    @PrimaryKey(autoGenerate = true) var dbId: Int = 0,
    var id: Int = 0,
    var data: Long = 0,//上传时间
    var type: Int = 0,
    var status: Status = Status.NONE,
    var filePath: String = "",
    var analysedMsg: String = "",
    var create_time: Long = System.currentTimeMillis(),
    var update_time: Long = System.currentTimeMillis(),
    var deleted_time: Long? = null,
) {


    fun upload(callback: ((Boolean) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(filePath)
            if (!file.exists()) {
                loge("uploadEcg error, file not exit")
                return@launch
            }
            status = Status.UPLOADING
            val map = HashMap<String, Any>()
            map["token"] = TokenRefreshUtil.getInstance().token
            map["lead"] = 1
            map["base"] = 1
            map["file"] = file
            val result = NetWortUtil.postMulti("/ecg/uploadEcg", map)
            if (result.isSucceed) {
                val uploadFileResult =
                    JsonUtil.jsonStr2Object(result.data, UploadFileResult::class.java)
                loge("uploadFileResult = ${result.data}")
                if (uploadFileResult.state == 0) {
                    status = Status.UPLOAD_SUCCESS
                    id = uploadFileResult.id
                    analyzeResult()
                    callback?.invoke(true)
                    return@launch
                }
            }
            callback?.invoke(false)
        }
    }

    fun analyzeResult(end: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            if (id <= 0) {
                loge("analyzeResult return by id = $id")
                return@launch
            }
            val map = HashMap<String, String>()
            map["token"] = TokenRefreshUtil.getInstance().token
            map["id"] = id.toString()
            val result = NetWortUtil.post("/ecg/getEcgRealResult", map)
            loge("analyzeResult = $result")
            analysedMsg = result.data
            if (result.isSucceed) {
                val analyzeResult =
                    JsonUtil.jsonStr2Object(result.data, HeartAnalyzeResult::class.java)
                end?.invoke()
                if (analyzeResult.state == 0) {

                }
            }
        }

    }
}

enum class Status {
    NONE,
    SAVING,
    SAVED,
    UPLOADING,
    UPLOAD_SUCCESS,
    UPLOAD_ERROR,
}

data class HeartAnalyzeResult(
    val analysedMsg: String,
    val heartRate: Int,
    val id: Int,
    val msg: String,
    val state: Int
)