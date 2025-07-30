package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.utils.NotificationData
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
        return config.urls.filter {
            it.packages.any { regex ->
                regex.matches(notificationData.packageName)
            }
        }
    }
}