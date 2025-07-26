package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "failed_notifications")
data class FailedNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)