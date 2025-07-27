package com.daohoangson.n8n.notificationlistener.di

import android.content.Context
import com.daohoangson.n8n.notificationlistener.data.database.AppDatabase
import com.daohoangson.n8n.notificationlistener.data.database.FailedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.database.UndecidedNotificationDao
import com.daohoangson.n8n.notificationlistener.data.repository.NotificationRepository
import com.daohoangson.n8n.notificationlistener.config.NotificationFilterEngine
import com.daohoangson.n8n.notificationlistener.network.NetworkModule
import com.daohoangson.n8n.notificationlistener.network.WebhookApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    @Provides
    fun provideFailedNotificationDao(database: AppDatabase): FailedNotificationDao {
        return database.failedNotificationDao()
    }

    @Provides
    fun provideUndecidedNotificationDao(database: AppDatabase): UndecidedNotificationDao {
        return database.undecidedNotificationDao()
    }

    @Provides
    @Singleton  
    fun provideWebhookApi(): WebhookApi {
        return NetworkModule.webhookApi
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        @ApplicationContext context: Context,
        webhookApi: WebhookApi,
        failedNotificationDao: FailedNotificationDao,
        undecidedNotificationDao: UndecidedNotificationDao
    ): NotificationRepository {
        return NotificationRepository(context, webhookApi, failedNotificationDao, undecidedNotificationDao)
    }
}