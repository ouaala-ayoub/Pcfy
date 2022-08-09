package com.example.pc.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()


