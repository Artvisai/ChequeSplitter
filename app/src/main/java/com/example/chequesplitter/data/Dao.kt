package com.example.chequesplitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    suspend fun insertCheque(cheque: Cheque)

    @Update
    suspend fun updateCheque(cheque: Cheque)
    @Insert
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("SELECT * FROM products WHERE idQR = :qr")
    fun getAllProductsByQr(qr: String): Flow<List<Product>>

    @Query("SELECT * FROM cheques")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM cheques WHERE qrData = :qr")
    fun getChequeByQr(qr: String): Cheque?
}