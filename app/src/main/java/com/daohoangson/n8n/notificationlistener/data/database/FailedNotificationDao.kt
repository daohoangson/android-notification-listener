package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FailedNotificationDao {
    @Insert
    suspend fun insertFailedNotification(notification: FailedNotification)
    
    @Query("SELECT * FROM failed_notifications ORDER BY timestamp ASC")
    suspend fun getAllFailedNotifications(): List<FailedNotification>
    
    @Delete
    suspend fun deleteFailedNotification(notification: FailedNotification)
    
    @Query("SELECT COUNT(*) FROM failed_notifications")
    suspend fun getFailedNotificationCount(): Int
    
    @Query("DELETE FROM failed_notifications")
    suspend fun deleteAllFailedNotifications()
}