package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFilterEngine @Inject constructor() {
    
    private val config = DefaultWebhookConfig.config
    
    fun isIgnored(notificationData: NotificationData): Boolean {
        return config.ignoredPackages.contains(notificationData.packageName)
    }
    
    fun findMatchingUrls(notificationData: NotificationData): List<WebhookUrl> {
        return config.urls.filter { webhookUrl ->
            webhookUrl.rules.any { rule ->
                matchesRule(notificationData, rule)
            }
        }
    }
    
    private fun matchesRule(notificationData: NotificationData, rule: FilterRule): Boolean {
        if (rule.packageName != notificationData.packageName) {
            return false
        }
        
        if (rule.titleRegex == null && rule.textRegex == null) {
            return true
        }
        
        rule.titleRegex?.let { titleRegex ->
            val title = notificationData.title ?: ""
            if (!titleRegex.matches(title)) {
                return false
            }
        }
        
        rule.textRegex?.let { textRegex ->
            val text = notificationData.text ?: ""
            if (!textRegex.matches(text)) {
                return false
            }
        }
        
        return true
    }
    
}