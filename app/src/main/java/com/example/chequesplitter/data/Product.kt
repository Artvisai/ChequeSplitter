package com.example.chequesplitter.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(tableName = "products", foreignKeys = [ForeignKey(
    entity = Cheque::class,
    parentColumns = arrayOf("qrData"),
    childColumns = arrayOf("idQR"),
    onDelete = ForeignKey.CASCADE
)])
data class Product(
    @PrimaryKey(autoGenerate = true)
    val productId: Int? = null,
    val name: String,
    val quantity: Float,
    val price: Int,
    val sum: Int,
    val customersString: String = "",
    @ColumnInfo(index = true)
    val idQR: String,
)
