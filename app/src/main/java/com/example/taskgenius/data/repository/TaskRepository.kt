package com.example.taskgenius.data.repository

import com.example.taskgenius.data.local.TaskEntity
import com.example.taskgenius.data.local.TaskStatus
import com.example.taskgenius.data.local.TaskDao
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    fun getTasksByStatus(status: TaskStatus): Flow<List<TaskEntity>> = taskDao.getTasksByStatus(status)

    fun getTasksByCategory(category: String): Flow<List<TaskEntity>> = taskDao.getTasksByCategory(category)


    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun updateTaskStatus(taskId: Int, status: TaskStatus) = taskDao.updateTaskStatus(taskId, status)
}
