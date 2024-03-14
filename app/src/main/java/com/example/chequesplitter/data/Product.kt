package com.example.chequesplitter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products"/*, foreignKeys = [ForeignKey(
    entity = Cheque::class,
    parentColumns = arrayOf("id"),
    childColumns = arrayOf("artist"),
    onDelete = ForeignKey.CASCADE
)]*/)
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String,
    val count: Int,
    val price: Int,
    val idQR: String
)
