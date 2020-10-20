package com.example.healthy.utils

import android.content.Context
import java.io.File
import java.lang.Exception
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 本地文件存储
 */
class LocalFileUtil {
    companion object {
        private fun getRootPath(context: Context): String? {
            return context.getExternalFilesDir(null)?.absolutePath
        }

        private fun getRootName(): String {
            return "Healthy"
        }

        fun getDateStr(): String {
            val date = Date(System.currentTimeMillis())
            val dateFormat = SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss")
            return dateFormat.format(date)
        }

        /**
         * 获取文件夹目录，创建失败时返回空
         */
        fun getDirectorPath(context: Context, name: String): String? {
            val root = getRootPath(context)
            if (root.isNullOrEmpty()) {
                return null
            }
            val path = "$root/${getRootName()}/$name"
            return if (createFileDirector(path)) {
                path
            } else {
                null
            }
        }

        /**
         * 创建文件夹
         */
        private fun createFileDirector(path: String): Boolean {
            val file = File(path)
            if (!file.exists()) {
                try {
                    file.mkdirs()
                } catch (e: Exception) {
                    e.printStackTrace()
                    return false
                }
            }
            return true
        }

        fun createFile(context: Context, path: String, name: String) {
            var dir = getDirectorPath(context, path)
            if(dir.isNullOrEmpty()){
                return
            }
            dir += "/$name"
            val file = File(dir)
            if(!file.exists()){
                file.createNewFile()
            }
        }

    }

}