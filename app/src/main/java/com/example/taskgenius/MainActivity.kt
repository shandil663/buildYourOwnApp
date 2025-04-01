package com.example.taskgenius

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.taskgenius.data.local.TaskDatabase
import com.example.taskgenius.data.repository.TaskRepository
import com.example.taskgenius.factory.TaskViewModelFactory
import android.Manifest
import com.example.taskgenius.ui.screens.TaskScreen
import com.example.taskgenius.viewmodel.TaskViewModel

class MainActivity : ComponentActivity() {
    private val REQUEST_RECORD_AUDIO_PERMISSION = 1

    override fun onStart() {
        super.onStart()

        if (true &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Permission granted, you can now use the microphone.", Toast.LENGTH_SHORT).show()
            } else {

                Toast.makeText(this, "Permission denied, cannot use the microphone.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val taskDao = TaskDatabase.getInstance(applicationContext).taskDao()
        val factory = TaskViewModelFactory(TaskRepository(taskDao))
        taskViewModel = ViewModelProvider(this, factory).get(TaskViewModel::class.java)

        setContent {
            TaskScreen(
                tasks = taskViewModel.tasks.collectAsState().value,
                onTaskClick = { task -> showToast("Clicked: ${task.category}") },
                onTaskStatusChange = { id, status -> taskViewModel.updateTaskStatus(id, status) },
                onTaskAdd = { task -> taskViewModel.addTask(task) }
            )
        }

    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
