package com.daohoangson.n8n.notificationlistener.network

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url

interface WebhookApi {
    @POST
    suspend fun sendNotification(@Url url: String, @Body payload: RequestBody): Response<Unit>
}