package com.ulatina.chatapp.network

class NegotiateRepository {
    private val api = RetrofitClient.instance.create(NegotiateApi::class.java)

    suspend fun getConnectionInfo(): NegotiateResponse {
        return api.getConnectionInfo()
    }
}