package com.example.chequesplitter.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

/*@Entity(tableName = "chequeswithproducts")
data class ChequeWithProducts(
    @Embedded val cheque: Cheque,
    @Relation(
        parentColumn = "chequeId",
        entityColumn = "productParentId"
    )
    val products: List<Product>
)*/
