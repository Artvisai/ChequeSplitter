package com.example.chequesplitter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.chequesplitter.MAIN_LIST_SCREEN
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.ui.theme.Purple40
import com.example.chequesplitter.ui.theme.PurpleGrey100
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat


@Composable
fun ChequeCalculateScreen(mainDb: MainDb, navController: NavHostController, qrData: String?, onClick: () -> Unit) {
    val productStateList = mainDb.dao.getAllProductsByQr(qrData ?: "")
        .collectAsState(initial = emptyList())
    val chequeCustomerStateList = remember {
        mutableStateListOf<String>()
    }
    val sumStateList = remember {
        mutableStateListOf<Float>()
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ){
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
        ) {
            itemsIndexed(chequeCustomerStateList) { i, item ->
                Column(modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleGrey100)){
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        text = item + ": " + NumberFormat.getCurrencyInstance().format(sumStateList[i]),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        CustomerCalculateButtons(navController, onClickCalculate = {
            for (prod in productStateList.value){
                var arrayState: JSONArray
                if (prod.customersString != ""){
                    arrayState = JSONObject(prod.customersString).getJSONArray("customers")
                    for (customer in 0 until  arrayState.length()){
                        if (arrayState[customer] !in chequeCustomerStateList){
                            chequeCustomerStateList.add(arrayState[customer].toString())
                        }
                    }
                }
            }
            for (i in chequeCustomerStateList){
                sumStateList.add(0f)
            }
            for ((i, item) in chequeCustomerStateList.withIndex()){
                var sumState = 0f
                for (prod in productStateList.value){
                    var arrayState: JSONArray
                    if (prod.customersString != ""){
                        arrayState = JSONObject(prod.customersString).getJSONArray("customers")
                        for (customer in 0 until  arrayState.length()){
                            if (arrayState[customer] == item){
                                sumState += prod.sum.toFloat()/100/arrayState.length()
                            }
                        }
                    }
                }
                sumStateList[i] = sumState
            }
        })
    }
}

@Composable
fun CustomerCalculateButtons(navController: NavController, onClickCalculate: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(15.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ){
        Button(onClick = {
            navController.navigate(MAIN_LIST_SCREEN)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(text = "Back")
        }
        Button(onClick = {
            onClickCalculate()
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(text = "Calculate")
        }
    }
}
