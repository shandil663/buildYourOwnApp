package com.example.taskgenius.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.taskgenius.R
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.utils.SpeechRecognizerHelper
import com.example.taskgenius.utils.TaskAIHelper
import kotlinx.coroutines.launch
import kotlin.math.pow

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
        "Professional" to R.drawable.prof,
        "Personal" to R.drawable.personal,
        "Household" to R.drawable.house,
        "Social" to R.drawable.social,
        "Wellness" to R.drawable.well,
        "General" to R.drawable.gen
    )
    var showDialog by remember { mutableStateOf(false) }
    var speechInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val speechRecognizer = remember { SpeechRecognizerHelper(context) { speechInput = it } }
    val coroutineScope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize().background(colorResource(id = R.color.white1))
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
                    showDialog=true
                    Toast.makeText(context, "Listening for your task...", Toast.LENGTH_SHORT).show()
                    speechRecognizer.startListening()
                },
                containerColor = Color(0xFFE18181)
            ) {
                Icon( painter = painterResource(id = R.drawable.ai), contentDescription = "Speak Task")
            }

            FloatingActionButton(
                onClick = onNavigateToNewTask,
                containerColor = Color(0xFFE18181)
            ) {
                Icon( painter = painterResource(id = R.drawable.plus), contentDescription = "Add Task")
            }
        }



        LaunchedEffect(speechInput) {
            if (speechInput.isNotEmpty()) {
                showDialog = true

                coroutineScope.launch {
                    Toast.makeText(context, "Got the text: $speechInput", Toast.LENGTH_SHORT).show()
                    val generatedTask = TaskAIHelper.generateTaskFromInput(speechInput, context)
                    Toast.makeText(context, "Generated task: ${generatedTask.title}", Toast.LENGTH_SHORT).show()
                    onTaskAdd(generatedTask)
                    Toast.makeText(context, "AI Task Added!", Toast.LENGTH_SHORT).show()
                    showDialog = false
                    speechInput = ""
                    isListening = false
                }
            }
        }

        if (showDialog) {
            AIProcessingDialog(showDialog = true) { showDialog = false }
        }



    }}

@Composable
fun CategoryCard(title: String, imageRes: Int, onClick: (String) -> Unit) {
    val categoryGradients = mapOf(
        "Professional" to arrayOf(0.0f to Color(0xFFBBDEFB), 0.8f to Color(0xFF4F8ABE), 1.0f to Color(0xFF438ED0)),
        "Personal" to arrayOf(0.0f to Color(0xFFE1BEE7), 0.8f to Color(0xFF7B1FA2), 1.0f to Color(0xFF7B1FA2)),
        "Social" to arrayOf(0.0f to Color(0xFFFFCCBC), 0.8f to Color(0xFFD84315), 1.0f to Color(0xFFD84315)),
        "Wellness" to arrayOf(0.0f to Color(0xFFC8E6C9), 0.8f to Color(0xFF388E3C), 1.0f to Color(0xFF388E3C)),
        "Household" to arrayOf(0.0f to Color(0xFFFFF9C4), 0.8f to Color(0xFFFBC02D), 1.0f to Color(0xFFFBC02D))
    )

    val backgroundGradient = Brush.verticalGradient(
        colorStops = categoryGradients[title] ?: arrayOf(
            0.0f to Color(0xFFCFD8DC), 0.8f to Color(0xFF455A64), 1.0f to Color(0xFF455A64)
        )
    )

    val borderStroke = Brush.linearGradient(
        colors = listOf(Color(0xFFF5B84C), Color(0xFF1C170E))
    )

    val bounceAnim = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(Unit) {
        bounceAnim.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(
                durationMillis = 600,
                easing = {
                    if (it < 0.5f) {
                        (4 * it * it * it).coerceAtMost(1f)
                    } else {
                        (1 - (-2 * it + 2).pow(2) / 2).coerceAtMost(1f)
                    }
                }
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .graphicsLayer(scaleX = bounceAnim.value, scaleY = bounceAnim.value)
            .clip(RoundedCornerShape(24.dp))
            .background(borderStroke)
            .padding(1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(22.dp))
                .background(backgroundGradient)
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
                    color = Color.White,
                    fontSize = 25.sp
                )
            }
        }
    }
}

@Composable
fun AIProcessingDialog(showDialog: Boolean, onDismiss: () -> Unit) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text("Cancel", color = Color.Red)
                }
            },
            modifier = Modifier.clip(RoundedCornerShape(16.dp)),
            containerColor = Color.White,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Processing...",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.ai_processing))
                    val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier.height(250.dp) .width(250.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "AI is generating your task...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        )
    }
}

