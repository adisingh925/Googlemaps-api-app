package com.adreal.randomplaces.SharedPreferences

import android.content.Context
import android.content.SharedPreferences

object SharedPreferences {
    private lateinit var prefs : SharedPreferences

    fun init(context: Context){
        prefs = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    fun read(key: String, value: String): String? {
        return prefs.getString(key, value)
    }

    fun read(key: String, value: Long): Long {
        return prefs.getLong(key, value)
    }

    fun write(key: String, value: String) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putString(key, value)
            apply()
        }
    }

    fun write(key: String, value: Long) {
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putLong(key, value)
            apply()
        }
    }

    fun deleteAll(){
        val prefsEditor: SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            clear()
            apply()
        }
    }
}