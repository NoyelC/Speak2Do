package com.example.speak2do.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Task(
    val task_title: String,
    val description: String,
    val date_time: String,
    val priority: String,
    val additional_notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

class TaskFirestoreRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun saveTask(task: Task) {
        firestore
            .collection("tasks")
            .add(task)
            .await()
    }
}
