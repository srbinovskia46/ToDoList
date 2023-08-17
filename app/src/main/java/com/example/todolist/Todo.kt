package com.example.todolist

import java.io.Serializable

data class Todo(
    var title: String,
    var isChecked: Boolean = false
):Serializable