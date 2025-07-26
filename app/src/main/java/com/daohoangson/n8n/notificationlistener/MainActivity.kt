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
import com.daohoangson.n8n.notificationlistener.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var repository: NotificationRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = NotificationRepository(this)
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NotificationListenerApp(
                        modifier = Modifier.padding(innerPadding),
                        repository = repository,
                        onOpenNotificationSettings = { openNotificationListenerSettings() },
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
    
    override fun onResume() {
        super.onResume()
        // Refresh UI state when returning from settings
    }
}

@Composable
fun NotificationListenerApp(
    modifier: Modifier = Modifier,
    repository: NotificationRepository,
    onOpenNotificationSettings: () -> Unit,
    isPermissionGranted: Boolean
) {
    var failedNotificationCount by remember { mutableIntStateOf(0) }
    var isRetrying by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    
    // Check failed notification count
    LaunchedEffect(Unit) {
        failedNotificationCount = repository.getFailedNotificationCount()
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
        
        // Failed notifications status
        Text(
            text = "Failed notifications: $failedNotificationCount",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Upload failed notifications button
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
            Text(if (isRetrying) "Uploading..." else "Upload Failed Notifications")
        }
    }
}