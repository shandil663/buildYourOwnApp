package com.example.taskgenius.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks

    private val _filteredTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val filteredTasks: StateFlow<List<TaskEntity>> = _filteredTasks

    init {
        fetchAllTasks()
    }

    private fun fetchAllTasks() {
        viewModelScope.launch {
            repository.getAllTasks().collectLatest { taskList ->
                _tasks.value = taskList
            }
        }
    }

    fun getTasksByStatus(status: TaskStatus) {
        viewModelScope.launch {
            repository.getTasksByStatus(status).collectLatest { taskList ->
                _filteredTasks.value = taskList
            }
        }
    }

    fun getTasksByCategory(category: String): StateFlow<List<TaskEntity>> {
        val filteredTasks = MutableStateFlow<List<TaskEntity>>(emptyList())
        viewModelScope.launch {
            repository.getTasksByCategory(category).collectLatest { taskList ->
                filteredTasks.value = taskList
            }
        }
        return filteredTasks
    }


    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun updateTaskStatus(taskId: Int, status: TaskStatus) {
        viewModelScope.launch {
            repository.updateTaskStatus(taskId, status)
        }
    }
}
