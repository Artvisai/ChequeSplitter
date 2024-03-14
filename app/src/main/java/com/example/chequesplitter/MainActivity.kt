package com.example.chequesplitter

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.data.Product
import com.example.chequesplitter.ui.theme.ComposeLesson7Theme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var mainDb: MainDb
    var counter = 0

    private val scanLauncher = registerForActivityResult(
        ScanContract()
    ) { result ->
        if (result.contents == null) {
            Toast.makeText(
                this,
                "Scan data is null!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val productByQr = mainDb.dao.getProductByQr(result.contents)
                if (productByQr == null){
                    mainDb.dao.insertProduct(Product(
                        null,
                        "Sauce - ${counter++}",
                        2,
                        70,
                        result.contents
                    ))
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Item saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }else{
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Duplicated item!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var counter = 0
            val productStateList = mainDb.dao.getAllProducts()
                .collectAsState(initial = emptyList())

            ComposeLesson7Theme {
                Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .fillMaxWidth()
                            .fillMaxHeight(0.9f),
                    ) {
                        items(productStateList.value) { product ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp)
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    text = product.name,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Button(onClick = {
                        scan()
                    }) {
                        Text(text = "Add new product")
                    }
                }
            }
        }
    }

    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a barcode")
        options.setCameraId(0) // Use a specific camera of the device
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        scanLauncher.launch(options)
    }
}