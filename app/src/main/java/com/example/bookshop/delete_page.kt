package com.example.bookshop

import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class DeletePage : AppCompatActivity() {
    private val BOOKS_URI = Uri.parse("content://com.example.MyApplication.BookStore/Books")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.delete_page)

        val deletedCount = deleteBook()
        Log.d("DeletePage", "Deleted count: $deletedCount")
    }

    private fun deleteBook(): Int {
        val contentResolver = contentResolver
        return contentResolver.delete(BOOKS_URI, null, null)
    }
}
