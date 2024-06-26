package com.example.chequesplitter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface Dao {
    @Insert
    suspend fun insertCheque(cheque: Cheque)

    @Update
    suspend fun updateCheque(cheque: Cheque)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Update
    suspend fun updateProduct(product: Product)
    @Insert
    suspend fun insertCustomer(customer: Customer)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("SELECT * FROM products WHERE idQR = :qr")
    fun getAllProductsByQr(qr: String): Flow<List<Product>>

    @Query("SELECT * FROM cheques")
    fun getAllCheques(): Flow<List<Cheque>>

    @Query("SELECT * FROM customers")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM cheques WHERE qrData = :qr")
    fun getChequeByQr(qr: String): Cheque?
    @Query("SELECT * FROM customers WHERE name = :n")
    fun getCustomerByName(n: String): Customer?
}