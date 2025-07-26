package com.daohoangson.n8n.notificationlistener.utils

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDataExtractorTest {
    private val gson = Gson()
    
    @Test
    fun `extractNotificationData should return valid JSON with all fields`() {
        // Arrange
        val statusBarNotification = mockk<StatusBarNotification>(relaxed = true)
        val notification = mockk<Notification>(relaxed = true)
        val extras = mockk<Bundle>(relaxed = true)
        
        every { statusBarNotification.packageName } returns "com.example.test"
        every { statusBarNotification.notification } returns notification
        every { statusBarNotification.postTime } returns 1234567890L
        every { statusBarNotification.id } returns 123
        every { statusBarNotification.tag } returns "test_tag"
        every { notification.extras } returns extras
        every { extras.getCharSequence("android.title") } returns "Test Title"
        every { extras.getCharSequence("android.text") } returns "Test Content"
        
        // Act
        val result = NotificationDataExtractor.extractNotificationData(statusBarNotification)
        
        // Assert
        assertTrue("Result should be valid JSON", result.isNotEmpty())
        
        val parsedJson = gson.fromJson(result, Map::class.java)
        assertEquals("com.example.test", parsedJson["packageName"])
        assertEquals("Test Title", parsedJson["title"])
        assertEquals("Test Content", parsedJson["text"])
        assertEquals(1234567890.0, parsedJson["timestamp"])
        assertEquals(123.0, parsedJson["id"])
        assertEquals("test_tag", parsedJson["tag"])
    }
    
    @Test
    fun `extractNotificationData should handle null values gracefully`() {
        // Arrange
        val statusBarNotification = mockk<StatusBarNotification>(relaxed = true)
        val notification = mockk<Notification>(relaxed = true)
        val extras = mockk<Bundle>(relaxed = true)
        
        every { statusBarNotification.packageName } returns "com.example.test"
        every { statusBarNotification.notification } returns notification
        every { statusBarNotification.postTime } returns 1234567890L
        every { statusBarNotification.id } returns 123
        every { statusBarNotification.tag } returns null
        every { notification.extras } returns extras
        every { extras.getCharSequence("android.title") } returns null
        every { extras.getCharSequence("android.text") } returns null
        
        // Act
        val result = NotificationDataExtractor.extractNotificationData(statusBarNotification)
        
        // Assert
        assertTrue("Result should be valid JSON", result.isNotEmpty())
        
        val parsedJson = gson.fromJson(result, Map::class.java)
        assertEquals("com.example.test", parsedJson["packageName"])
        assertEquals("", parsedJson["title"])
        assertEquals("", parsedJson["text"])
        assertEquals(1234567890.0, parsedJson["timestamp"])
        assertEquals(123.0, parsedJson["id"])
        assertEquals(null, parsedJson["tag"])
    }
    
    @Test
    fun `extractNotificationData should handle null extras`() {
        // Arrange
        val statusBarNotification = mockk<StatusBarNotification>(relaxed = true)
        val notification = mockk<Notification>(relaxed = true)
        
        every { statusBarNotification.packageName } returns "com.example.test"
        every { statusBarNotification.notification } returns notification
        every { statusBarNotification.postTime } returns 1234567890L
        every { statusBarNotification.id } returns 123
        every { statusBarNotification.tag } returns "test_tag"
        every { notification.extras } returns null
        
        // Act
        val result = NotificationDataExtractor.extractNotificationData(statusBarNotification)
        
        // Assert
        assertTrue("Result should be valid JSON", result.isNotEmpty())
        
        val parsedJson = gson.fromJson(result, Map::class.java)
        assertEquals("com.example.test", parsedJson["packageName"])
        assertEquals("", parsedJson["title"])
        assertEquals("", parsedJson["text"])
        assertEquals(1234567890.0, parsedJson["timestamp"])
        assertEquals(123.0, parsedJson["id"])
        assertEquals("test_tag", parsedJson["tag"])
    }
}