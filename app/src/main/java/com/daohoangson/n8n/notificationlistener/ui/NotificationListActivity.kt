package com.daohoangson.n8n.notificationlistener.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.compose.runtime.rememberCoroutineScope
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListScreen(repository: NotificationRepository) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var failedNotifications by remember { mutableStateOf<List<FailedNotification>>(emptyList()) }
    var undecidedNotifications by remember { mutableStateOf<List<UndecidedNotification>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showUrlSelectionDialog by remember { mutableStateOf(false) }
    var selectedUndecidedNotification by remember { mutableStateOf<UndecidedNotification?>(null) }
    val coroutineScope = rememberCoroutineScope()
    
    fun reloadNotifications() {
        coroutineScope.launch {
            loadNotifications(repository) { failed, undecided ->
                failedNotifications = failed
                undecidedNotifications = undecided
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadNotifications(repository) { failed, undecided ->
            failedNotifications = failed
            undecidedNotifications = undecided
            isLoading = false
        }
    }
    
    if (showUrlSelectionDialog && selectedUndecidedNotification != null) {
        UrlSelectionDialog(
            notification = selectedUndecidedNotification!!,
            onDismiss = { 
                showUrlSelectionDialog = false
                selectedUndecidedNotification = null
            },
            onUpload = { notification, url ->
                showUrlSelectionDialog = false
                selectedUndecidedNotification = null
                reloadNotifications()
            },
            repository = repository
        )
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
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            selectedTabIndex == 0 -> {
                FailedNotificationsList(
                    notifications = failedNotifications,
                    onRetry = { reloadNotifications() },
                    onDelete = { reloadNotifications() },
                    onDeleteAll = { reloadNotifications() },
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
                    onDelete = { reloadNotifications() },
                    onDeleteAll = { reloadNotifications() },
                    repository = repository
                )
            }
        }
    }
}

@Composable
fun <T> GenericNotificationsList(
    notifications: List<T>,
    onDeleteAll: () -> Unit,
    repository: NotificationRepository,
    deleteAction: suspend (T) -> Unit,
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
                            notifications.forEach { notification ->
                                deleteAction(notification)
                            }
                            onDeleteAll()
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
                        onDeleteAll()
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
    onRetry: (FailedNotification) -> Unit,
    onDelete: (FailedNotification) -> Unit,
    onDeleteAll: () -> Unit,
    repository: NotificationRepository
) {
    val coroutineScope = rememberCoroutineScope()
    
    GenericNotificationsList(
        notifications = notifications,
        onDeleteAll = onDeleteAll,
        repository = repository,
        deleteAction = { repository.deleteFailedNotification(it) }
    ) { notification, onDeleteSingle ->
        FailedNotificationCard(
            notification = notification,
            onRetry = {
                coroutineScope.launch {
                    repository.retryFailedNotification(notification)
                    onRetry(notification)
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
    onDelete: (UndecidedNotification) -> Unit,
    onDeleteAll: () -> Unit,
    repository: NotificationRepository
) {
    GenericNotificationsList(
        notifications = notifications,
        onDeleteAll = onDeleteAll,
        repository = repository,
        deleteAction = { repository.deleteUndecidedNotification(it) }
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

private suspend fun loadNotifications(
    repository: NotificationRepository,
    onLoaded: (List<FailedNotification>, List<UndecidedNotification>) -> Unit
) {
    val failedNotifications = repository.getAllFailedNotifications()
    val undecidedNotifications = repository.getAllUndecidedNotifications()
    onLoaded(failedNotifications, undecidedNotifications)
}