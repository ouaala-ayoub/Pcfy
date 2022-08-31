package com.example.pc.utils

import android.content.Context
import android.content.DialogInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.pc.R
import okhttp3.ResponseBody
import org.json.JSONObject

interface OnDialogClicked{
    fun onPositiveButtonClicked()
    fun onNegativeButtonClicked()
}

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()

fun makeDialog(
    context: Context,
    onDialogClicked: OnDialogClicked,
    title: String,
    message: String
){
    val myDialog = AlertDialog
        .Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(context.resources.getString(R.string.Oui)) { _, _ ->
            onDialogClicked.onPositiveButtonClicked()
        }
        .setNegativeButton(context.resources.getString(R.string.Cancel)){ _, _ ->
            onDialogClicked.onNegativeButtonClicked()
        }
        .show()
}

fun getError(responseBody: ResponseBody): String {
    val jsonObj = JSONObject(responseBody.charStream().readText())
    return jsonObj.getString("error")
}



