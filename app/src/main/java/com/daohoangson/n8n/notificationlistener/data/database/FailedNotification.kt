package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failed_notifications")
data class FailedNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payload: String,           // JSON notification data
    val webhookUrl: String,        // Which URL failed
    val webhookName: String,       // Display name
    val packageName: String,       // For quick filtering
    val title: String?,            // For display purposes
    val text: String?,             // For display purposes
    val timestamp: Long = System.currentTimeMillis(),
    val errorMessage: String?      // Network error details
)