package com.daohoangson.n8n.notificationlistener.utils

import android.service.notification.StatusBarNotification
import com.google.gson.Gson

data class NotificationData(
    val packageName: String,
    val title: String?,
    val text: String?,
    val timestamp: Long,
    val id: Int,
    val tag: String?
) {
    companion object {
        private val gson = Gson()
    }
    
    fun toJson(): String {
        return gson.toJson(this)
    }
}

object NotificationDataExtractor {
    
    fun extractNotificationData(statusBarNotification: StatusBarNotification): NotificationData {
        val notification = statusBarNotification.notification
        val extras = notification.extras
        
        return NotificationData(
            packageName = statusBarNotification.packageName,
            title = extras?.getCharSequence("android.title")?.toString(),
            text = extras?.getCharSequence("android.text")?.toString(),
            timestamp = statusBarNotification.postTime,
            id = statusBarNotification.id,
            tag = statusBarNotification.tag
        )
    }
    
}