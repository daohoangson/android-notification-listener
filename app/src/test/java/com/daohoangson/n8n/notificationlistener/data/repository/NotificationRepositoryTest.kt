package com.daohoangson.n8n.notificationlistener.data.repository

import android.content.Context
import com.daohoangson.n8n.notificationlistener.config.WebhookUrl
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotificationRepositoryTest {
    private lateinit var context: Context
    private lateinit var dao: FailedNotificationDao
    private lateinit var undecidedDao: UndecidedNotificationDao
    private lateinit var webhookApi: WebhookApi
    private lateinit var repository: NotificationRepository
    
    @Before
    fun setup() {
        context = mockk(relaxed = true)
        dao = mockk(relaxed = true)
        undecidedDao = mockk(relaxed = true)
        webhookApi = mockk(relaxed = true)
        
        repository = NotificationRepository(context, webhookApi, dao, undecidedDao)
    }
    
    @Test
    fun `sendToWebhook success should return true`() = runTest {
        // Arrange
        val payload = "{\"packageName\":\"com.test\",\"title\":\"Test\",\"text\":\"Message\"}"
        val webhookUrl = WebhookUrl("Test URL", "http://test.com", emptyList())
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        
        coEvery { webhookApi.sendNotification(any(), any<RequestBody>()) } returns successResponse
        
        // Act
        val result = repository.sendToWebhook(payload, webhookUrl)
        
        // Assert
        assertTrue(result)
        coVerify(exactly = 1) { webhookApi.sendNotification(any(), any<RequestBody>()) }
    }
    
    @Test
    fun `sendToWebhook failure should return false`() = runTest {
        // Arrange
        val payload = "{\"packageName\":\"com.test\",\"title\":\"Test\",\"text\":\"Message\"}"
        val webhookUrl = WebhookUrl("Test URL", "http://test.com", emptyList())
        val failureResponse = mockk<Response<Unit>>()
        every { failureResponse.isSuccessful } returns false
        
        coEvery { webhookApi.sendNotification(any(), any<RequestBody>()) } returns failureResponse
        
        // Act
        val result = repository.sendToWebhook(payload, webhookUrl)
        
        // Assert
        assertFalse(result)
        coVerify(exactly = 1) { webhookApi.sendNotification(any(), any<RequestBody>()) }
    }
    
    @Test
    fun `sendToWebhook exception should return false`() = runTest {
        // Arrange
        val payload = "{\"packageName\":\"com.test\",\"title\":\"Test\",\"text\":\"Message\"}"
        val webhookUrl = WebhookUrl("Test URL", "http://test.com", emptyList())
        
        coEvery { webhookApi.sendNotification(any(), any<RequestBody>()) } throws Exception("Network error")
        
        // Act
        val result = repository.sendToWebhook(payload, webhookUrl)
        
        // Assert
        assertFalse(result)
        coVerify(exactly = 1) { webhookApi.sendNotification(any(), any<RequestBody>()) }
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
            FailedNotification(id = 1, payload = "payload1", webhookUrl = "http://test1.com", webhookName = "Test1", packageName = "com.test1", title = "Title1", text = "Text1", errorMessage = null),
            FailedNotification(id = 2, payload = "payload2", webhookUrl = "http://test2.com", webhookName = "Test2", packageName = "com.test2", title = "Title2", text = "Text2", errorMessage = null),
            FailedNotification(id = 3, payload = "payload3", webhookUrl = "http://test3.com", webhookName = "Test3", packageName = "com.test3", title = "Title3", text = "Text3", errorMessage = null)
        )
        
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        
        coEvery { dao.getAllFailedNotifications() } returns failedNotifications
        coEvery { webhookApi.sendNotification(any(), any<RequestBody>()) } returns successResponse
        coEvery { dao.deleteFailedNotification(any()) } returns Unit
        
        // Act
        val result = repository.retryFailedNotifications()
        
        // Assert
        assertTrue("All notifications should be successful", result)
        coVerify(exactly = 1) { dao.getAllFailedNotifications() }
        coVerify(exactly = 3) { webhookApi.sendNotification(any(), any<RequestBody>()) }
        coVerify(exactly = 3) { dao.deleteFailedNotification(any()) }
    }
    
    @Test
    fun `retryFailedNotifications should return false if some notifications fail`() = runTest {
        // Arrange
        val failedNotifications = listOf(
            FailedNotification(id = 1, payload = "payload1", webhookUrl = "http://test1.com", webhookName = "Test1", packageName = "com.test1", title = "Title1", text = "Text1", errorMessage = null),
            FailedNotification(id = 2, payload = "payload2", webhookUrl = "http://test2.com", webhookName = "Test2", packageName = "com.test2", title = "Title2", text = "Text2", errorMessage = null)
        )
        
        val successResponse = mockk<Response<Unit>>()
        every { successResponse.isSuccessful } returns true
        
        val failureResponse = mockk<Response<Unit>>()
        every { failureResponse.isSuccessful } returns false
        
        coEvery { dao.getAllFailedNotifications() } returns failedNotifications
        coEvery { webhookApi.sendNotification(any(), any<RequestBody>()) } returnsMany listOf(successResponse, failureResponse)
        coEvery { dao.deleteFailedNotification(any()) } returns Unit
        
        // Act
        val result = repository.retryFailedNotifications()
        
        // Assert
        assertFalse("Should return false when some notifications fail", result)
        coVerify(exactly = 1) { dao.getAllFailedNotifications() }
        coVerify(exactly = 2) { webhookApi.sendNotification(any(), any<RequestBody>()) }
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