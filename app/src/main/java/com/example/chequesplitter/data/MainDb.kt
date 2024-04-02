package com.example.chequesplitter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Cheque::class, Product::class/*, ChequeWithProducts::class*/],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MainDb : RoomDatabase() {
    abstract val dao: Dao
    companion object {
        @Volatile
        private var INSTANCE: MainDb? = null

        fun getInstance(context: Context): MainDb {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        MainDb::class.java, "main_db")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}