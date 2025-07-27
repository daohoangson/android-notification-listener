package com.daohoangson.n8n.notificationlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.daohoangson.n8n.notificationlistener.config.NotificationFilterEngine
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import com.daohoangson.n8n.notificationlistener.utils.NotificationDataExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Inject
    lateinit var repository: NotificationRepository
    
    @Inject
    lateinit var filterEngine: NotificationFilterEngine
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    val notificationData = NotificationDataExtractor.extractNotificationData(statusBarNotification)
                    processNotification(notificationData)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private suspend fun processNotification(notificationData: NotificationData) {
        // Check if notification should be ignored (black-holed)
        if (filterEngine.isIgnored(notificationData)) {
            return // Black-hole ignored notifications
        }
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        
        if (matchingUrls.isEmpty()) {
            // Store as undecided notification
            val jsonPayload = notificationData.toJson()
            repository.storeUndecidedNotification(jsonPayload, "NO_MATCH", notificationData)
            return
        }
        
        // Send to matching URLs
        val jsonPayload = notificationData.toJson()
        
        for (webhookUrl in matchingUrls) {
            val success = repository.sendToWebhook(jsonPayload, webhookUrl)
            if (!success) {
                repository.storeFailedNotification(jsonPayload, webhookUrl, notificationData, "Failed to send")
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel() // Cancel all running coroutines to prevent memory leaks
    }
}