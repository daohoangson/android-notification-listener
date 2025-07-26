package com.daohoangson.n8n.notificationlistener.network

import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WebhookApiTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var webhookApi: WebhookApi
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            
        webhookApi = retrofit.create(WebhookApi::class.java)
    }
    
    @After
    fun teardown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `sendNotification should return successful response on 200`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val payload = mapOf("test" to "data")
        
        // Act
        val response = webhookApi.sendNotification(payload)
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertEquals(200, response.code())
        
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/webhook-test/d904a5db-1633-42f2-84ff-dea794b002d5", request.path)
        assertTrue("Request should contain JSON", request.body.readUtf8().contains("test"))
    }
    
    @Test
    fun `sendNotification should return unsuccessful response on 400`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(400))
        val payload = mapOf("test" to "data")
        
        // Act
        val response = webhookApi.sendNotification(payload)
        
        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals(400, response.code())
    }
    
    @Test
    fun `sendNotification should return unsuccessful response on 500`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        val payload = mapOf("test" to "data")
        
        // Act
        val response = webhookApi.sendNotification(payload)
        
        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals(500, response.code())
    }
    
    @Test
    fun `sendNotification should handle complex payload`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val payload = mapOf(
            "packageName" to "com.example.app",
            "title" to "Test Notification",
            "text" to "This is a test message",
            "timestamp" to 1234567890L,
            "id" to 123,
            "tag" to "test_tag"
        )
        
        // Act
        val response = webhookApi.sendNotification(payload)
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        
        val request = mockWebServer.takeRequest()
        val requestBody = request.body.readUtf8()
        assertTrue("Request should contain packageName", requestBody.contains("com.example.app"))
        assertTrue("Request should contain title", requestBody.contains("Test Notification"))
        assertTrue("Request should contain text", requestBody.contains("This is a test message"))
    }
}