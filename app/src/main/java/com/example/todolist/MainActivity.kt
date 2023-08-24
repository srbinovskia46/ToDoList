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
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.view.menu.MenuView.ItemView

class MainActivity : AppCompatActivity(){

    private lateinit var todoAdapter: ToDoAdapter
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channel name"
    val NOTIFICATION_ID = 0
    var reverseOrder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createNotificationChannel()

        // Change the color of ActionBar background
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2196F3")))
        // Change the color of action bar text
        supportActionBar?.setTitle(Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>"))

        todoAdapter = ToDoAdapter(mutableListOf()){
            todo -> showEditDialog(todo)
        }

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
                if (reverseOrder){
                    todoAdapter.addFirst(todo)
                    rvTodoItems.smoothScrollToPosition(0)
                }else{
                    todoAdapter.addTodo(todo)
                }
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if  (item.itemId == R.id.reverse_order){
            reverseOrder = !reverseOrder
            todoAdapter.reverseOrderOfItems()
            val rvTodoItems = findViewById<RecyclerView>(R.id.rvTodoItems)
            rvTodoItems.adapter = todoAdapter
            rvTodoItems.layoutManager = LinearLayoutManager(this)
            todoAdapter.saveListToInternalStorage(this)

        }else if (item.itemId == R.id.help){
            Toast.makeText(this, "You clicked help", Toast.LENGTH_SHORT).show()
        }
        return super.onOptionsItemSelected(item)
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
            notificationText = "All tasks completed"
        }

        val contentText = generateContentText(todoAdapter.getTodoItems())

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            //.setContentTitle("ToDo List")
            .setContentText(contentText)
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setSmallIcon(R.mipmap.ic_launcher)
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

    private fun generateContentText(todoList: List<Todo>): String{
        val countOfListItems = todoList.count()
        if (countOfListItems == 1){
            return "You have 1 task to complete"
        }else if (countOfListItems == 0){
            return  "All tasks completed"
        }else{
            return String.format("You have %d tasks to complete", countOfListItems)
        }
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
        val editSaveButton = dialogView.findViewById<Button>(R.id.btnSave)
        val editCancelButton = dialogView.findViewById<Button>(R.id.btnCancel)
        editTodoTitle.setText(todo.title)

        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Edit Todo")
            .setView(dialogView)
//            .setPositiveButton("Save") { _, _ ->
//                val editedTitle = editTodoTitle.text.toString()
//                if (editedTitle.isNotEmpty()) {
//                    todo.title = editedTitle
//                    todoAdapter.notifyDataSetChanged()
//                }
//            }
            //.setNegativeButton("Cancel", null)
            .show()

        editSaveButton.setOnClickListener {
            val editedTitle = editTodoTitle.text.toString()
            if (editedTitle.isNotEmpty()){
                todo.title = editedTitle
                todoAdapter.notifyDataSetChanged()
            }
            showToDoListNotification(todoAdapter.getTodoItems())
            todoAdapter.saveListToInternalStorage(this)
            alertDialog.cancel()
        }

        editCancelButton.setOnClickListener {
            alertDialog.cancel()
        }

    }
}