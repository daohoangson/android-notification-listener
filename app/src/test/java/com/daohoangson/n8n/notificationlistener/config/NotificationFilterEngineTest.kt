package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import org.junit.Test
import org.junit.Assert.*

class NotificationFilterEngineTest {

    private val filterEngine = NotificationFilterEngine()

    @Test
    fun isIgnored_shouldReturnTrue_forIgnoredPackages() {
        val notificationData = NotificationData(
            packageName = "com.android.systemui",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        assertTrue(filterEngine.isIgnored(notificationData))
    }

    @Test
    fun isIgnored_shouldReturnFalse_forNonIgnoredPackages() {
        val notificationData = NotificationData(
            packageName = "com.slack",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        assertFalse(filterEngine.isIgnored(notificationData))
    }

    @Test
    fun findMatchingUrls_shouldReturnEmpty_forIgnoredPackages() {
        val notificationData = NotificationData(
            packageName = "com.google.android.gms",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldReturnMatchingUrl_forPackageNameMatch() {
        val notificationData = NotificationData(
            packageName = "com.slack",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Slack Notifications", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldReturnEmpty_forNonMatchingPackage() {
        val notificationData = NotificationData(
            packageName = "com.nonexistent.app",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchTitleRegex() {
        val notificationData = NotificationData(
            packageName = "com.facebook.katana",
            title = "John mentioned you in a comment",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Social Media", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldNotMatch_whenTitleRegexFails() {
        val notificationData = NotificationData(
            packageName = "com.facebook.katana",
            title = "John posted a photo",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchTextRegex_caseInsensitive() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Security Alert",
            text = "FRAUD detected on your account",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Urgent Alerts", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldMatchTextRegex_suspicious() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Security Alert",
            text = "Suspicious activity detected",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Urgent Alerts", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldNotMatch_whenTextRegexFails() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Account Update",
            text = "Your balance has been updated",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldHandleNullTitle() {
        val notificationData = NotificationData(
            packageName = "com.facebook.katana",
            title = null,
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldHandleNullText() {
        val notificationData = NotificationData(
            packageName = "com.banking.app",
            title = "Security Alert",
            text = null,
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldMatchMultipleUrls_forSamePackage() {
        val notificationData = NotificationData(
            packageName = "com.instagram.android",
            title = "New message",
            text = "You have a new direct message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Social Media", matchingUrls[0].name)
    }

    @Test
    fun findMatchingUrls_shouldMatch_withNoRegexRules() {
        val notificationData = NotificationData(
            packageName = "com.microsoft.teams",
            title = "Any title",
            text = "Any text",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        
        val matchingUrls = filterEngine.findMatchingUrls(notificationData)
        assertEquals(1, matchingUrls.size)
        assertEquals("Slack Notifications", matchingUrls[0].name)
    }
}