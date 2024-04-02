package com.example.chequesplitter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "cheques")
data class Cheque(
@PrimaryKey(autoGenerate = true)
val chequeId: Int? = null,
val storeName: String,
val qrData: String,
val date: LocalDateTime? = null
)