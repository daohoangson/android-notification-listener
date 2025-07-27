package com.daohoangson.n8n.notificationlistener

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.ui.NotificationListActivity
import com.daohoangson.n8n.notificationlistener.ui.theme.MyApplicationTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var repository: NotificationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationListenerApp(
                        modifier = Modifier.padding(innerPadding),
                        repository = repository,
                        onOpenNotificationSettings = { openNotificationListenerSettings() },
                        onOpenNotificationList = { openNotificationListActivity() },
                        isPermissionGranted = isNotificationListenerEnabled()
                    )
                }
            }
        }
    }
    
    private fun isNotificationListenerEnabled(): Boolean {
        val packageName = packageName
        val enabledListeners = Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )
        return enabledListeners?.contains(packageName) == true
    }
    
    private fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)
    }
    
    private fun openNotificationListActivity() {
        val intent = Intent(this, NotificationListActivity::class.java)
        startActivity(intent)
    }
    
}

@Composable
fun NotificationListenerApp(
    modifier: Modifier = Modifier,
    repository: NotificationRepository,
    onOpenNotificationSettings: () -> Unit,
    onOpenNotificationList: () -> Unit,
    isPermissionGranted: Boolean
) {
    var failedNotificationCount by remember { mutableIntStateOf(0) }
    var undecidedNotificationCount by remember { mutableIntStateOf(0) }
    var isRetrying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check notification counts
    LaunchedEffect(Unit) {
        failedNotificationCount = repository.getFailedNotificationCount()
        undecidedNotificationCount = repository.getUndecidedNotificationCount()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Notification Listener",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Permission status
        Text(
            text = if (isPermissionGranted) "✅ Notification access granted" else "❌ Notification access required",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Enable notification access button
        Button(
            onClick = onOpenNotificationSettings,
            enabled = !isPermissionGranted
        ) {
            Text("Enable Notification Access")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Notification counts
        Text(
            text = "Failed notifications: $failedNotificationCount",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "Undecided notifications: $undecidedNotificationCount",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Manage Notifications button
        Button(
            onClick = onOpenNotificationList,
            enabled = (failedNotificationCount > 0 || undecidedNotificationCount > 0)
        ) {
            Text("Manage Notifications")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Legacy retry all button (for backward compatibility)
        Button(
            onClick = {
                coroutineScope.launch {
                    isRetrying = true
                    val success = repository.retryFailedNotifications()
                    if (success) {
                        failedNotificationCount = repository.getFailedNotificationCount()
                    }
                    isRetrying = false
                }
            },
            enabled = failedNotificationCount > 0 && !isRetrying
        ) {
            Text(if (isRetrying) "Retrying..." else "Retry All Failed")
        }
    }
}