package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.data.database.AppDatabase
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.NetworkModule
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotificationRepositoryTest {
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var dao: FailedNotificationDao
    private lateinit var webhookApi: WebhookApi
    private lateinit var repository: NotificationRepository
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        database = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        webhookApi = mockk(relaxed = true)
        
        every { database.failedNotificationDao() } returns dao
        
        // Mock static methods
        mockkObject(AppDatabase.Companion)
        every { AppDatabase.getDatabase(context) } returns database
        
        mockkObject(NetworkModule)
        every { NetworkModule.webhookApi } returns webhookApi
        
        repository = NotificationRepository(context)
    }
    
    @Test
    fun `sendNotification success should not store in database`() = runTest {
        // Arrange
        val payload = "test payload"
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        coEvery { webhookApi.sendNotification(payload) } returns successResponse
        
        // Act
        repository.sendNotification(payload)
        
        // Assert
        coVerify(exactly = 1) { webhookApi.sendNotification(payload) }
        coVerify(exactly = 0) { dao.insertFailedNotification(any()) }
    }
    
    @Test
    fun `sendNotification failure should store in database`() = runTest {
        // Arrange
        val payload = "test payload"
        val failureResponse = mockk<Response<Unit>>()
        every { failureResponse.isSuccessful } returns false
        coEvery { webhookApi.sendNotification(payload) } returns failureResponse
        coEvery { dao.insertFailedNotification(any()) } returns Unit
        
        val capturedNotification = slot<FailedNotification>()
        
        // Act
        repository.sendNotification(payload)
        
        // Assert
        coVerify(exactly = 1) { webhookApi.sendNotification(payload) }
        coVerify(exactly = 1) { dao.insertFailedNotification(capture(capturedNotification)) }
        assertEquals(payload, capturedNotification.captured.payload)
    }
    
    @Test
    fun `sendNotification exception should store in database`() = runTest {
        // Arrange
        val payload = "test payload"
        coEvery { webhookApi.sendNotification(payload) } throws Exception("Network error")
        coEvery { dao.insertFailedNotification(any()) } returns Unit
        
        val capturedNotification = slot<FailedNotification>()
        
        // Act
        repository.sendNotification(payload)
        
        // Assert
        coVerify(exactly = 1) { webhookApi.sendNotification(payload) }
        coVerify(exactly = 1) { dao.insertFailedNotification(capture(capturedNotification)) }
        assertEquals(payload, capturedNotification.captured.payload)
    }
    
    @Test
    fun `getFailedNotificationCount should return correct count`() = runTest {
        // Arrange
        coEvery { dao.getFailedNotificationCount() } returns 5
        
        // Act
        val count = repository.getFailedNotificationCount()
        
        // Assert
        assertEquals(5, count)
        coVerify(exactly = 1) { dao.getFailedNotificationCount() }
    }
    
    @Test
    fun `retryFailedNotifications should send all notifications and delete successful ones`() = runTest {
        // Arrange
        val failedNotifications = listOf(
            FailedNotification(id = 1, payload = "payload1"),
            FailedNotification(id = 2, payload = "payload2"),
            FailedNotification(id = 3, payload = "payload3")
        )
        
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        
        coEvery { dao.getAllFailedNotifications() } returns failedNotifications
        coEvery { webhookApi.sendNotification("payload1") } returns successResponse
        coEvery { webhookApi.sendNotification("payload2") } returns successResponse
        coEvery { webhookApi.sendNotification("payload3") } returns successResponse
        coEvery { dao.deleteFailedNotification(any()) } returns Unit
        
        // Act
        val result = repository.retryFailedNotifications()
        
        // Assert
        assertTrue("All notifications should be successful", result)
        coVerify(exactly = 1) { dao.getAllFailedNotifications() }
        coVerify(exactly = 1) { webhookApi.sendNotification("payload1") }
        coVerify(exactly = 1) { webhookApi.sendNotification("payload2") }
        coVerify(exactly = 1) { webhookApi.sendNotification("payload3") }
        coVerify(exactly = 3) { dao.deleteFailedNotification(any()) }
    }
    
    @Test
    fun `retryFailedNotifications should return false if some notifications fail`() = runTest {
        // Arrange
        val failedNotifications = listOf(
            FailedNotification(id = 1, payload = "payload1"),
            FailedNotification(id = 2, payload = "payload2")
        )
        
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        
        val failureResponse = mockk<Response<Unit>>()
        every { failureResponse.isSuccessful } returns false
        
        coEvery { dao.getAllFailedNotifications() } returns failedNotifications
        coEvery { webhookApi.sendNotification("payload1") } returns successResponse
        coEvery { webhookApi.sendNotification("payload2") } returns failureResponse
        coEvery { dao.deleteFailedNotification(any()) } returns Unit
        
        // Act
        val result = repository.retryFailedNotifications()
        
        // Assert
        assertFalse("Should return false when some notifications fail", result)
        coVerify(exactly = 1) { dao.getAllFailedNotifications() }
        coVerify(exactly = 1) { webhookApi.sendNotification("payload1") }
        coVerify(exactly = 1) { webhookApi.sendNotification("payload2") }
        coVerify(exactly = 1) { dao.deleteFailedNotification(failedNotifications[0]) }
        coVerify(exactly = 0) { dao.deleteFailedNotification(failedNotifications[1]) }
    }
    
    @Test
    fun `retryFailedNotifications should handle exceptions gracefully`() = runTest {
        // Arrange
        coEvery { dao.getAllFailedNotifications() } throws Exception("Database error")
        
        // Act
        val result = repository.retryFailedNotifications()
        
        // Assert
        assertFalse("Should return false on exception", result)
        coVerify(exactly = 1) { dao.getAllFailedNotifications() }
    }
}