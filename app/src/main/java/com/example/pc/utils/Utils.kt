package com.example.pc.utils

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pc.R

interface OnDialogClicked{
    fun onPositiveButtonClicked()
}

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()

fun makeDialog(
    context: Context,
    onDialogClicked: OnDialogClicked,
    title: String,
    message: String
){

    AlertDialog
        .Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(context.resources.getString(R.string.Oui)) { _, _ ->
            onDialogClicked.onPositiveButtonClicked()
        }
        .setNegativeButton(context.resources.getString(R.string.Cancel), null)
        .show()
}



