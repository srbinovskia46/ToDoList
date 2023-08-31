package com.example.todolist

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.AudioAttributes
import android.media.AudioManager.STREAM_MUSIC
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
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity(){

    private lateinit var todoAdapter: ToDoAdapter
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    val CHANNEL_ID = "channelID"
    val CHANNEL_NAME = "channel name"
    val NOTIFICATION_ID = 0
    var reverseOrder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set up permission request launcher for notification
        requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            if (it){

            }else{
                Snackbar.make(
                findViewById<View>(androidx.appcompat.R.id.content).rootView,
                "Please grant Notification permission from App Settings",
                Snackbar.LENGTH_LONG
                ).show()
            }
        }

        createNotificationChannel()

        // Change the color of ActionBar background
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#2196F3")))
        // Change the color of action bar text
        supportActionBar?.title = Html.fromHtml("<font color=\"#FFFFFF\">" + getString(R.string.app_name) + "</font>")

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

        etTodoTitle.setOnEditorActionListener{
            _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE){
                val todoTitle = etTodoTitle.text.toString().trim()

                if (todoTitle.isNotEmpty()){
                    val todo = Todo(todoTitle)
                    if (reverseOrder) {
                        todoAdapter.addFirst(todo)
                        rvTodoItems.smoothScrollToPosition(0)
                    }
                    else{
                        todoAdapter.addTodo(todo)
                        rvTodoItems.smoothScrollToPosition(todoList.size-1)
                    }

                    etTodoTitle.text.clear()
                }
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        btnAddTodo.setOnClickListener {
            val todoTitle = etTodoTitle.text.toString()
            if (todoTitle.isNotEmpty()) {
                val todo = Todo(todoTitle)
                if (reverseOrder){
                    todoAdapter.addFirst(todo)
                    rvTodoItems.smoothScrollToPosition(0)
                }else{
                    todoAdapter.addTodo(todo)
                    rvTodoItems.smoothScrollToPosition(todoList.size-1)
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

    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if  (item.itemId == R.id.reverse_order){
            reverseOrder = !reverseOrder
            todoAdapter.reverseOrderOfItems()
            val rvTodoItems = findViewById<RecyclerView>(R.id.rvTodoItems)
            if (reverseOrder){
                item.setTitle(R.string.oldest_first)
            }else
                item.setTitle(R.string.newest_first)
            rvTodoItems.adapter = todoAdapter
            rvTodoItems.layoutManager = LinearLayoutManager(this)
            todoAdapter.saveListToInternalStorage(this)

        }else if (item.itemId == R.id.credits){
            Toast.makeText(this, R.string.created_by, Toast.LENGTH_LONG).show()
        }
        return super.onOptionsItemSelected(item)
    }



    private fun createPendingIntent(): PendingIntent? {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        return pendingIntent
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("MissingPermission")
    fun showToDoListNotification(todoList: List<Todo>) {
        if (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED)
        {

        }else{
            requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        var notificationText = buildNotificationText(todoList)

        if (todoList.isEmpty()) {
            notificationText = R.string.all_tasks_completed.toString()
        }

        val contentText = generateContentText(todoAdapter.getTodoItems())

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
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
            return getString(R.string.one_task_to_complete)
        }else if (countOfListItems == 0){
            return  getString(R.string.all_tasks_completed)
        }else{
            return String.format(getString(R.string.tasks_left_to_complete), countOfListItems)
        }
    }

    private fun buildNotificationText(todoList: List<Todo>): String {
        val stringBuilder = StringBuilder()
        if (todoList.size == 1){
            stringBuilder.append(getString(R.string.task))
        }else{
            stringBuilder.append(getString(R.string.tasks))
        }

        if (todoList.size <= 5){
            for (item in todoList) {
                stringBuilder.append("• ").append(item.title).append("\n")
            }
        }else {
            var counter = 0
            for (item in todoList) {
                if (counter == 5){
                    val otherTasks = todoList.size - 5
                    if (otherTasks == 1)
                        stringBuilder.append("+1").append(getString(R.string.other_task))
                    else
                        stringBuilder.append("+").append(todoList.size - 5).append(getString(R.string.other_tasks))
                    break
                }

                stringBuilder.append("• ").append(item.title).append("\n")
                counter++
            }
        }

        return stringBuilder.toString()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lightColor = Color.BLUE
                enableLights(true)
            }
            channel.setSound(null, AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())
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
            .setTitle(getString(R.string.edit_todo))
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