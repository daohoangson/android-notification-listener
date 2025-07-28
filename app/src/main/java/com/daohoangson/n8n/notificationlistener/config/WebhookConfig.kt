package com.daohoangson.n8n.notificationlistener.config

import com.daohoangson.n8n.notificationlistener.BuildConfig

data class WebhookConfig(
    val urls: List<WebhookUrl>, val ignoredPackages: List<Regex>
)

data class WebhookUrl(
    val url: String, val name: String, val rules: List<FilterRule>
)

data class FilterRule(
    val packageName: Regex? = null, val title: Regex? = null, val text: Regex? = null
)

object DefaultWebhookConfig {
    val config = WebhookConfig(
        urls = listOf(
            WebhookUrl(
                url = BuildConfig.WEBHOOK_URL_BANK, name = "Bank apps", rules = listOf(
                    FilterRule(packageName = Regex.fromLiteral("com.VCB")),
                    FilterRule(packageName = Regex.fromLiteral("com.vib.myvib2")),
                    FilterRule(packageName = Regex.fromLiteral("vn.com.techcombank.bb.app"))
                )
            ),
            WebhookUrl(
                url = BuildConfig.WEBHOOK_URL_CHAT, name = "Chat apps", rules = listOf(
                    FilterRule(packageName = Regex.fromLiteral("com.discord")),
                    FilterRule(packageName = Regex.fromLiteral("com.facebook.orca")),
                    FilterRule(packageName = Regex.fromLiteral("com.Slack")),
                    FilterRule(packageName = Regex.fromLiteral("org.telegram.messenger")),
                    FilterRule(packageName = Regex.fromLiteral("com.zing.zalo"))
                )
            ),
        ), ignoredPackages = listOf(
            Regex("^com\\.android.*"),
            Regex("^com\\.google.*"),
            Regex("^com\\.samsung.*"),
            Regex("^com\\.sec.*")
        )
    )
}