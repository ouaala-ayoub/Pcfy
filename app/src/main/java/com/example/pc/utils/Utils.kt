package com.example.pc.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import com.example.pc.R
import com.example.pc.data.models.network.Tokens
import io.github.nefilim.kjwt.JWT

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()


