package com.example.todolist

import android.content.Context
import android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class ToDoAdapter(
    private val todos: MutableList<Todo>
) : RecyclerView.Adapter<ToDoAdapter.TodoViewHolder>() {

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTodoTitle: TextView = itemView.findViewById(R.id.tvTodoTitle)
        val cbDone: CheckBox = itemView.findViewById(R.id.cbDone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_todo,
                parent,
                false
            )
        )
    }

    fun getTodoItems() : MutableList<Todo>{
        return todos
    }

    fun addTodo(todo: Todo) {
        todos.add(todo)
        notifyItemInserted(todos.size - 1)
    }

    fun deleteDoneTodos() {
        todos.removeAll { todo ->
            todo.isChecked
        }
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return todos.size
    }

    private fun toggleStrikeThrough(tvTodoTitle: TextView, isChecked: Boolean) {
        if (isChecked) {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags or STRIKE_THRU_TEXT_FLAG
        } else {
            tvTodoTitle.paintFlags = tvTodoTitle.paintFlags and STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val curTodo = todos[position]
        holder.itemView.apply {
            holder.tvTodoTitle.text = curTodo.title
            holder.cbDone.isChecked = curTodo.isChecked
            toggleStrikeThrough(holder.tvTodoTitle, curTodo.isChecked)
            holder.cbDone.setOnCheckedChangeListener { _, isChecked ->
                toggleStrikeThrough(holder.tvTodoTitle, isChecked)
                curTodo.isChecked = !curTodo.isChecked
            }
        }
    }

    fun saveListToInternalStorage(context: Context){
        try {
            val fileOutputStream = context.openFileOutput("todolist.txt", Context.MODE_PRIVATE)
            val objectOutputStream = ObjectOutputStream(fileOutputStream)
            objectOutputStream.writeObject(todos)
            objectOutputStream.close()
            fileOutputStream.close()
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    fun loadListToInternalStorage(context: Context){
        val todoList: MutableList<Todo> = mutableListOf()
        try{
            val fileInputStream = context.openFileInput("todolist.txt")
            val objectInputStream = ObjectInputStream(fileInputStream)
            val tmpList = objectInputStream.readObject()
            if (tmpList is List<*>){
                todos.clear()
                for (item in tmpList){
                    if (item is Todo){
                        todos.add(item)
                    }
                }
            }
            fileInputStream.close()
            objectInputStream.close()
            notifyDataSetChanged()
        }catch (e: IOException){
            e.printStackTrace()
        }catch (e: ClassNotFoundException){
            e.printStackTrace()
        }
    }
}