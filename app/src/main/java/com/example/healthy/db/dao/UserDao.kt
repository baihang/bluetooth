package com.example.healthy.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.healthy.db.entry.Users

@Dao
interface UserDao {

    @Query("SELECT * FROM users")
    fun getAll(): List<Users>

    @Query("SELECT * FROM users WHERE id IN (:id)")
    fun loadByIds(id: Int) : Users

    @Query("SELECT * FROM users WHERE mobile LIKE :mobile")
    fun loadByMobile(mobile: String) : Users

    @Delete
    fun delete(users: Users)

    @Insert
    fun insert(users: Users)
}