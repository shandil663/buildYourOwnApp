package com.example.taskgenius.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String?,
    val createdAt: Long,
    val dueAt: Long?,
    val category: String,
    val status: TaskStatus
)

enum class TaskStatus {
    PENDING, COMPLETED, OVERDUE, ERROR
}
