package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import org.junit.Test
import org.junit.Assert.*

class NotificationFilterEngineTest {

    private val filterEngine = NotificationFilterEngine()

    // ========================================
    // Tests for isIgnored() method
    // ========================================

    @Test
    fun isIgnored_shouldReturnTrue_forSystemPackages() {
        val systemNotification = NotificationData(
            packageName = "com.android.systemui",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )

        assertTrue(filterEngine.isIgnored(systemNotification))
    }

    @Test
    fun isIgnored_shouldReturnTrue_forGoogleServicesPackages() {
        val gmsNotification = NotificationData(
            packageName = "com.google.android.gms",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )

        assertTrue(filterEngine.isIgnored(gmsNotification))
    }

    @Test
    fun isIgnored_shouldReturnTrue_forSocialMediaApps() {
        // Test Slack
        val slackNotification = NotificationData(
            packageName = "com.Slack",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        assertTrue(filterEngine.isIgnored(slackNotification))

        // Test Facebook Messenger
        val messengerNotification = NotificationData(
            packageName = "com.facebook.orca",
            title = "John mentioned you in a comment",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        assertTrue(filterEngine.isIgnored(messengerNotification))

        // Test Facebook
        val facebookNotification = NotificationData(
            packageName = "com.facebook.katana",
            title = "John posted a photo",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        assertTrue(filterEngine.isIgnored(facebookNotification))

        // Test Discord
        val discordNotification = NotificationData(
            packageName = "com.discord",
            title = "New message",
            text = "You have a new direct message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        assertTrue(filterEngine.isIgnored(discordNotification))

        // Test Telegram
        val telegramNotification = NotificationData(
            packageName = "org.telegram.messenger",
            title = "Any title",
            text = "Any text",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        assertTrue(filterEngine.isIgnored(telegramNotification))
    }

    // ========================================
    // Tests for findMatchingUrls() method - Bank Apps
    // ========================================

    @Test
    fun findMatchingUrls_shouldMatchBankApps() {
        // Test VCB
        val vcbNotification = NotificationData(
            packageName = "com.VCB",
            title = "Security Alert",
            text = "FRAUD detected on your account",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val vcbUrls = filterEngine.findMatchingUrls(vcbNotification)
        assertEquals(1, vcbUrls.size)
        assertEquals("Bank apps", vcbUrls[0].name)

        // Test Techcombank
        val techcombankNotification = NotificationData(
            packageName = "vn.com.techcombank.bb.app",
            title = "Security Alert",
            text = "Suspicious activity detected",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val techcombankUrls = filterEngine.findMatchingUrls(techcombankNotification)
        assertEquals(1, techcombankUrls.size)
        assertEquals("Bank apps", techcombankUrls[0].name)

        // Test MoMo
        val momoNotification = NotificationData(
            packageName = "com.mservice.momotransfer",
            title = "Payment Notification",
            text = "You have received money",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val momoUrls = filterEngine.findMatchingUrls(momoNotification)
        assertEquals(1, momoUrls.size)
        assertEquals("Bank apps", momoUrls[0].name)

        // Test MyVIB
        val vibNotification = NotificationData(
            packageName = "com.vib.myvib2",
            title = "Transaction Alert",
            text = "Your account has been debited",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val vibUrls = filterEngine.findMatchingUrls(vibNotification)
        assertEquals(1, vibUrls.size)
        assertEquals("Bank apps", vibUrls[0].name)
    }

    // ========================================
    // Tests for findMatchingUrls() method - Empty Results
    // ========================================

    @Test
    fun findMatchingUrls_shouldReturnEmpty_forNonMatchingPackages() {
        val nonMatchingNotification = NotificationData(
            packageName = "com.nonexistent.app",
            title = "Test Title",
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )

        val matchingUrls = filterEngine.findMatchingUrls(nonMatchingNotification)
        assertTrue(matchingUrls.isEmpty())
    }

    @Test
    fun findMatchingUrls_shouldReturnEmpty_forUnconfiguredBankApp() {
        val unconfiguredBankNotification = NotificationData(
            packageName = "com.banking.app",
            title = "Account Update",
            text = "Your balance has been updated",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )

        val matchingUrls = filterEngine.findMatchingUrls(unconfiguredBankNotification)
        assertTrue(matchingUrls.isEmpty())
    }

    // ========================================
    // Tests for findMatchingUrls() method - Edge Cases
    // ========================================

    @Test
    fun findMatchingUrls_shouldHandleNullTitleAndText() {
        // Test null title
        val nullTitleNotification = NotificationData(
            packageName = "com.VCB",
            title = null,
            text = "Test Message",
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val nullTitleUrls = filterEngine.findMatchingUrls(nullTitleNotification)
        assertEquals(1, nullTitleUrls.size)
        assertEquals("Bank apps", nullTitleUrls[0].name)

        // Test null text
        val nullTextNotification = NotificationData(
            packageName = "com.VCB",
            title = "Security Alert",
            text = null,
            timestamp = System.currentTimeMillis(),
            id = 1,
            tag = null
        )
        val nullTextUrls = filterEngine.findMatchingUrls(nullTextNotification)
        assertEquals(1, nullTextUrls.size)
        assertEquals("Bank apps", nullTextUrls[0].name)
    }
}