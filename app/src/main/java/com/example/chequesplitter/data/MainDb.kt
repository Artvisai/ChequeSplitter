package com.example.chequesplitter.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [/*Cheque::class, */Product::class],
    version = 1
)
abstract class MainDb : RoomDatabase() {
    abstract val dao: Dao
}