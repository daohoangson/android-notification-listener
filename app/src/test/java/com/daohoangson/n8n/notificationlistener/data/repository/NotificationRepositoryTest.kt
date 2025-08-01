package com.daohoangson.n8n.notificationlistener.data.repository

import com.daohoangson.n8n.notificationlistener.config.WebhookUrl
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotificationDao
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.RequestBody
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class NotificationRepositoryTest {
    private lateinit var dao: FailedNotificationDao
    private lateinit var undecidedDao: UndecidedNotificationDao
    private lateinit var webhookApi: WebhookApi
    private lateinit var repository: NotificationRepository
    
    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        undecidedDao = mockk(relaxed = true)
        webhookApi = mockk(relaxed = true)
        
        repository = NotificationRepository(webhookApi, dao, undecidedDao)
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
    

}