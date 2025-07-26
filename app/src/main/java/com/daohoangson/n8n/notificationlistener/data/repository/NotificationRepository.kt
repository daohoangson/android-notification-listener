package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import com.daohoangson.n8n.notificationlistener.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val context: Context,
    private val webhookApi: WebhookApi,
    private val failedNotificationDao: FailedNotificationDao
) {
    
    suspend fun sendNotification(jsonPayload: String) {
        withContext(Dispatchers.IO) {
            try {
                val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendNotification(Constants.WEBHOOK_PATH, requestBody)
                if (!response.isSuccessful) {
                    storeFailedNotification(jsonPayload)
                }
            } catch (e: Exception) {
                storeFailedNotification(jsonPayload)
            }
        }
    }
    
    private suspend fun storeFailedNotification(payload: String) {
        val failedNotification = FailedNotification(payload = payload)
        failedNotificationDao.insertFailedNotification(failedNotification)
    }
    
    suspend fun getFailedNotificationCount(): Int {
        return withContext(Dispatchers.IO) {
            failedNotificationDao.getFailedNotificationCount()
        }
    }
    
    suspend fun retryFailedNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val failedNotifications = failedNotificationDao.getAllFailedNotifications()
                var allSuccessful = true
                
                for (failedNotification in failedNotifications) {
                    try {
                        val requestBody = failedNotification.payload.toRequestBody("application/json".toMediaType())
                        val response = webhookApi.sendNotification(Constants.WEBHOOK_PATH, requestBody)
                        if (response.isSuccessful) {
                            failedNotificationDao.deleteFailedNotification(failedNotification)
                        } else {
                            allSuccessful = false
                        }
                    } catch (e: Exception) {
                        allSuccessful = false
                    }
                }
                
                allSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}