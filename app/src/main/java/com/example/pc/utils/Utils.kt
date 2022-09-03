package com.example.pc.utils

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.pc.R
import com.example.pc.data.models.network.Error
import okhttp3.ResponseBody
import org.json.JSONObject

private const val TAG = "Utils"
private const val ERROR404 = "Page introuvable"
private const val ERROR500 = "Erreur interne du serveur"
private const val ERROR401 = "Email ou password incorrect"

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
): AlertDialog {
    val myDialog = AlertDialog
        .Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setCancelable(false)
        .setPositiveButton(context.resources.getString(R.string.Oui)) { _, _ ->
            onDialogClicked.onPositiveButtonClicked()
        }

        .setNegativeButton(context.resources.getString(R.string.Cancel)){ dialog, _ ->
            onDialogClicked.onNegativeButtonClicked()
        }
        .create()

    myDialog.setOnCancelListener {
        it.dismiss()
    }

    return myDialog
}

fun getError(responseBody: ResponseBody, code: Int): Error? {
    return try{
        val jsonObj = JSONObject(responseBody.charStream().readText())
        Error(jsonObj.getString("error"), code)
    }
    catch (e: Exception){
        val error = e.message?.let { Error(it, code) }
        Log.i(TAG, "getError: $error")
        return error
    }
}

fun getTheErrorMessage(error: Error){
    if (error.code == 404){
        error.message = ERROR404
    }
    when(error.code){
        404 -> error.message = ERROR404
        500 -> error.message = ERROR500
        401 -> error.message = ERROR401
    }
}

