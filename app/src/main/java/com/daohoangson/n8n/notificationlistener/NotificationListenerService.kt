package com.daohoangson.n8n.notificationlistener

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.data.repository.ProcessingResult
import com.daohoangson.n8n.notificationlistener.utils.NotificationDataExtractor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListenerService : NotificationListenerService() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Inject
    lateinit var repository: NotificationRepository
    
    override fun onCreate() {
        super.onCreate()
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        
        sbn?.let { statusBarNotification ->
            serviceScope.launch {
                try {
                    val payload = NotificationDataExtractor.extractNotificationData(statusBarNotification)
                    val result = repository.processNotification(payload)
                    // Log processing result for debugging (could be useful for monitoring)
                    when (result) {
                        ProcessingResult.SENT_TO_URLS -> {
                            // Successfully sent to webhook(s)
                        }
                        ProcessingResult.IGNORED -> {
                            // Package was in ignore list
                        }
                        ProcessingResult.NO_MATCHING_RULES -> {
                            // No rules matched, stored as undecided
                        }
                        ProcessingResult.FAILED_TO_SEND -> {
                            // Network failure, stored as failed
                        }
                    }
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