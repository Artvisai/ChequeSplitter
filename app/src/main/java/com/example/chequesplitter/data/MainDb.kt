package com.example.chequesplitter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Cheque::class, Product::class/*, ChequeWithProducts::class*/],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MainDb : RoomDatabase() {
    abstract val dao: Dao
}