package com.example.healthy.db.entry

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Users(
    @PrimaryKey val id: Int,
    val name: String?,
    val mobile: String?
)
