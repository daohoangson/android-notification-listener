package com.daohoangson.n8n.notificationlistener.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UndecidedNotificationDao {
    
    @Insert
    suspend fun insertUndecidedNotification(undecidedNotification: UndecidedNotification)
    
    @Query("SELECT * FROM undecided_notifications ORDER BY timestamp DESC")
    suspend fun getAllUndecidedNotifications(): List<UndecidedNotification>
    
    @Query("SELECT * FROM undecided_notifications WHERE reason = :reason ORDER BY timestamp DESC")
    suspend fun getUndecidedNotificationsByReason(reason: String): List<UndecidedNotification>
    
    @Delete
    suspend fun deleteUndecidedNotification(undecidedNotification: UndecidedNotification)
    
    @Query("DELETE FROM undecided_notifications")
    suspend fun deleteAllUndecidedNotifications()
    
    @Query("SELECT COUNT(*) FROM undecided_notifications")
    suspend fun getUndecidedNotificationCount(): Int
}