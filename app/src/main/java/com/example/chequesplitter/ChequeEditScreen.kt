package com.example.chequesplitter

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chequesplitter.data.Customer
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.data.Product
import com.example.chequesplitter.ui.theme.Purple200
import com.example.chequesplitter.ui.theme.Purple40
import com.example.chequesplitter.ui.theme.PurpleGrey100
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat

@Composable
fun ChequeEditScreen(mainDb: MainDb, navController: NavController, qrData: String?, onClick: () -> Unit){
    val productStateList = mainDb.dao.getAllProductsByQr(qrData ?: "")
        .collectAsState(initial = emptyList())
    val customerStateList = mainDb.dao.getAllCustomers()
        .collectAsState(initial = emptyList())
    val dialogChooseCustomerState = remember {
        mutableStateOf(false)
    }
    val isInArrayState = remember {
        mutableStateOf(false)
    }
    val chosenUserTextState = remember { mutableStateOf("") }
    if (dialogChooseCustomerState.value){
        DialogChooseCustomer(dialogChooseCustomerState, customerStateList, onClickCustomer = {
            chosenUserTextState.value = it
        })
    }
    val productUserStringList = remember {
        mutableStateListOf<String>()
    }
    val arrayStateList = remember {
        mutableStateListOf<JSONArray>()
    }
    val objStateList = remember {
        mutableStateListOf<JSONObject>()
    }
    for (i in productStateList.value){
        productUserStringList.add("")
        arrayStateList.add(JSONArray())
        objStateList.add(JSONObject())
    }
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.8f)/*
                    .verticalScroll(rememberScrollState())*/,
        ) {
            itemsIndexed(productStateList.value) {i, items ->
                val arrayState = remember {
                    mutableStateOf(JSONArray())
                }
                val objState = remember {
                    mutableStateOf(JSONObject())
                }
                objStateList[i] = JSONObject().put("customers", arrayStateList[i])
                Column(
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Purple200)
                )
                {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        text = (i + 1).toString() + ". " + items.name +
                                "\n Summary: " +
                                NumberFormat.getCurrencyInstance()
                                    .format(items.price.toFloat() / 100) +
                                " * " + items.quantity + " = " +
                                NumberFormat.getCurrencyInstance()
                                    .format(items.sum.toFloat() / 100),
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        text = "Customers: " + objStateList[i].getString("customers") + items.customersString
                    )
                    Row(modifier = Modifier
                        .padding(10.dp)
                        .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically){
                        Button(onClick = {
                            if (chosenUserTextState.value != ""){
                                for (t in 0 until arrayStateList[i].length()) {
                                    if (arrayStateList[i].getString(t) == chosenUserTextState.value) {
                                        arrayStateList[i].remove(t)
                                        break
                                    }
                                    Log.e("MyLog", "Remove: " + objStateList[i].toString())
                                }
                                Log.e("MyLog", "Remove: " + objStateList[i].toString())
                            }
                            objStateList[i] = JSONObject().put("customers", arrayStateList[i])
                            productUserStringList[i] = objStateList[i].toString()
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
                        {
                            Text(text = "Remove")
                        }
                        Button(onClick = {
                            isInArrayState.value = false
                            if (chosenUserTextState.value != ""){
                                for (t in 0 until arrayStateList[i].length()){
                                    isInArrayState.value = isInArrayState.value or (arrayStateList[i].getString(t) == chosenUserTextState.value)
                                    Log.e("MyLog", "Add: " + objStateList[i].toString())
                                }
                                if (!isInArrayState.value){
                                    arrayStateList[i].put(chosenUserTextState.value)
                                    Log.e("MyLog", "Add After: " + objStateList[i].toString())
                                }
                                Log.e("MyLog", "Add After: " + objStateList[i].toString())
                            }
                            objStateList[i] = JSONObject().put("customers", arrayStateList[i])
                            productUserStringList[i] = objStateList[i].toString()
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
                        {
                            Text(text = "Add")
                        }

                    }
                }
            }
        }
        Row(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PurpleGrey100),
            verticalAlignment = Alignment.CenterVertically){
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(15.dp),
                text = chosenUserTextState.value,
            )
            Button(onClick = {
                dialogChooseCustomerState.value = true
            },
                colors = ButtonDefaults.buttonColors(containerColor = Purple40))
            {
                Text(text = "Choose")
            }

        }
        EditButtonsRow(navController, productStateList, onClickEditCheque = {
            CoroutineScope(Dispatchers.IO).launch {
                for ((i, prod) in productStateList.value.withIndex()){
                    mainDb.dao.updateProduct(
                        Product(
                        prod.productId,
                        prod.name,
                        prod.quantity,
                        prod.price,
                        prod.sum,
                        productUserStringList[i],
                        prod.idQR
                    )
                    )
                    Log.e("MyLog", productUserStringList[i])
                }
            }
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogChooseCustomer(dialogState: MutableState<Boolean>, customerStateList: State<List<Customer>>, onClickCustomer: (String) -> Unit){
    val textState = remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = {
        dialogState.value = false
    }, content = {
        LazyColumn(modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .fillMaxHeight()
            .clip(RoundedCornerShape(10.dp))
            .background(PurpleGrey100)
        ){
            items(customerStateList.value){customer ->
                Column(modifier = Modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleGrey100)
                    .clickable {
                        textState.value = customer.name
                        onClickCustomer(textState.value)
                        dialogState.value = false
                    }){
                    Text(modifier = Modifier
                        .padding(15.dp)
                        .fillMaxWidth(),
                        text = customer.name,
                        fontSize = 25.sp
                    )
                }

            }
        }
    })
}

@Composable
fun EditButtonsRow(
    navController: NavController,
    productStateList: State<List<Product>>,
    onClickEditCheque: (State<List<Product>>) -> Unit
){
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Button(onClick = {
            navController.navigate(MAIN_LIST_SCREEN)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(text = "Back")
        }
        Button(onClick = {
            onClickEditCheque(productStateList)
            navController.navigate(MAIN_LIST_SCREEN)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
        {
            Text(text = "Ok")
        }
    }
}
