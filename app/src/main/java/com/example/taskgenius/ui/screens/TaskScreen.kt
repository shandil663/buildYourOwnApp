package com.example.taskgenius.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import android.widget.Toast
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalContext

import com.example.taskgenius.utils.SpeechRecognizerHelper
import com.example.taskgenius.utils.TaskAIHelper
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(
    tasks: List<TaskEntity>,
    onTaskClick: (TaskEntity) -> Unit,
    onTaskStatusChange: (Int, TaskStatus) -> Unit,
    onTaskAdd: (TaskEntity) -> Unit,
    onNavigateToNewTask: () -> Unit
) {
    var speechInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizerHelper(context) { speechInput = it } }
    val coroutineScope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 16.dp, top = 50.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Your Tasks",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Button(onClick = {
            isListening = true
            Toast.makeText(context, "Listening for your task...", Toast.LENGTH_SHORT).show()
            speechRecognizer.startListening()
        }) {
            Text("🎙 Speak Your Task")
        }

        if (speechInput.isNotEmpty()) {
            LaunchedEffect(speechInput) {
                coroutineScope.launch {
                    Toast.makeText(context, "Got the text: $speechInput", Toast.LENGTH_SHORT).show()
                    val generatedTask = TaskAIHelper.generateTaskFromInput(speechInput,context)
                    Toast.makeText(context, "Generated task: ${generatedTask.title}", Toast.LENGTH_SHORT).show()

                    onTaskAdd(generatedTask)
                    Toast.makeText(context, "AI Task Added!", Toast.LENGTH_SHORT).show()
                    speechInput = ""
                    isListening = false
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToNewTask,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("➕ Add New Task")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(tasks) { task ->
                TaskItem(task, onTaskClick, onTaskStatusChange)
            }
        }
    }
}


@Composable
fun TaskItem(
    task: TaskEntity,
    onTaskClick: (TaskEntity) -> Unit,
    onTaskStatusChange: (Int, TaskStatus) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onTaskClick(task) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Due: ${task.dueAt ?: "No deadline"}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }

            Button(
                onClick = { onTaskStatusChange(task.id, TaskStatus.COMPLETED) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
            ) {
                Text("Complete")
            }
        }
    }
}


