package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.config.WebhookUrl
import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotification
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import com.daohoangson.n8n.notificationlistener.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

enum class ProcessingResult {
    SENT_TO_URLS,
    IGNORED,
    NO_MATCHING_RULES,
    FAILED_TO_SEND
}

@Singleton
class NotificationRepository @Inject constructor(
    private val context: Context,
    private val webhookApi: WebhookApi,
    private val failedNotificationDao: FailedNotificationDao,
    private val undecidedNotificationDao: UndecidedNotificationDao
) {
    
    suspend fun sendToWebhook(jsonPayload: String, webhookUrl: WebhookUrl): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendNotification(webhookUrl.url, requestBody)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    fun getFailedNotificationCountFlow(): Flow<Int> = failedNotificationDao.getFailedNotificationCountFlow()
    
    fun getUndecidedNotificationCountFlow(): Flow<Int> = undecidedNotificationDao.getUndecidedNotificationCountFlow()
    
    fun getAllFailedNotificationsFlow(): Flow<List<FailedNotification>> = failedNotificationDao.getAllFailedNotificationsFlow()
    
    fun getAllUndecidedNotificationsFlow(): Flow<List<UndecidedNotification>> = undecidedNotificationDao.getAllUndecidedNotificationsFlow()
    
    suspend fun storeUndecidedNotification(
        payload: String, 
        reason: String,
        notificationData: NotificationData
    ) {
        withContext(Dispatchers.IO) {
            val undecidedNotification = UndecidedNotification(
                payload = payload,
                packageName = notificationData.packageName,
                title = notificationData.title,
                text = notificationData.text,
                reason = reason
            )
            undecidedNotificationDao.insertUndecidedNotification(undecidedNotification)
        }
    }
    
    suspend fun storeFailedNotification(
        payload: String, 
        webhookUrl: WebhookUrl,
        notificationData: NotificationData,
        errorMessage: String?
    ) {
        withContext(Dispatchers.IO) {
            val failedNotification = FailedNotification(
                payload = payload,
                webhookUrl = webhookUrl.url,
                webhookName = webhookUrl.name,
                packageName = notificationData.packageName,
                title = notificationData.title,
                text = notificationData.text,
                errorMessage = errorMessage
            )
            failedNotificationDao.insertFailedNotification(failedNotification)
        }
    }
    

    
    suspend fun retryFailedNotification(failedNotification: FailedNotification): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = failedNotification.payload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendNotification(failedNotification.webhookUrl, requestBody)
                
                if (response.isSuccessful) {
                    failedNotificationDao.deleteFailedNotification(failedNotification)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    suspend fun uploadUndecidedNotification(undecidedNotification: UndecidedNotification, webhookUrl: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val requestBody = undecidedNotification.payload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendNotification(webhookUrl, requestBody)
                
                if (response.isSuccessful) {
                    undecidedNotificationDao.deleteUndecidedNotification(undecidedNotification)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    suspend fun deleteUndecidedNotification(undecidedNotification: UndecidedNotification) {
        withContext(Dispatchers.IO) {
            undecidedNotificationDao.deleteUndecidedNotification(undecidedNotification)
        }
    }
    
    suspend fun deleteUndecidedNotifications(notifications: List<UndecidedNotification>) {
        withContext(Dispatchers.IO) {
            undecidedNotificationDao.deleteUndecidedNotifications(notifications)
        }
    }
    
    suspend fun deleteFailedNotification(failedNotification: FailedNotification) {
        withContext(Dispatchers.IO) {
            failedNotificationDao.deleteFailedNotification(failedNotification)
        }
    }
    
    suspend fun deleteFailedNotifications(notifications: List<FailedNotification>) {
        withContext(Dispatchers.IO) {
            failedNotificationDao.deleteFailedNotifications(notifications)
        }
    }
}