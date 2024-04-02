package com.example.chequesplitter

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.data.Cheque
import com.example.chequesplitter.data.MyInterface
import com.example.chequesplitter.data.Product
import com.example.chequesplitter.ui.theme.ChequeSplitterTheme
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
const val API_TOKEN = "Ваш API-токен"

@AndroidEntryPoint
class MainActivity : ComponentActivity(), MyInterface {
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
                val chequeByQr = mainDb.dao.getChequeByQr(result.contents)
                if (chequeByQr == null){
                    //parsing data there
                    getChequeResult(result.contents)

                    //add an item if not added
                    /*mainDb.dao.insertCheque(Cheque(
                        null,
                        "Store - ${counter++}",
                        result.contents,
                        LocalDateTime.of(LocalDate.now(), LocalTime.now())
                    ))
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Item saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }*/
                }else{
                    /*getChequeResult(result.contents)*/
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
            val chequeStateList = mainDb.dao.getAllCheques()
                .collectAsState(initial = emptyList())
            var productStateList : State<List<Product>>


            ChequeSplitterTheme {
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
                        items(chequeStateList.value) { cheque ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp, end = 10.dp)
                            ) {
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(15.dp),
                                    text = cheque.storeName + "\n" + cheque.date,
                                    textAlign = TextAlign.Center
                                )
                                productStateList = mainDb.dao.getAllProductsByQr(cheque.qrData)
                                    .collectAsState(initial = emptyList())
                                LazyColumn(
                                    modifier = Modifier
                                        .padding(top = 10.dp)
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.1f),
                                ){
                                    items(productStateList.value){product ->
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Card(modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 10.dp, end = 10.dp)
                                        ){
                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(15.dp),
                                                text = product.name + " " + product.price,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    ButtonsRow()
                }
            }
        }
    }
    @Preview (showBackground = true)
    @Composable
    fun ButtonsRow(){
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(onClick = {
                scan()
            }) {
                Text(text = "Scan cheque")
            }
            Button(onClick = {

            }) {
                Text(text = "Add cheque")
            }
        }
    }

    private fun scan() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan a QR-code")
        options.setCameraId(0) // Use a specific camera of the device
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        scanLauncher.launch(options)
    }

    private fun getChequeResult(qrraw: String) {
        val url = "https://proverkacheka.com/api/v1/check/get"
        val queue = Volley.newRequestQueue(this)
        val requestBody: String
        try{
            val jsonBody = JSONObject()
            jsonBody.put("qrraw", qrraw)
            jsonBody.put("token", API_TOKEN)
            requestBody = jsonBody.toString()

            val stringRequest = object : StringRequest(
                Method.POST,
                url,
                Response.Listener {
                response->
                    Log.e("MyLog", response)
                    val obj = JSONObject(response)
                    val temp = obj.getJSONObject("data").getJSONObject("json")
                    //Name of Store
                    this.onCallback(temp.getString("user"))
                    //2020-10-17T19:23:00
                    val arrayDateTime = temp.getString("dateTime").split("T")
                    val date = LocalDate.parse(arrayDateTime[0], DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    val time = LocalTime.parse(arrayDateTime[1], DateTimeFormatter.ofPattern("HH:mm:ss"))
                    Log.e("MyLog","Response: ${temp.getString("dateTime")}")
                    val productArray = temp.getJSONArray("items")
                    CoroutineScope(Dispatchers.IO).launch {
                        mainDb.dao.insertCheque(
                            Cheque(
                                qrraw,
                                "Store - ${temp.getString("user")}",
                                LocalDateTime.of(date, time)
                            )
                        )
                        for (i in 0 until productArray.length()){
                            val product = productArray[i] as JSONObject
                            Log.e("MyLog","Product: $product")
                            mainDb.dao.insertProduct(
                                Product(
                                    null,
                                    product.getString("name"),
                                    product.getString("quantity").toFloat(),
                                    product.getString("price").toInt(),
                                    product.getString("sum").toInt(),
                                    qrraw
                                )
                            )
                        }

                    }
                    runOnUiThread {
                        Toast.makeText(
                            this@MainActivity,
                            "Item saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Log.e("MyLog","Response: ${temp.getString("user")}")
                },

                Response.ErrorListener {
                    Log.e("MyLog","Volley error: $it")
                }
            ){
                override fun getBodyContentType(): String {
                    return "application/json"
                }

                @Throws(AuthFailureError::class)
                override fun getBody(): ByteArray {
                    return requestBody.toByteArray()
                }
            }
            queue.add(stringRequest)
        } catch (e: JSONException){
            e.printStackTrace()
        }
    }

    override fun onCallback(response: String): String {
        return response
    }
}