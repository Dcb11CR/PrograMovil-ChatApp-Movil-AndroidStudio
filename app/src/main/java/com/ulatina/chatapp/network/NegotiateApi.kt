package com.ulatina.chatapp.network

import retrofit2.http.GET

data class NegotiateResponse(
    val url: String,
    val accessToken: String
)

interface NegotiateApi {
    @GET("negotiate")
    suspend fun getConnectionInfo(): NegotiateResponse
}
