package com.example.healthy.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.healthy.bean.HistoryFile

@Dao
interface HistoryFileDao {

    @Query("SELECT * FROM history_file where deleted_time is NULL ORDER by create_time DESC")
    fun getAll(): List<HistoryFile>

    @Insert
    fun insert(historyFile: HistoryFile)

    @Update
    fun update(historyFile: HistoryFile)

    @Delete
    fun delete(historyFile: HistoryFile)

    fun softDelete(historyFile: HistoryFile){
        historyFile.deleted_time = System.currentTimeMillis()
        update(historyFile)
    }

}