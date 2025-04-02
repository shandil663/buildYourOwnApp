package com.example.taskgenius

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.taskgenius.data.local.TaskDatabase
import com.example.taskgenius.data.repository.TaskRepository
import com.example.taskgenius.factory.TaskViewModelFactory
import com.example.taskgenius.ui.screens.NewTaskScreen
import com.example.taskgenius.ui.screens.TaskScreen
import com.example.taskgenius.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 1
    private lateinit var taskViewModel: TaskViewModel

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            val message = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                "Permission granted, you can now use the microphone."
            } else {
                "Permission denied, cannot use the microphone."
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        onTaskClick = { task -> showToast("Clicked: ${task.category}") },
                        onTaskStatusChange = { id, status -> taskViewModel.updateTaskStatus(id, status) },
                        onTaskAdd = { task -> taskViewModel.addTask(task) },
                        onNavigateToNewTask = { navController.navigate("new_task") }
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
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
