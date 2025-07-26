package com.daohoangson.n8n.notificationlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.utils.NotificationDataExtractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: NotificationRepository
    
    override fun onCreate() {
        super.onCreate()
        repository = NotificationRepository(this)
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    val payload = NotificationDataExtractor.extractNotificationData(statusBarNotification)
                    repository.sendNotification(payload)
                } catch (e: Exception) {
                    // Log error but don't crash the service
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // CoroutineScope will be cleaned up automatically when the service is destroyed
    }
}