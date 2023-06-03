package com.example.bookshop
import android.content.*
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.text.TextUtils
import java.lang.IllegalArgumentException
import java.util.HashMap
import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
class BookStore : ContentProvider() {
    companion object {
        val PROVIDER_NAME = "com.example.MyApplication.BookStore"
        val URL = "content://" + PROVIDER_NAME + "/Books"
        val CONTENT_URI = Uri.parse(URL)
        val _ID = "_id"
        val NAME = "name"
        val PRICE = "price"
        private val BookStore_PROJECTION_MAP: HashMap<String, String>? = null
        val BOOKS = 1
        val BOOK_ID = 2
        val uriMatcher: UriMatcher? = null
        val DATABASE_NAME = "BookStore"
        val BOOKS_TABLE_NAME = "books"
        val DATABASE_VERSION = 1
        val CREATE_DB_TABLE = " CREATE TABLE " + BOOKS_TABLE_NAME +        " (_id INTEGER PRIMARY KEY         AUTOINCREMENT, " + "name TEXT NOT NULL, " + " price TEXT NOT NULL);"
        private lateinit var dbHelper: DatabaseHelper

    }
    private var sUriMatcher = UriMatcher(UriMatcher.NO_MATCH);
    init{
        sUriMatcher.addURI(PROVIDER_NAME, "books", BOOKS);
        sUriMatcher.addURI(PROVIDER_NAME, "books/#", BOOK_ID);
    }

    private var db: SQLiteDatabase? = null



    private class DatabaseHelper internal constructor(context: Context?) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(CREATE_DB_TABLE)
        }


        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS " + BOOKS_TABLE_NAME)
            onCreate(db)
        }
    }

    override fun onCreate(): Boolean {
        val context = context?.applicationContext
        if (context != null) {
            dbHelper = DatabaseHelper(context)
            db = dbHelper.writableDatabase
            return db != null
        }
        return false
    }
    //-------------------------------------------------------------(slide 13)
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val rowID = db!!.insert(BOOKS_TABLE_NAME, "", values)
        if (rowID > 0) {
            val _uri = ContentUris.withAppendedId(CONTENT_URI, rowID)
            context!!.contentResolver.notifyChange(_uri, null)
            return _uri
        }


        throw SQLException("Failed to add a record into $uri")
    }
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        var sortOrder = sortOrder
        val qb = SQLiteQueryBuilder()
        qb.tables = BOOKS_TABLE_NAME
        when (uriMatcher!!.match(uri)) {
            BOOK_ID -> qb.appendWhere(_ID + "=" + uri.pathSegments[1])

            else -> {
                null
            }
        }
        if (sortOrder == null || sortOrder === "") {
            /*** By default sort on student names*/
            sortOrder = NAME
        }

        val db = dbHelper.writableDatabase // Get the writable database from the DatabaseHelper
        val c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context!!.contentResolver, uri)
        return c
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            BOOKS -> count = db!!.delete(
                BOOKS_TABLE_NAME, selection,
                selectionArgs
            )
            BOOK_ID -> {
                val id = uri.pathSegments[1]
                count = db!!.delete(
                    BOOKS_TABLE_NAME,
                    _ID + " = " + id +
                            if (!TextUtils.isEmpty(selection)) " AND ($selection)" else "",
                    selectionArgs
                )
            }
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        var count = 0
        when (uriMatcher!!.match(uri)) {
            BOOKS -> count = db!!.update(
                BOOKS_TABLE_NAME, values, selection,
                selectionArgs
            )
            BOOK_ID -> count = db!!.update(
                BOOKS_TABLE_NAME,
                values,
                _ID + " = " + uri.pathSegments[1] + (if (!TextUtils.isEmpty(selection)) " AND ($selection)" else ""),
                selectionArgs
            )
            else -> throw IllegalArgumentException("Unknown URI $uri")
        }
        context!!.contentResolver.notifyChange(uri, null)
        return count
    }
    override fun getType(uri: Uri): String? {
        when (uriMatcher!!.match(uri)) {
            BOOKS -> return "vnd.android.cursor.dir/vnd.example.students"
            BOOK_ID -> return "vnd.android.cursor.item/vnd.example.students"
            else -> throw IllegalArgumentException("Unsupported URI: $uri")

        }
    }
}

public class MainActivity : AppCompatActivity() {

    // Define the content URI for the BookStore ContentProvider
    private val BOOKS_URI = Uri.parse("content://com.example.MyApplication.BookStore/Books")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val insert: Button =findViewById(R.id.insert)
        val delete: Button =findViewById(R.id.delete)
        val query1: Button =findViewById(R.id.query)

        insert.setOnClickListener{
        setContentView(R.layout.insert1)
        }
        delete.setOnClickListener{
        setContentView(R.layout.delete_page)
        }
        query1.setOnClickListener{
            setContentView(R.layout.querypage)
        }



    }
    fun onClickAddBk(view: View?) {

        val values = ContentValues()
        values.put(
            BookStore.NAME,
            (findViewById<View>(R.id.book_text) as EditText).text.toString()
        )


        values.put(
            BookStore.PRICE,
            (findViewById<View>(R.id.price_i) as EditText).text.toString()
        )


        val uri = contentResolver.insert(
            BookStore.CONTENT_URI, values
        )


        Toast.makeText(baseContext, uri.toString(), Toast.LENGTH_LONG).show()
        val enterbtton: Button= findViewById(R.id.insert_button);

        enterbtton.setOnClickListener{setContentView(R.layout.activity_main) }


    }

    fun OnClickQueryBook(view: View?) {
        val booksID: EditText = findViewById(R.id.bookID)
        val URL = "content://com.example.MyApplication.BookStore/Books/" + booksID.text.toString()
        val books = Uri.parse(URL)
        var c = contentResolver.query(books, null, null, null, null)
        if (c != null) {
            if (c?.moveToFirst() == true) {
                do {
                    Toast.makeText(
                        this,
                        c.getString(c.getColumnIndexOrThrow(BookStore._ID)) +
                                ", " + c.getString(c.getColumnIndexOrThrow(BookStore.NAME)) + ", "
                                + c.getString(c.getColumnIndexOrThrow(BookStore.PRICE)),
                        Toast.LENGTH_SHORT
                    ).show()
                } while (c.moveToNext())

            }
        }
        val querybtton: Button = findViewById(R.id.querybtn);

        querybtton.setOnClickListener {
            setContentView(R.layout.activity_main)
        }


    }
    fun OnclickdeleteBook(view: View?){
        val booksID: EditText =findViewById(R.id.ID_book)
        val URL ="content://com.example.MyApplication.BookStore/Books/" + booksID.text.toString()
        val books=Uri.parse(URL)
        var c= contentResolver.delete(books,null,null)
        val deletebtton: Button= findViewById(R.id.Delete);

        deletebtton.setOnClickListener{setContentView(R.layout.activity_main)}
            }

    }





















