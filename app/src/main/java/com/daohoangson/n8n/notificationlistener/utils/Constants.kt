package com.daohoangson.n8n.notificationlistener.utils

import com.daohoangson.n8n.notificationlistener.BuildConfig

object Constants {
    val WEBHOOK_BASE_URL: String = BuildConfig.WEBHOOK_BASE_URL
    val WEBHOOK_PATH: String = BuildConfig.WEBHOOK_PATH
    const val DATABASE_NAME = "notification_listener_db"
    const val DATABASE_VERSION = 2
    const val NETWORK_TIMEOUT_SECONDS = 30L
}