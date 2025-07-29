package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.BuildConfig

data class WebhookConfig(
    val urls: List<WebhookUrl>, val ignoredPackages: List<Regex>
)

data class WebhookUrl(
    val url: String, val name: String, val rules: List<FilterRule>
)

data class FilterRule(
    val packageName: Regex? = null, val text: Regex? = null
)

object DefaultWebhookConfig {
    val config = WebhookConfig(
        urls = listOf(
            WebhookUrl(
                url = BuildConfig.WEBHOOK_URL_BANK, name = "Bank apps", rules = listOf(
                    FilterRule(packageName = Regex.fromLiteral("com.mservice.momotransfer")),
                    FilterRule(packageName = Regex.fromLiteral("com.VCB")),
                    FilterRule(packageName = Regex.fromLiteral("com.vib.myvib2")),

                    FilterRule(
                        packageName = Regex.fromLiteral("vn.com.techcombank.bb.app"),
                        text = Regex(".*Han muc kha dung.*")
                    ),
                    FilterRule(
                        packageName = Regex.fromLiteral("vn.com.techcombank.bb.app"),
                        text = Regex(".*So du.*")
                    )
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
            Regex.fromLiteral("com.instagram.barcelona"),
            Regex.fromLiteral("com.linkedin.android"),
            // system
            Regex.fromLiteral("android"),
            Regex("^com\\.android.*"),
            Regex("^com\\.google.*"),
            Regex("^com\\.samsung.*"),
            Regex("^com\\.sec.*"),
            // others
            Regex.fromLiteral("com.grabtaxi.passenger"),
            Regex("^com.netflix.*"),
            Regex.fromLiteral("com.openai.chatgpt"),
        )
    )
}