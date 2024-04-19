package com.example.chequesplitter.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "customers")
data class Customer(
@PrimaryKey
val name: String
)