package com.daohoangson.n8n.notificationlistener.config

import org.junit.Test
import org.junit.Assert.*

class NotificationFilterEngineTest {

    private val filterEngine = NotificationFilterEngine()

    @Test
    fun isIgnored_shouldReturnTrueForIgnoredPackages() {
        assertTrue(filterEngine.isIgnored("com.android.systemui"))
        assertTrue(filterEngine.isIgnored("com.google.android.gms"))
        assertTrue(filterEngine.isIgnored("com.android.providers.downloads"))
    }

    @Test
    fun isIgnored_shouldReturnFalseForNonIgnoredPackages() {
        assertFalse(filterEngine.isIgnored("com.slack"))
        assertFalse(filterEngine.isIgnored("com.instagram.android"))
        assertFalse(filterEngine.isIgnored("com.unknown.app"))
    }

    @Test
    fun findMatchingUrls_shouldReturnEmptyListForIgnoredPackage() {
        val notificationData = NotificationData(
            packageName = "com.android.systemui",
            title = "Test",
            text = "Test message"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchExactPackageName() {
        val notificationData = NotificationData(
            packageName = "com.slack",
            title = "New message",
            text = "Hello world"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Slack Notifications", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldMatchPackageNameWithTitleRegex() {
        val notificationData = NotificationData(
            packageName = "com.facebook.katana",
            title = "John mentioned you in a comment",
            text = "Check out this post"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Social Media", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldNotMatchPackageNameWithFailingTitleRegex() {
        val notificationData = NotificationData(
            packageName = "com.facebook.katana",
            title = "John posted a new photo",
            text = "Check out this post"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchPackageNameWithTextRegex() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Security Alert",
            text = "Suspicious activity detected on your account"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Urgent Alerts", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldNotMatchPackageNameWithFailingTextRegex() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Account Update",
            text = "Your monthly statement is ready"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldReturnEmptyListForUnknownPackage() {
        val notificationData = NotificationData(
            packageName = "com.unknown.app",
            title = "Test",
            text = "Test message"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchMultipleUrls() {
        // Note: With the current config, no package matches multiple URLs
        // This test is for future configurations
        val notificationData = NotificationData(
            packageName = "com.instagram.android",
            title = "New follower",
            text = "Someone started following you"
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Social Media", matchingUrls[0].name)
    }

    @Test
    fun extractNotificationData_shouldParseJsonCorrectly() {
        val jsonPayload = """
            {
                "packageName": "com.slack",
                "title": "New message",
                "text": "Hello from John",
                "timestamp": 1234567890,
                "id": 123,
                "tag": "message"
            }
        """.trimIndent()
        
        val notificationData = filterEngine.extractNotificationData(jsonPayload)
        assertEquals("com.slack", notificationData.packageName)
        assertEquals("New message", notificationData.title)
        assertEquals("Hello from John", notificationData.text)
    }

    @Test
    fun extractNotificationData_shouldHandleNullValues() {
        val jsonPayload = """
            {
                "packageName": "com.test.app",
                "title": null,
                "text": null,
                "timestamp": 1234567890
            }
        """.trimIndent()
        
        val notificationData = filterEngine.extractNotificationData(jsonPayload)
        assertEquals("com.test.app", notificationData.packageName)
        assertNull(notificationData.title)
        assertNull(notificationData.text)
    }
}