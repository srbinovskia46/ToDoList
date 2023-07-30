package com.example.todolist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var todoAdapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        todoAdapter = ToDoAdapter(mutableListOf())

        val rvTodoItems = findViewById<RecyclerView>(R.id.rvTodoItems)
        val btnAddTodo = findViewById<Button>(R.id.btnAddTodo)
        val etTodoTitle = findViewById<EditText>(R.id.edTodoTitle)
        val btnDeleteDoneTodos = findViewById<Button>(R.id.btnDeleteDoneTodos)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLoad = findViewById<Button>(R.id.btnLoad)

        rvTodoItems.adapter = todoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                etTodoTitle.text.clear()
            }
        }

        btnDeleteDoneTodos.setOnClickListener {
            todoAdapter.deleteDoneTodos()
        }

        btnSave.setOnClickListener {
            todoAdapter.saveListToInternalStorage(this) }

        btnLoad.setOnClickListener {
            todoAdapter.loadListToInternalStorage(this)
        }
    }
}