package com.example.pc.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pc.R
import com.example.pc.data.models.network.Tokens
import io.github.nefilim.kjwt.JWT

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()

fun makeDialog(context: Context){
}

