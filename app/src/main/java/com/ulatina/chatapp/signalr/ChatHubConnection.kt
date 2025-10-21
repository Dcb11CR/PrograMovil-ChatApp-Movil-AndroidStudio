package com.ulatina.chatapp.signalr

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class ChatHubConnection {

    private var hubConnection: HubConnection? = null
    private val TAG = "SignalR"
    private val NEGOTIATE_URL = "https://programovil.net"


    /** Conecta y registra handlers para JSON plano (String) */
    fun connect(onReceiveJson: (String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1) negotiate
                val client = OkHttpClient()
                val response = client.newCall(Request.Builder().url(NEGOTIATE_URL).build()).execute()
                val body = response.body?.string() ?: throw IllegalStateException("negotiation empty")
                val json = JSONObject(body)
                val url = json.getString("url")
                val accessToken = json.getString("accessToken")

                // 2) crear hub y handlers para E2EE
                hubConnection = HubConnectionBuilder.create(url)
                    .withAccessTokenProvider(Single.fromCallable { accessToken })
                    .build()

                // Recibe claves públicas
                hubConnection?.on("ReceivePublicKey", { payload: String ->
                    onReceiveJson(payload)
                }, String::class.java)

                // Recibe mensajes cifrados
                hubConnection?.on("ReceiveCipher", { payload: String ->
                    onReceiveJson(payload)
                }, String::class.java)

                // (opcional) si aún usas mensajes en claro, deja esto mientras migras:
                hubConnection?.on("ReceiveMessage", { payload: String ->
                    onReceiveJson(payload)
                }, String::class.java)

                // 3) conectar
                hubConnection?.start()?.blockingAwait()
                Log.i(TAG, "✅ Conectado con negotiate + handlers E2EE")
            } catch (e: Exception) {
                Log.e(TAG, "❌ connect error", e)
            }
        }
    }

    /** Envía un JSON a un método del hub (por ejemplo: SendCipher o SharePublicKey) */
    fun sendJson(method: String, payloadJson: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                hubConnection?.send(method, payloadJson)
            } catch (e: Exception) {
                Log.e(TAG, "❌ sendJson error", e)
            }
        }
    }

    fun stop() { hubConnection?.stop() }
}
