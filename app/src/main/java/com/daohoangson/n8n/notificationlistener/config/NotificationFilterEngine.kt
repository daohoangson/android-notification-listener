package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationDataExtractor
import javax.inject.Inject
import javax.inject.Singleton

data class NotificationData(
    val packageName: String,
    val title: String?,
    val text: String?
)

@Singleton
class NotificationFilterEngine @Inject constructor() {
    
    private val config = DefaultWebhookConfig.config
    
    fun isIgnored(packageName: String): Boolean {
        return config.ignoredPackages.contains(packageName)
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
    
    fun extractNotificationData(jsonPayload: String): NotificationData {
        val gson = com.google.gson.Gson()
        val jsonObject = gson.fromJson(jsonPayload, com.google.gson.JsonObject::class.java)
        
        return NotificationData(
            packageName = jsonObject.get("packageName")?.takeIf { !it.isJsonNull }?.asString ?: "",
            title = jsonObject.get("title")?.takeIf { !it.isJsonNull }?.asString,
            text = jsonObject.get("text")?.takeIf { !it.isJsonNull }?.asString
        )
    }
}