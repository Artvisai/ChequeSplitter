package com.example.chequesplitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    suspend fun insertCheque(cheque: Cheque)

    @Update
    suspend fun updateCheque(cheque: Cheque)

    @Query("SELECT * FROM cheques")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE qrData = :qr")
    fun getChequeByQr(qr: String): Cheque?
}