package com.example.healthy.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.healthy.db.dao.UserDao
import com.example.healthy.db.entry.Users
import okio.Okio

@Database(entities = arrayOf(Users::class), version = 1, exportSchema = false)
abstract class AbstractAppDataBase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {

        private var instance: AbstractAppDataBase? = null

        fun getInstance(context: Context): AbstractAppDataBase {
            return instance ?: synchronized(this) {
                instance ?: getDatabase(context).also { instance = it }
            }
        }

        private fun getDatabase(application: Context): AbstractAppDataBase {
            return Room.databaseBuilder(
                application,
                AbstractAppDataBase::class.java,
                "healthy_db"
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}