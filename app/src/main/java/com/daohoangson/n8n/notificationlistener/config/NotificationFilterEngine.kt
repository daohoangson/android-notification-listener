package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationData
import org.apache.commons.lang3.StringUtils
import java.text.Normalizer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationFilterEngine @Inject constructor() {

    private val config = DefaultWebhookConfig.config

    fun isIgnored(notificationData: NotificationData): Boolean {
        return config.ignoredPackages.any { regex ->
            regex.matches(notificationData.packageName)
        }
    }

    fun findMatchingUrls(notificationData: NotificationData): List<WebhookUrl> {
        return config.urls.filter { webhookUrl ->
            webhookUrl.rules.any { rule ->
                matchesRule(notificationData, rule)
            }
        }
    }

    private fun matchesRule(notificationData: NotificationData, rule: FilterRule): Boolean {
        rule.packageName?.let {
            if (!it.matches(notificationData.packageName)) {
                return false
            }
        }

        rule.text?.let {
            val str = StringUtils.stripAccents(notificationData.text ?: "")
            if (!it.matches(str)) {
                return false
            }
        }

        return true
    }

}