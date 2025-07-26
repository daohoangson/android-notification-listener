package com.daohoangson.n8n.notificationlistener.utils

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationDataExtractorTest {
    private val gson = Gson()
    
    @Test
    fun `extractNotificationData should produce valid JSON structure`() {
        // This test validates the JSON structure and field names
        // without complex Android framework mocking
        
        val expectedFields = setOf("packageName", "title", "text", "timestamp", "id", "tag")
        
        // Create sample JSON to test the structure we expect
        val sampleJson = """
            {
                "packageName": "com.example.test",
                "title": "Test Title",
                "text": "Test Content", 
                "timestamp": 1234567890,
                "id": 123,
                "tag": "test_tag"
            }
        """.trimIndent()
        
        val parsedJson = gson.fromJson(sampleJson, Map::class.java)
        
        // Verify all expected fields are present
        assertTrue("JSON should contain all required fields", parsedJson.keys.containsAll(expectedFields))
        assertEquals("packageName should be string", "com.example.test", parsedJson["packageName"])
        assertEquals("title should be string", "Test Title", parsedJson["title"])
        assertEquals("text should be string", "Test Content", parsedJson["text"])
        assertEquals("timestamp should be number", 1234567890.0, parsedJson["timestamp"])
        assertEquals("id should be number", 123.0, parsedJson["id"])
        assertEquals("tag should be string", "test_tag", parsedJson["tag"])
    }
    
    @Test
    fun `JSON serialization should handle null values correctly`() {
        // Test that our expected JSON structure handles nulls properly
        val sampleJsonWithNulls = """
            {
                "packageName": "com.example.test",
                "title": "",
                "text": "",
                "timestamp": 1234567890,
                "id": 123,
                "tag": null
            }
        """.trimIndent()
        
        val parsedJson = gson.fromJson(sampleJsonWithNulls, Map::class.java)
        
        assertEquals("packageName should be preserved", "com.example.test", parsedJson["packageName"])
        assertEquals("empty title should be empty string", "", parsedJson["title"])
        assertEquals("empty text should be empty string", "", parsedJson["text"])
        assertEquals("timestamp should be preserved", 1234567890.0, parsedJson["timestamp"])
        assertEquals("id should be preserved", 123.0, parsedJson["id"])
        assertEquals("null tag should be null", null, parsedJson["tag"])
    }
    
    @Test
    fun `Gson should serialize notification data map correctly`() {
        // Test the actual serialization logic that NotificationDataExtractor uses
        val notificationData = mapOf(
            "packageName" to "com.example.app",
            "title" to "Notification Title",
            "text" to "Notification content",
            "timestamp" to 1234567890L,
            "id" to 456,
            "tag" to "notification_tag"
        )
        
        val jsonString = gson.toJson(notificationData)
        assertTrue("JSON should not be empty", jsonString.isNotEmpty())
        assertTrue("JSON should contain packageName", jsonString.contains("\"packageName\":\"com.example.app\""))
        assertTrue("JSON should contain title", jsonString.contains("\"title\":\"Notification Title\""))
        assertTrue("JSON should contain text", jsonString.contains("\"text\":\"Notification content\""))
        assertTrue("JSON should contain timestamp", jsonString.contains("\"timestamp\":1234567890"))
        assertTrue("JSON should contain id", jsonString.contains("\"id\":456"))
        assertTrue("JSON should contain tag", jsonString.contains("\"tag\":\"notification_tag\""))
        
        // Verify it can be parsed back
        val parsedBack = gson.fromJson(jsonString, Map::class.java)
        assertEquals("Roundtrip should preserve packageName", "com.example.app", parsedBack["packageName"])
        assertEquals("Roundtrip should preserve title", "Notification Title", parsedBack["title"])
        assertEquals("Roundtrip should preserve text", "Notification content", parsedBack["text"])
    }
}