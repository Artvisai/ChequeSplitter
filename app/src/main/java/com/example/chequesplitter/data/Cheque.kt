package com.example.chequesplitter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "cheques")
data class Cheque(
@PrimaryKey
val qrData: String,
val storeName: String,
val date: LocalDateTime? = null
)