package com.app.imagedownloader.framework.Utils

import android.content.Context
import android.widget.Toast
import com.google.firebase.crashlytics.FirebaseCrashlytics

object Logger {

    fun log(content: String) {
        println(content)
    }

    fun debugToast(length: Boolean = false, context: Context, content: String) {
//        val l = if (length) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
//        Toast.makeText(context, content, l).show()
    }
}