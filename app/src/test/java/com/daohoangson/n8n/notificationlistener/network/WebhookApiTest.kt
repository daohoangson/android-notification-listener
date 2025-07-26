package com.daohoangson.n8n.notificationlistener.network

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
        val jsonPayload = """{"test":"data"}"""
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
        
        // Act
        val response = webhookApi.sendNotification("webhook-test/test", requestBody)
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        assertEquals(200, response.code())
        
        val request = mockWebServer.takeRequest()
        assertEquals("POST", request.method)
        assertEquals("/webhook-test/test", request.path)
        assertTrue("Request should contain JSON", request.body.readUtf8().contains("test"))
    }
    
    @Test
    fun `sendNotification should return unsuccessful response on 400`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(400))
        val jsonPayload = """{"test":"data"}"""
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
        
        // Act
        val response = webhookApi.sendNotification("webhook-test/test", requestBody)
        
        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals(400, response.code())
    }
    
    @Test
    fun `sendNotification should return unsuccessful response on 500`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(500))
        val jsonPayload = """{"test":"data"}"""
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
        
        // Act
        val response = webhookApi.sendNotification("webhook-test/test", requestBody)
        
        // Assert
        assertFalse("Response should not be successful", response.isSuccessful)
        assertEquals(500, response.code())
    }
    
    @Test
    fun `sendNotification should handle complex payload`() = runTest {
        // Arrange
        mockWebServer.enqueue(MockResponse().setResponseCode(200))
        val jsonPayload = """
            {
                "packageName": "com.example.app",
                "title": "Test Notification",
                "text": "This is a test message",
                "timestamp": 1234567890,
                "id": 123,
                "tag": "test_tag"
            }
        """.trimIndent()
        val requestBody = jsonPayload.toRequestBody("application/json".toMediaType())
        
        // Act
        val response = webhookApi.sendNotification("webhook-test/test", requestBody)
        
        // Assert
        assertTrue("Response should be successful", response.isSuccessful)
        
        val request = mockWebServer.takeRequest()
        val requestBodyString = request.body.readUtf8()
        assertTrue("Request should contain packageName", requestBodyString.contains("com.example.app"))
        assertTrue("Request should contain title", requestBodyString.contains("Test Notification")) 
        assertTrue("Request should contain text", requestBodyString.contains("This is a test message"))
    }
}