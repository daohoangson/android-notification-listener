package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "undecided_notifications")
data class UndecidedNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payload: String,           // JSON notification data
    val packageName: String,       // For quick filtering
    val title: String?,            // For display purposes
    val text: String?,             // For display purposes
    val timestamp: Long = System.currentTimeMillis(),
    val reason: String             // "NO_MATCH" only (ignored notifications are black-holed)
)