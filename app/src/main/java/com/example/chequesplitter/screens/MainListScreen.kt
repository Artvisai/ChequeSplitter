package com.example.chequesplitter.screens

import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chequesplitter.CHEQUE_CALCULATE_SCREEN
import com.example.chequesplitter.CHEQUE_EDIT_SCREEN
import com.example.chequesplitter.CUSTOMER_ADD_SCREEN
import com.example.chequesplitter.data.Cheque
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.data.Product
import com.example.chequesplitter.ui.theme.Purple200
import com.example.chequesplitter.ui.theme.Purple40
import com.example.chequesplitter.ui.theme.PurpleGrey100
import com.journeyapps.barcodescanner.ScanOptions
import java.text.NumberFormat

@Composable
fun MainListScreen(mainDb : MainDb, scanLauncher : ActivityResultLauncher<ScanOptions>, chequeStateList : State<List<Cheque>>, navController: NavController, onClick: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
        ) {
            items(chequeStateList.value) { cheque ->
                ChequeItem(mainDb, cheque, navController)
            }
        }
        MainButtonsRow(navController, scanLauncher, onClick)
    }
}

@Composable
fun MainButtonsRow(navController: NavController, scanLauncher : ActivityResultLauncher<ScanOptions>, onClick: () -> Unit){
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ){
        Button(onClick = {
            scan(scanLauncher)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
        ) {
            Text(text = "Scan cheque")
        }
        Button(onClick = {
            navController.navigate(CUSTOMER_ADD_SCREEN)
        },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40))
        {
            Text(text = "Add people")
        }
    }
}


private fun scan(scanLauncher : ActivityResultLauncher<ScanOptions>) {
    val options = ScanOptions()
    options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
    options.setPrompt("Scan a QR-code")
    options.setCameraId(0) // Use a specific camera of the device
    options.setBeepEnabled(false)
    options.setBarcodeImageEnabled(true)
    scanLauncher.launch(options)
}

@Composable
fun ChequeItem(mainDb : MainDb, cheque: Cheque, navController: NavController){
    var isExpanded by remember {
        mutableStateOf(false)
    }
    val productStateList = mainDb.dao.getAllProductsByQr(cheque.qrData)
        .collectAsState(initial = emptyList())
    Card(colors = CardDefaults.cardColors(
        containerColor = Purple200,
    ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (isExpanded) 0.dp else 10.dp)
            .clickable {
                isExpanded = !isExpanded
            }
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            text = cheque.storeName + "\n" + cheque.date,
            textAlign = TextAlign.Center
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                navController.navigate("$CHEQUE_EDIT_SCREEN/${cheque.qrData}")
            },
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)
            ) {
                Text(text = "Edit")
            }
            Button(onClick = {
                navController.navigate("$CHEQUE_CALCULATE_SCREEN/${cheque.qrData}")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Purple40)
            ) {
            Text(text = "Calculate")
        }
        }
    }
    if (isExpanded)
        ProductsColumn(productStateList)
}

@Composable
fun ProductsColumn(productStateList: State<List<Product>>){
    Column(
        modifier = Modifier
            .padding(bottom = 10.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(PurpleGrey100),
    ){
        for ((i, items) in productStateList.value.withIndex()){
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(15.dp),
                text = (i+1).toString() + ". " + items.name +
                        "\n Summary: " +
                        NumberFormat.getCurrencyInstance().format(items.price.toFloat()/100) +
                        " * " + items.quantity + " = " +
                        NumberFormat.getCurrencyInstance().format(items.sum.toFloat()/100),
            )

        }
    }
}

