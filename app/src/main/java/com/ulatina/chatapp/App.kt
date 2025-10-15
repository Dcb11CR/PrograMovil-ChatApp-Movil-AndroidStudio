package com.ulatina.chatapp

import android.app.Application
import android.util.Log
import io.reactivex.rxjava3.plugins.RxJavaPlugins

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Evita que errores RxJava no manejados tumben la app
        RxJavaPlugins.setErrorHandler { e ->
            Log.e("RxJava", "Unhandled Rx error", e)
        }
    }
}
