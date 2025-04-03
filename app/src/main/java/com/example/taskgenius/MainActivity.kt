package com.example.taskgenius

import Charts
import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.app.AlarmManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskgenius.data.local.TaskDatabase
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.repository.TaskRepository
import com.example.taskgenius.factory.TaskViewModelFactory
import com.example.taskgenius.ui.screens.CategoryTasksScreen
import com.example.taskgenius.ui.screens.NewTaskScreen
import com.example.taskgenius.ui.screens.TaskScreen
import com.example.taskgenius.viewmodel.TaskViewModel
import com.google.gson.Gson

class MainActivity : ComponentActivity() {
    private val REQUEST_PERMISSIONS = 1
    private lateinit var taskViewModel: TaskViewModel
    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.RECORD_AUDIO
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS) // Required for Android 13+
        }
    }.toTypedArray()

    override fun onStart() {
        super.onStart()
        if (!hasAllPermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_PERMISSIONS)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasExactAlarmPermission()) {
            requestExactAlarmPermission()
        }
    }

    private fun hasAllPermissions(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun hasExactAlarmPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManagerCompat.canScheduleExactAlarms(getSystemService(ALARM_SERVICE) as AlarmManager)
        } else {
            true
        }
    }


    private fun requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(
                Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val deniedPermissions = permissions.zip(grantResults.toList())
                .filter { it.second != PackageManager.PERMISSION_GRANTED }
                .map { it.first }

            if (deniedPermissions.isEmpty()) {
                Toast.makeText(this, "All permissions granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Some permissions denied: ${deniedPermissions.joinToString()}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel(applicationContext)
        val taskDao = TaskDatabase.getInstance(applicationContext).taskDao()
        val factory = TaskViewModelFactory(TaskRepository(taskDao))
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        setContent {
            val navController = rememberNavController()
            val tasks = taskViewModel.tasks.collectAsState().value

            NavHost(navController = navController, startDestination = "task_screen") {
                composable("task_screen") {
                    TaskScreen(
                        tasks = tasks,
                        onTaskClick = { task ->
                            val taskJson = Gson().toJson(task)
                            navController.navigate("task_details/$taskJson")
                        },
                        onTaskStatusChange = { taskId, newStatus ->
                            taskViewModel.updateTaskStatus(taskId, newStatus)
                        },
                        onTaskAdd = { task ->
                            taskViewModel.addTask(task)
                        },
                        onNavigateToNewTask = {
                            navController.navigate("new_task")
                        }
                        ,
                        onCardClick = { category ->
                            navController.navigate("category_tasks/$category")
                        }
                    )
                }

                composable("new_task") {
                    NewTaskScreen(
                        onTaskAdded = { task ->
                            taskViewModel.addTask(task)
                            navController.navigate("task_screen")
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable("task_details/{taskJson}") { backStackEntry ->
                    val taskJson = backStackEntry.arguments?.getString("taskJson")
                    val task = taskJson?.let { Gson().fromJson(it, TaskEntity::class.java) }

                    task?.let {
                        Charts(task = it)
                    }
                }
                composable("category_tasks/{category}") { backStackEntry ->
                    val category = backStackEntry.arguments?.getString("category") ?: ""
                    CategoryTasksScreen(
                        category = category,
                        onTaskClick = { task ->
                            val taskJson = Gson().toJson(task)
                            navController.navigate("task_details/$taskJson")
                        },
                        viewModel = taskViewModel
                    )
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Task Channel"
        val descriptionText = "Channel for task notifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel("task_channel", name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
