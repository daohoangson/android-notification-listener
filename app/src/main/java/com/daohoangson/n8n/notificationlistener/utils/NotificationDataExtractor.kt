package com.daohoangson.n8n.notificationlistener.utils

import android.service.notification.StatusBarNotification
import com.google.gson.Gson

object NotificationDataExtractor {
    private val gson = Gson()
    
    fun extractNotificationData(statusBarNotification: StatusBarNotification): String {
        val notification = statusBarNotification.notification
        val extras = notification.extras
        
        val notificationData = mapOf(
            "packageName" to statusBarNotification.packageName,
            "title" to (extras?.getCharSequence("android.title")?.toString() ?: ""),
            "text" to (extras?.getCharSequence("android.text")?.toString() ?: ""),
            "timestamp" to statusBarNotification.postTime,
            "id" to statusBarNotification.id,
            "tag" to statusBarNotification.tag
        )
        
        return gson.toJson(notificationData)
    }
}