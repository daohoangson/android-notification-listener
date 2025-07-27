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
    
    @Delete
    suspend fun deleteFailedNotification(notification: FailedNotification)
    
    @Delete
    suspend fun deleteFailedNotifications(notifications: List<FailedNotification>)
    
    @Query("SELECT COUNT(*) FROM failed_notifications")
    fun getFailedNotificationCountFlow(): Flow<Int>
    
    @Query("SELECT * FROM failed_notifications ORDER BY timestamp ASC")
    fun getAllFailedNotificationsFlow(): Flow<List<FailedNotification>>

}