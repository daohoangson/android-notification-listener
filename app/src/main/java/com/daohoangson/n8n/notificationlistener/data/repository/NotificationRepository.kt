package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.config.NotificationFilterEngine 
import com.daohoangson.n8n.notificationlistener.config.WebhookUrl
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotification
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import com.daohoangson.n8n.notificationlistener.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    private val undecidedNotificationDao: UndecidedNotificationDao,
    private val filterEngine: NotificationFilterEngine
) {
    
    suspend fun processNotification(jsonPayload: String): ProcessingResult {
        return withContext(Dispatchers.IO) {
            try {
                val notificationData = filterEngine.extractNotificationData(jsonPayload)
                
                if (filterEngine.isIgnored(notificationData.packageName)) {
                    return@withContext ProcessingResult.IGNORED
                }
                
                val matchingUrls = filterEngine.findMatchingUrls(notificationData)
                
                if (matchingUrls.isEmpty()) {
                    storeUndecidedNotification(jsonPayload, "NO_MATCH", notificationData)
                    return@withContext ProcessingResult.NO_MATCHING_RULES
                }
                
                val results = sendToMultipleUrls(jsonPayload, matchingUrls, notificationData)
                
                return@withContext if (results.hasFailures) {
                    ProcessingResult.FAILED_TO_SEND
                } else {
                    ProcessingResult.SENT_TO_URLS
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ProcessingResult.FAILED_TO_SEND
            }
        }
    }
    
    private suspend fun sendToMultipleUrls(
        jsonPayload: String, 
        urls: List<WebhookUrl>,
        notificationData: com.daohoangson.n8n.notificationlistener.config.NotificationData
    ): SendResults {
        var hasFailures = false
        
        for (webhookUrl in urls) {
            try {
                val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
                val response = webhookApi.sendNotification(webhookUrl.url, requestBody)
                
                if (!response.isSuccessful) {
                    storeFailedNotification(jsonPayload, webhookUrl, notificationData, "HTTP ${response.code()}")
                    hasFailures = true
                }
            } catch (e: Exception) {
                storeFailedNotification(jsonPayload, webhookUrl, notificationData, e.message)
                hasFailures = true
            }
        }
        
        return SendResults(hasFailures)
    }
    
    private suspend fun storeUndecidedNotification(
        payload: String, 
        reason: String,
        notificationData: com.daohoangson.n8n.notificationlistener.config.NotificationData
    ) {
        val undecidedNotification = UndecidedNotification(
            payload = payload,
            packageName = notificationData.packageName,
            title = notificationData.title,
            text = notificationData.text,
            reason = reason
        )
        undecidedNotificationDao.insertUndecidedNotification(undecidedNotification)
    }
    
    private suspend fun storeFailedNotification(
        payload: String, 
        webhookUrl: WebhookUrl,
        notificationData: com.daohoangson.n8n.notificationlistener.config.NotificationData,
        errorMessage: String?
    ) {
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
    
    // Legacy method for backward compatibility - now uses processNotification
    suspend fun sendNotification(jsonPayload: String) {
        processNotification(jsonPayload)
    }
    
    suspend fun getFailedNotificationCount(): Int {
        return withContext(Dispatchers.IO) {
            failedNotificationDao.getFailedNotificationCount()
        }
    }
    
    suspend fun getUndecidedNotificationCount(): Int {
        return withContext(Dispatchers.IO) {
            undecidedNotificationDao.getUndecidedNotificationCount()
        }
    }
    
    suspend fun getAllUndecidedNotifications(): List<UndecidedNotification> {
        return withContext(Dispatchers.IO) {
            undecidedNotificationDao.getAllUndecidedNotifications()
        }
    }
    
    suspend fun getAllFailedNotifications(): List<FailedNotification> {
        return withContext(Dispatchers.IO) {
            failedNotificationDao.getAllFailedNotifications()
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
    
    suspend fun deleteFailedNotification(failedNotification: FailedNotification) {
        withContext(Dispatchers.IO) {
            failedNotificationDao.deleteFailedNotification(failedNotification)
        }
    }
    
    // Legacy method for backward compatibility
    suspend fun retryFailedNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val failedNotifications = failedNotificationDao.getAllFailedNotifications()
                var allSuccessful = true
                
                for (failedNotification in failedNotifications) {
                    val success = retryFailedNotification(failedNotification)
                    if (!success) {
                        allSuccessful = false
                    }
                }
                
                allSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
    
    private data class SendResults(val hasFailures: Boolean)
}