package com.example.chequesplitter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "products", foreignKeys = [ForeignKey(
    entity = Cheque::class,
    parentColumns = arrayOf("chequeId"),
    childColumns = arrayOf("productParentId"),
    onDelete = ForeignKey.CASCADE
)])
data class Product(
    @PrimaryKey(autoGenerate = true)
    val productId: Int? = null,
    val name: String,
    val count: Int,
    val price: Int,
    val idQR: String,
    @ColumnInfo(index = true)
    val productParentId: Int
)
