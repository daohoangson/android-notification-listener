package com.daohoangson.n8n.notificationlistener.config

data class WebhookConfig(
    val urls: List<WebhookUrl>,
    val ignoredPackages: List<String>
)

data class WebhookUrl(
    val url: String,
    val name: String,
    val rules: List<FilterRule>
)

data class FilterRule(
    val packageName: String,
    val titleRegex: Regex? = null,
    val textRegex: Regex? = null
)

object DefaultWebhookConfig {
    val config = WebhookConfig(
        urls = listOf(
            WebhookUrl(
                url = "https://n8n.cloud/webhook/slack-notifications",
                name = "Slack Notifications",
                rules = listOf(
                    FilterRule(packageName = "com.slack"),
                    FilterRule(packageName = "com.microsoft.teams")
                )
            ),
            WebhookUrl(
                url = "https://n8n.cloud/webhook/social-media",
                name = "Social Media",
                rules = listOf(
                    FilterRule(packageName = "com.instagram.android"),
                    FilterRule(packageName = "com.twitter.android"),
                    FilterRule(
                        packageName = "com.facebook.katana",
                        titleRegex = ".*mentioned you.*".toRegex()
                    )
                )
            ),
            WebhookUrl(
                url = "https://n8n.cloud/webhook/urgent-alerts",
                name = "Urgent Alerts",
                rules = listOf(
                    FilterRule(
                        packageName = "com.android.phone",
                        titleRegex = ".*Emergency.*".toRegex()
                    ),
                    FilterRule(
                        packageName = "com.banking.app",
                        textRegex = ".*fraud.*|.*suspicious.*".toRegex()
                    )
                )
            )
        ),
        ignoredPackages = listOf(
            "com.android.systemui",
            "com.google.android.gms",
            "com.android.providers.downloads"
        )
    )
}