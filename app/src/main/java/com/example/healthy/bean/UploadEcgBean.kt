package com.example.healthy.bean

data class UploadEcgBean(
    val channel: Int,
    val heartRate: Int,
    val result: String
)