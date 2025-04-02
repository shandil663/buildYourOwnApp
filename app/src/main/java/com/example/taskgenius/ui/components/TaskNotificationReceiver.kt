package com.example.taskgenius.ui.components

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.taskgenius.R

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return

        val taskTitle = intent?.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskTime = intent?.getStringExtra("TASK_TIME") ?: ""

        val notificationManager = ContextCompat.getSystemService(context, NotificationManager::class.java)
                as NotificationManager

        val notification = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.bars)
            .setContentTitle(taskTitle)
            .setContentText(taskTime)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskTitle.hashCode(), notification)
    }
}
