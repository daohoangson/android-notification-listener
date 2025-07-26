package com.daohoangson.n8n.notificationlistener.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface WebhookApi {
    @POST("webhook-test/d904a5db-1633-42f2-84ff-dea794b002d5")
    suspend fun sendNotification(@Body payload: Any): Response<Unit>
}