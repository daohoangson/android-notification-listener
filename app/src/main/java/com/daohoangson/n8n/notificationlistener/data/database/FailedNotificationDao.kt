package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FailedNotificationDao {
    @Insert
    suspend fun insertFailedNotification(notification: FailedNotification)
    
    @Query("SELECT * FROM failed_notifications ORDER BY timestamp ASC")
    suspend fun getAllFailedNotifications(): List<FailedNotification>
    
    @Delete
    suspend fun deleteFailedNotification(notification: FailedNotification)
    
    @Delete
    suspend fun deleteFailedNotifications(notifications: List<FailedNotification>)
    
    @Query("SELECT COUNT(*) FROM failed_notifications")
    suspend fun getFailedNotificationCount(): Int
    
    @Query("SELECT COUNT(*) FROM failed_notifications")
    fun getFailedNotificationCountFlow(): Flow<Int>
    
    @Query("DELETE FROM failed_notifications")
    suspend fun deleteAllFailedNotifications()
    
    @Query("SELECT * FROM failed_notifications WHERE webhookUrl = :url ORDER BY timestamp DESC")
    suspend fun getFailedNotificationsByUrl(url: String): List<FailedNotification>
    
    @Query("SELECT * FROM failed_notifications ORDER BY timestamp ASC")
    fun getAllFailedNotificationsFlow(): Flow<List<FailedNotification>>
    
    @Query("SELECT * FROM failed_notifications WHERE packageName = :packageName ORDER BY timestamp DESC")
    suspend fun getFailedNotificationsByPackage(packageName: String): List<FailedNotification>
}