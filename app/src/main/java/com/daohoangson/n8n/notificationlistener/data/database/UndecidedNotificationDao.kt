package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UndecidedNotificationDao {
    
    @Insert
    suspend fun insertUndecidedNotification(undecidedNotification: UndecidedNotification)

    @Delete
    suspend fun deleteUndecidedNotification(undecidedNotification: UndecidedNotification)
    
    @Delete
    suspend fun deleteUndecidedNotifications(notifications: List<UndecidedNotification>)
    
    @Query("SELECT COUNT(*) FROM undecided_notifications")
    fun getUndecidedNotificationCountFlow(): Flow<Int>
    
    @Query("SELECT * FROM undecided_notifications ORDER BY timestamp DESC")
    fun getAllUndecidedNotificationsFlow(): Flow<List<UndecidedNotification>>
}