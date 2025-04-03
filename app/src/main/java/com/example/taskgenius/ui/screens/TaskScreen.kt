package com.example.taskgenius.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.taskgenius.R
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.utils.SpeechRecognizerHelper
import com.example.taskgenius.utils.TaskAIHelper
import kotlinx.coroutines.launch

@Composable
fun TaskScreen(
    tasks: List<TaskEntity>,
    onTaskClick: (TaskEntity) -> Unit,
    onTaskStatusChange: (Int, TaskStatus) -> Unit,
    onTaskAdd: (TaskEntity) -> Unit,
    onNavigateToNewTask: () -> Unit,
    onCardClick: (String)-> Unit
) {
    val categories = listOf(
        "Professional" to R.drawable.bars,
        "Personal" to R.drawable.bars,
        "Household" to R.drawable.bars,
        "Social" to R.drawable.bars,
        "Wellness" to R.drawable.bars,
        "General" to R.drawable.bars
    )

    var speechInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizerHelper(context) { speechInput = it } }
    val coroutineScope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp, start = 16.dp, end = 16.dp, bottom = 80.dp)
        ) {
            Text(
                text = "Task Categories",
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(categories) { (title, imageRes) ->
                    CategoryCard(title, imageRes) {
                        onCardClick(title)
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    isListening = true
                    Toast.makeText(context, "Listening for your task...", Toast.LENGTH_SHORT).show()
                    speechRecognizer.startListening()
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(imageVector = Icons.Default.Build, contentDescription = "Speak Task")
            }

            FloatingActionButton(
                onClick = onNavigateToNewTask,
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
            }
        }

        if (speechInput.isNotEmpty()) {
            LaunchedEffect(speechInput) {
                coroutineScope.launch {
                    Toast.makeText(context, "Got the text: $speechInput", Toast.LENGTH_SHORT).show()
                    val generatedTask = TaskAIHelper.generateTaskFromInput(speechInput, context)
                    Toast.makeText(context, "Generated task: ${generatedTask.title}", Toast.LENGTH_SHORT).show()
                    onTaskAdd(generatedTask)
                    Toast.makeText(context, "AI Task Added!", Toast.LENGTH_SHORT).show()
                    speechInput = ""
                    isListening = false
                }
            }
        }
    }
}

@Composable
fun CategoryCard(title: String, imageRes: Int, onClick: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFB0BEC5),
                        Color(0xFF455A64)
                    )
                )
            )
            .clickable { onClick(title) },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = title,
                modifier = Modifier.size(60.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
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
