package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.BuildConfig

data class WebhookConfig(
    val urls: List<WebhookUrl>, val ignoredPackages: List<Regex>
)

data class WebhookUrl(
    val url: String, val name: String, val packages: List<Regex>
)

object DefaultWebhookConfig {
    val config = WebhookConfig(
        urls = listOf(
            WebhookUrl(
                url = BuildConfig.WEBHOOK_URL_BANK, name = "Bank apps", packages = listOf(
                    Regex.fromLiteral("com.mservice.momotransfer"),
                    Regex.fromLiteral("com.VCB"),
                    Regex.fromLiteral("com.vib.myvib2"),
                    Regex.fromLiteral("vn.com.techcombank.bb.app"),
                )
            ),
        ), ignoredPackages = listOf(
            // chat
            Regex.fromLiteral("com.discord"),
            Regex.fromLiteral("com.facebook.orca"),
            Regex.fromLiteral("com.Slack"),
            Regex.fromLiteral("org.telegram.messenger"),
            Regex.fromLiteral("com.whatsapp"),
            Regex.fromLiteral("com.zing.zalo"),
            // social
            Regex.fromLiteral("com.facebook.katana"),
            Regex("^com\\.instagram.*"),
            Regex.fromLiteral("com.linkedin.android"),
            // system
            Regex.fromLiteral("android"),
            Regex("^com\\.android.*"),
            Regex("^com\\.google.*"),
            Regex("^com\\.osp.*"),
            Regex("^com\\.samsung.*"),
            Regex("^com\\.sec.*"),
            // others
            Regex.fromLiteral("com.grabtaxi.passenger"),
            Regex("^com.netflix.*"),
            Regex.fromLiteral("com.openai.chatgpt"),
        )
    )
}