package com.daohoangson.n8n.notificationlistener.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.daohoangson.n8n.notificationlistener.config.DefaultWebhookConfig
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotification
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotification
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationListActivity : ComponentActivity() {
    
    @Inject
    lateinit var repository: NotificationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MyApplicationTheme {
                NotificationListScreen(repository)
            }
        }
    }
}

@Composable
fun NotificationListScreen(repository: NotificationRepository) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val failedNotifications by repository.getAllFailedNotificationsFlow().collectAsState(initial = emptyList())
    val undecidedNotifications by repository.getAllUndecidedNotificationsFlow().collectAsState(initial = emptyList())
    var showUrlSelectionDialog by remember { mutableStateOf(false) }
    var selectedUndecidedNotification by remember { mutableStateOf<UndecidedNotification?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    if (showUrlSelectionDialog) {
        selectedUndecidedNotification?.let { notification ->
            UrlSelectionDialog(
                notification = notification,
                onDismiss = { 
                    showUrlSelectionDialog = false
                    selectedUndecidedNotification = null
                },
                onUpload = { uploadedNotification, url ->
                    showUrlSelectionDialog = false
                    selectedUndecidedNotification = null
                },
                repository = repository
            )
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Notification Management",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        TabRow(selectedTabIndex = selectedTabIndex) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Failed (${failedNotifications.size})") }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Undecided (${undecidedNotifications.size})") }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when (selectedTabIndex) {
            0 -> {
                FailedNotificationsList(
                    notifications = failedNotifications,
                    repository = repository
                )
            }
            else -> {
                UndecidedNotificationsList(
                    notifications = undecidedNotifications,
                    onUpload = { notification ->
                        selectedUndecidedNotification = notification
                        showUrlSelectionDialog = true
                    },
                    repository = repository
                )
            }
        }
    }
}

@Composable
fun <T> GenericNotificationsList(
    notifications: List<T>,
    deleteAction: suspend (T) -> Unit,
    bulkDeleteAction: suspend (List<T>) -> Unit,
    itemContent: @Composable (T, () -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    
    Column {
        if (notifications.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { 
                        coroutineScope.launch {
                            bulkDeleteAction(notifications)
                        }
                    }
                ) {
                    Text("Delete All")
                }
            }
        }
        
        LazyColumn {
            items(notifications) { notification ->
                itemContent(notification) {
                    coroutineScope.launch {
                        deleteAction(notification)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FailedNotificationsList(
    notifications: List<FailedNotification>,
    repository: NotificationRepository
) {
    val coroutineScope = rememberCoroutineScope()
    
    GenericNotificationsList(
        notifications = notifications,
        deleteAction = { repository.deleteFailedNotification(it) },
        bulkDeleteAction = { repository.deleteFailedNotifications(it) }
    ) { notification, onDeleteSingle ->
        FailedNotificationCard(
            notification = notification,
            onRetry = {
                coroutineScope.launch {
                    repository.retryFailedNotification(notification)
                }
            },
            onDelete = onDeleteSingle
        )
    }
}

@Composable
fun UndecidedNotificationsList(
    notifications: List<UndecidedNotification>,
    onUpload: (UndecidedNotification) -> Unit,
    repository: NotificationRepository
) {
    GenericNotificationsList(
        notifications = notifications,
        deleteAction = { repository.deleteUndecidedNotification(it) },
        bulkDeleteAction = { repository.deleteUndecidedNotifications(it) }
    ) { notification, onDeleteSingle ->
        UndecidedNotificationCard(
            notification = notification,
            onUpload = { onUpload(notification) },
            onDelete = onDeleteSingle
        )
    }
}

object DateFormatters {
    val notificationTime: java.time.format.DateTimeFormatter = 
        java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm")
}

@Composable
fun NotificationDisplayContent(
    packageName: String,
    title: String?,
    text: String?,
    timestamp: Long,
    additionalContent: @Composable () -> Unit = {},
    actions: @Composable () -> Unit
) {
    Text(
        text = "Package: $packageName",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold
    )
    title?.let {
        Text(
            text = "Title: $it",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
    text?.let {
        Text(
            text = "Text: $it",
            style = MaterialTheme.typography.bodySmall,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
    additionalContent()
    Text(
        text = "Time: ${java.time.Instant.ofEpochMilli(timestamp).atZone(java.time.ZoneId.systemDefault()).format(DateFormatters.notificationTime)}",
        style = MaterialTheme.typography.bodySmall
    )
    
    Spacer(modifier = Modifier.height(8.dp))
    actions()
}

@Composable
fun FailedNotificationCard(
    notification: FailedNotification,
    onRetry: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            NotificationDisplayContent(
                packageName = notification.packageName,
                title = notification.title,
                text = notification.text,
                timestamp = notification.timestamp,
                additionalContent = {
                    Text(
                        text = "Webhook: ${notification.webhookName}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    notification.errorMessage?.let {
                        Text(
                            text = "Error: $it",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                        OutlinedButton(onClick = onDelete) {
                            Text("Delete")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun UndecidedNotificationCard(
    notification: UndecidedNotification,
    onUpload: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            NotificationDisplayContent(
                packageName = notification.packageName,
                title = notification.title,
                text = notification.text,
                timestamp = notification.timestamp,
                additionalContent = {
                    Text(
                        text = "Reason: ${notification.reason}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(onClick = onUpload) {
                            Text("Upload")
                        }
                        OutlinedButton(onClick = onDelete) {
                            Text("Delete")
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun UrlSelectionDialog(
    notification: UndecidedNotification,
    onDismiss: () -> Unit,
    onUpload: (UndecidedNotification, String) -> Unit,
    repository: NotificationRepository
) {
    val availableUrls = DefaultWebhookConfig.config.urls
    val coroutineScope = rememberCoroutineScope()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Webhook URL") },
        text = {
            Column {
                Text("Choose a webhook URL to send this notification to:")
                Spacer(modifier = Modifier.height(8.dp))
                availableUrls.forEach { webhookUrl ->
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.uploadUndecidedNotification(notification, webhookUrl.url)
                                onUpload(notification, webhookUrl.url)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = webhookUrl.name,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
