package com.ulatina.chatapp.signalr

import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class ChatHubConnection {

    private var hubConnection: HubConnection? = null

    companion object {
        private const val TAG = "SignalR"
        // Local dev: tu backend corriendo con dotnet run
        private const val NEGOTIATE_URL = "http://10.0.2.2:5242/negotiate"
    }

    private val http by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun connect(username: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.i(TAG, "🌐 Solicitando negotiate desde $NEGOTIATE_URL")
                val req = Request.Builder().url(NEGOTIATE_URL).build()
                val res = http.newCall(req).execute()
                if (!res.isSuccessful) {
                    Log.e(TAG, "❌ Error en negotiate: HTTP ${res.code}")
                    return@launch
                }
                val body = res.body?.string()
                if (body.isNullOrEmpty()) {
                    Log.e(TAG, "❌ Respuesta vacía del negotiate.")
                    return@launch
                }

                val json = JSONObject(body)
                var url = json.getString("url")
                val accessToken = json.getString("accessToken")

                // (Opcional) pasa el username como query si tu Hub lo usa
                if (!username.isNullOrBlank()) {
                    val encoded = URLEncoder.encode(username, "UTF-8")
                    url = if (url.contains("?")) "$url&username=$encoded" else "$url?username=$encoded"
                }

                hubConnection = HubConnectionBuilder.create(url)
                    .withAccessTokenProvider(Single.fromCallable { accessToken })
                    .build()

                // Listener de mensajes
                hubConnection?.on("ReceiveMessage", { payload: JSONObject ->
                    val user = payload.optString("user")
                    val message = payload.optString("message")
                    val fecha = payload.optString("fechaHoraCostaRica")
                    Log.i(TAG, "💬 [$fecha] $user: $message")
                }, JSONObject::class.java)

                // Log en cierre
                hubConnection?.onClosed { error ->
                    Log.w(TAG, "🔌 Conexión cerrada: ${error?.message}")
                }

                // Inicia conexión con manejo de errores (evita OnErrorNotImplementedException)
                hubConnection?.start()
                    ?.subscribe(
                        { Log.i(TAG, "✅ Conectado exitosamente al hub en Azure.") },
                        { e -> Log.e(TAG, "❌ Error al conectar: ${e.message}", e) }
                    )

            } catch (t: Throwable) {
                Log.e(TAG, "❌ Excepción conectando: ${t.message}", t)
            }
        }
    }

    fun isConnected(): Boolean =
        hubConnection?.connectionState == HubConnectionState.CONNECTED

    fun sendMessage(user: String, message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!isConnected()) {
                    Log.e(TAG, "⚠️ No hay conexión activa al hub (estado=${hubConnection?.connectionState}).")
                    return@launch
                }
                hubConnection?.send("SendMessage", user, message)
                Log.i(TAG, "📤 Enviado: $user -> $message")
            } catch (t: Throwable) {
                Log.e(TAG, "❌ Error al enviar mensaje: ${t.message}", t)
            }
        }
    }

    fun disconnect() {
        hubConnection?.stop()
            ?.subscribe(
                { Log.i(TAG, "🔌 Conexión detenida correctamente.") },
                { e -> Log.e(TAG, "❌ Error al detener conexión: ${e.message}", e) }
            )
    }
}
