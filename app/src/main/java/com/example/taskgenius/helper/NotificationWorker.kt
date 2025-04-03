package com.example.taskgenius.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.taskgenius.R

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val taskTitle = inputData.getString("TASK_TITLE") ?: "Task Reminder"
        val taskTime = inputData.getString("TASK_TIME") ?: ""

        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("task_channel", "Task Channel", NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, "task_channel")
            .setSmallIcon(R.drawable.bars)
            .setContentTitle(taskTitle)
            .setContentText(taskTime)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskTitle.hashCode(), notification)

        return Result.success()
    }
}