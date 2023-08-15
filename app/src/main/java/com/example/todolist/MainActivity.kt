package com.example.todolist

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

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
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnLoad = findViewById<Button>(R.id.btnLoad)

        //Get list of to-do items from the adapter
        val todoList = todoAdapter.getTodoItems()


        //Add Items
        rvTodoItems.adapter = todoAdapter
        rvTodoItems.layoutManager = LinearLayoutManager(this)

        todoAdapter.loadListToInternalStorage(this)

        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                todoAdapter.addTodo(todo)
                etTodoTitle.text.clear()
            }
            todoAdapter.saveListToInternalStorage(this)
        }

        btnDeleteDoneTodos.setOnClickListener {
            todoAdapter.deleteDoneTodos()
            todoAdapter.saveListToInternalStorage(this)
        }

        btnSave.setOnClickListener {
            todoAdapter.saveListToInternalStorage(this)
            showToDoListNotification(todoList)
        }

        btnLoad.setOnClickListener {
            todoAdapter.loadListToInternalStorage(this)
        }
    }

    @SuppressLint("MissingPermission")
    fun showToDoListNotification(todoList: List<Todo>){
        val notificationText = buildNotificationText(todoList)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ToDo List")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentText("Todo")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(notificationText))
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun buildNotificationText(todoList: List<Todo>): String {
        val stringBuilder = StringBuilder()
        for (item in todoList){
            stringBuilder.append(item.title).append("\n")
        }
        return stringBuilder.toString()
    }



    fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT).apply {
                lightColor = Color.YELLOW
                enableLights(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}