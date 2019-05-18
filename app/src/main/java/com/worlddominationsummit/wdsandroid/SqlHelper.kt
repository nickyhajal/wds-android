package com.worlddominationsummit.wdsandroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import org.jetbrains.anko.db.*
import org.json.JSONArray

/**
 * Created by nicky on 7/6/17.
 */
class SqlHelper(ctx: Context) : ManagedSQLiteOpenHelper(ctx, "mydb") {

    companion object {
        private var instance: SqlHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): SqlHelper {
            if (instance == null) {
                instance = SqlHelper(ctx.applicationContext)
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

}

