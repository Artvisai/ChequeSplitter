package com.example.chequesplitter

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.chequesplitter.data.Cheque
import com.example.chequesplitter.data.Customer
import com.example.chequesplitter.data.MainDb
import com.example.chequesplitter.data.MyInterface
import com.example.chequesplitter.data.Product
import com.example.chequesplitter.screens.CustomerAddScreen
import com.example.chequesplitter.screens.MainListScreen
import com.example.chequesplitter.ui.theme.ChequeSplitterTheme
import com.example.chequesplitter.ui.theme.Purple200
import com.example.chequesplitter.ui.theme.Purple40
import com.example.chequesplitter.ui.theme.PurpleGrey100
import com.journeyapps.barcodescanner.ScanContract
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

const val API_TOKEN = "Ваш API-токен"
const val MAIN_LIST_SCREEN = "main_list_screen"
const val CHEQUE_EDIT_SCREEN = "cheque_edit_screen"
const val CUSTOMER_ADD_SCREEN = "customer_add_screen"


@AndroidEntryPoint
class MainActivity : ComponentActivity(), MyInterface {
    @Inject
    lateinit var mainDb: MainDb

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
            val navController = rememberNavController()
            val chequeStateList = mainDb.dao.getAllCheques()
                .collectAsState(initial = emptyList())
            val customerStateList = mainDb.dao.getAllCustomers()
                .collectAsState(initial = emptyList())

            ChequeSplitterTheme {
                NavHost(
                    navController = navController,
                    startDestination = MAIN_LIST_SCREEN
                ){
                    composable(MAIN_LIST_SCREEN){
                        MainListScreen(mainDb, scanLauncher, chequeStateList, navController) {
                            navController.navigate(CHEQUE_EDIT_SCREEN)
                            navController.navigate(CUSTOMER_ADD_SCREEN)
                        }
                    }
                    composable(
                        "cheque_edit_screen/{qrData}",
                        arguments = listOf(navArgument("qrData"
                        ) { type = NavType.StringType }))
                    { navBackStack ->
                        val qrData = navBackStack.arguments?.getString("qrData")
                        ChequeEditScreen(navController, qrData) {
                            navController.navigate(MAIN_LIST_SCREEN)
                        }
                    }
                    composable(CUSTOMER_ADD_SCREEN){
                        CustomerAddScreen(this@MainActivity, mainDb, navController, customerStateList){
                            navController.navigate(MAIN_LIST_SCREEN)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ChequeEditScreen(navController: NavController, qrData: String?, onClick: () -> Unit){
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
        for (i in productStateList.value){
            productUserStringList.add("")
        }
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .verticalScroll(rememberScrollState()),
            ) {
                for((i, items) in productStateList.value.withIndex()) {
                    val arrayState = remember {
                    mutableStateOf(JSONArray())
                    }
                    val objState = remember {
                        mutableStateOf(JSONObject())
                    }
                    objState.value = JSONObject().put("customers", arrayState.value)
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
                            text = "Customers: " + objState.value.getString("customers") + items.customersString
                        )
                        Row(modifier = Modifier
                            .padding(10.dp)
                            .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically){
                            Button(onClick = {
                                if (chosenUserTextState.value != ""){
                                    for (t in 0 until arrayState.value.length()) {
                                        if (arrayState.value.getString(t) == chosenUserTextState.value) {
                                            arrayState.value.remove(t)
                                            break
                                        }
                                        Log.e("MyLog", "Remove: " + objState.value.toString())
                                    }
                                    Log.e("MyLog", "Remove: " + objState.value.toString())
                                }
                                objState.value = JSONObject().put("customers", arrayState.value)
                                productUserStringList[i] = objState.value.toString()
                            },
                                colors = ButtonDefaults.buttonColors(containerColor = Purple40))
                            {
                                Text(text = "Remove")
                            }
                            Button(onClick = {
                                isInArrayState.value = false
                                if (chosenUserTextState.value != ""){
                                    for (t in 0 until arrayState.value.length()){
                                        isInArrayState.value = isInArrayState.value or (arrayState.value.getString(t) == chosenUserTextState.value)
                                        Log.e("MyLog", "Add: " + objState.value.toString())
                                    }
                                    if (!isInArrayState.value){
                                        arrayState.value.put(chosenUserTextState.value)
                                        Log.e("MyLog", "Add After: " + objState.value.toString())
                                    }
                                    Log.e("MyLog", "Add After: " + objState.value.toString())
                                }
                                objState.value = JSONObject().put("customers", arrayState.value)
                                productUserStringList[i] = objState.value.toString()
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
                        mainDb.dao.updateProduct(Product(
                            prod.productId,
                            prod.name,
                            prod.quantity,
                            prod.price,
                            prod.sum,
                            productUserStringList[i],
                            prod.idQR
                        ))
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
                                temp.getString("user"),
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
                                    "",
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