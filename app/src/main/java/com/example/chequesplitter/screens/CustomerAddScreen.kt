package com.example.chequesplitter.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chequesplitter.MAIN_LIST_SCREEN
import com.example.chequesplitter.data.Customer
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.ui.theme.Purple40
import com.example.chequesplitter.ui.theme.PurpleGrey100
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CustomerAddScreen(context: Context, mainDb : MainDb, navController: NavController, customerStateList: State<List<Customer>>, onClick: () -> Unit){
    val textState = remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally){
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.7f),
        ) {
            items(customerStateList.value) { customer ->
                Column(modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(PurpleGrey100)){
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(15.dp),
                        text = customer.name,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        TextField(value = textState.value, onValueChange = {
            textState.value = it
        },
            label = {
                Text(text = "Name")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        )
        CustomerAddButtons(context, mainDb, navController, textState)
    }
}

@Composable
fun CustomerAddButtons(context: Context, mainDb : MainDb, navController: NavController, textState: MutableState<String>) {
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
            addCustomer(context, mainDb, textState)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
        {
            Text(text = "Save")
        }
    }
}


private fun addCustomer(context: Context, mainDb : MainDb, textState: MutableState<String>) {
    if (textState.value == "") {
        Toast.makeText(
            context,
            "Text data is null!",
            Toast.LENGTH_SHORT
        ).show()
    } else {
        CoroutineScope(Dispatchers.IO).launch {
            val customerByName = mainDb.dao.getCustomerByName(textState.value)
            if (customerByName == null){
                mainDb.dao.insertCustomer(
                    Customer(
                        textState.value
                    )
                )
            }
        }

    }
}
