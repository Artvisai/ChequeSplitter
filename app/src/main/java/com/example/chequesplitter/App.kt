package com.example.chequesplitter

import android.app.Application
import com.example.chequesplitter.data.MainDb
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {
    val database by lazy { MainDb.createDatabase(this) }
}