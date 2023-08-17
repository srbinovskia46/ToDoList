package com.example.todolist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity(){

    private lateinit var todoAdapter: ToDoAdapter
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channel name"
    val NOTIFICATION_ID = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        todoAdapter = ToDoAdapter(mutableListOf())

        val rvTodoItems = findViewById<RecyclerView>(R.id.rvTodoItems)
        val btnAddTodo = findViewById<Button>(R.id.btnAddTodo)
        val etTodoTitle = findViewById<EditText>(R.id.edTodoTitle)
        val btnDeleteDoneTodos = findViewById<Button>(R.id.btnDeleteDoneTodos)

        //Get list of to-do items from the adapter
        val todoList = todoAdapter.getTodoItems()

        //Add Items
        rvTodoItems.adapter = todoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        todoAdapter.loadListToInternalStorage(this)

        showToDoListNotification(todoAdapter.getTodoItems())

        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                etTodoTitle.text.clear()
            }
            todoAdapter.saveListToInternalStorage(this)
            showToDoListNotification(todoList)
        }

        btnDeleteDoneTodos.setOnClickListener {
            todoAdapter.deleteDoneTodos()
            todoAdapter.saveListToInternalStorage(this)
            showToDoListNotification(todoList)
        }
    }



    private fun createPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        return pendingIntent
    }

    @SuppressLint("MissingPermission")
    fun showToDoListNotification(todoList: List<Todo>) {
        var notificationText = buildNotificationText(todoList)

        if (todoList.isEmpty()) {
            notificationText = "All tasks competed"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ToDo List")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notificationText)
            )
            .setContentIntent(createPendingIntent())
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationText(todoList: List<Todo>): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("Tasks:\n")
        for (item in todoList) {
            stringBuilder.append("• ").append(item.title).append("\n")
        }
        return stringBuilder.toString()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.YELLOW
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun showEditDialog(todo: Todo) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit, null)
        val editTodoTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        editTodoTitle.setText(todo.title)

        AlertDialog.Builder(this)
            .setTitle("Edit Todo")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val editedTitle = editTodoTitle.text.toString()
                if (editedTitle.isNotEmpty()) {
                    todo.title = editedTitle
                    todoAdapter.notifyDataSetChanged()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}