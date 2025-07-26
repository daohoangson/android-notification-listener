package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.data.database.AppDatabase
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificationRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val failedNotificationDao = database.failedNotificationDao()
    private val webhookApi = NetworkModule.webhookApi
    
    suspend fun sendNotification(payload: Any) {
        withContext(Dispatchers.IO) {
            try {
                val response = webhookApi.sendNotification(payload)
                if (!response.isSuccessful) {
                    storeFailedNotification(payload.toString())
                }
            } catch (e: Exception) {
                storeFailedNotification(payload.toString())
            }
        }
    }
    
    private suspend fun storeFailedNotification(payload: String) {
        val failedNotification = FailedNotification(payload = payload)
        failedNotificationDao.insertFailedNotification(failedNotification)
    }
    
    suspend fun getFailedNotificationCount(): Int {
        return withContext(Dispatchers.IO) {
            failedNotificationDao.getFailedNotificationCount()
        }
    }
    
    suspend fun retryFailedNotifications(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val failedNotifications = failedNotificationDao.getAllFailedNotifications()
                var allSuccessful = true
                
                for (failedNotification in failedNotifications) {
                    try {
                        val response = webhookApi.sendNotification(failedNotification.payload)
                        if (response.isSuccessful) {
                            failedNotificationDao.deleteFailedNotification(failedNotification)
                        } else {
                            allSuccessful = false
                        }
                    } catch (e: Exception) {
                        allSuccessful = false
                    }
                }
                
                allSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }
}