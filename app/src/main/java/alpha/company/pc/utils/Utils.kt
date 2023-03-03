package alpha.company.pc.utils

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import alpha.company.pc.R
import alpha.company.pc.data.models.local.OrderStatus
import alpha.company.pc.data.models.network.Error
import alpha.company.pc.ui.fragments.UserStepTwo
import okhttp3.ResponseBody
import org.json.JSONObject
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

private const val TAG = "Utils"
//private const val ERROR404 = "Page introuvable"
//private const val ERROR500 = "Erreur interne du serveur"
//private const val ERROR401 = "Email ou password incorrect"
const val BASE_AWS_S3_LINK = "https://pcfy.s3.eu-west-3.amazonaws.com/"
const val USERS_AWS_S3_LINK = "https://pcfy-profiles.s3.eu-west-3.amazonaws.com/"
const val ERROR_MSG = "Erreur inattendue"
const val NON_AUTHENTICATED = "Utilisateur non authentifié"

//real ids
//const val INTERSTITIAL_ORDER_DONE_ADD_ID = "ca-app-pub-8302914035567843/9280644946"
//const val INTERSTITIAL_ORDER_CLICKED_ID = "ca-app-pub-8302914035567843/9262950186"
//const val INTERSTITIAL_ANNONCE_CLICKED_ID = "ca-app-pub-8302914035567843/5021286992"

//test ids
const val INTERSTITIAL_ORDER_DONE_ADD_ID = "ca-app-pub-3940256099942544/1033173712"
const val INTERSTITIAL_ORDER_CLICKED_ID = "ca-app-pub-3940256099942544/1033173712"
const val INTERSTITIAL_ANNONCE_CLICKED_ID = "ca-app-pub-3940256099942544/1033173712"

interface OnDialogClicked {
    fun onPositiveButtonClicked()
    fun onNegativeButtonClicked()
}

fun circularProgressBar(context: Context): CircularProgressDrawable {
    val circularProgressDrawable = CircularProgressDrawable(context)
    circularProgressDrawable.apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }
    return circularProgressDrawable
}

fun Context.toast(message: String, length: Int) =
    Toast.makeText(this, message, length).show()

fun makeDialog(
    context: Context,
    onDialogClicked: OnDialogClicked,
    title: String?,
    message: String?,
    view: View? = null,
    negativeText: String = context.resources.getString(R.string.Cancel),
    positiveText: String = context.resources.getString(R.string.Oui)

): AlertDialog {
    val myDialog = AlertDialog
        .Builder(context)
        .setTitle(title)
        .setMessage(message)
        .setView(view)
        .setCancelable(false)
        .setPositiveButton(positiveText) { _, _ ->
            onDialogClicked.onPositiveButtonClicked()
        }

        .setNegativeButton(negativeText) { _, _ ->
            onDialogClicked.onNegativeButtonClicked()
        }
        .create()

    myDialog.setOnCancelListener {
        it.dismiss()
    }

    return myDialog
}

fun makeSnackBar(
    view: View,
    message: String,
    duration: Int
): Snackbar {
    return Snackbar.make(view, message, duration)
}

fun getError(responseBody: ResponseBody?, code: Int): Error? {
    return try {
        Log.e(TAG, "JSONObject or msg :${responseBody?.charStream()?.readText()} ")
        val jsonObj = responseBody?.charStream()?.readText()?.let { JSONObject(it) }
        jsonObj?.getString("error")?.let { Error(it, code) }
    } catch (e: Exception) {
        val error = e.message?.let { Error(it, code) }
        Log.e(TAG, "error parsing JSON error message: $error")
        return error
    }
}

//fun getTheErrorMessage(error: Error) {
//    when (error.code) {
//        404 -> error.message = ERROR404
//        500 -> error.message = ERROR500
//        401 -> error.message = ERROR401
//    }
//}

//private fun <T> goToActivityWithUserId(userId: String, context: Context, activity: Class<T>) {
//    val intent = Intent(context, activity)
//    intent.putExtra("id", userId)
//    context.startActivity(intent)
//}

class URIPathHelper {

    fun getPath(context: Context, uri: Uri): String? {
        val isKitKatOrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKatOrAbove && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(id)
                )
                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).toTypedArray()
                val type = split[0]
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }
                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            }
        } else if ("content".equals(uri.scheme, ignoreCase = true)) {
            return getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)
        try {
            cursor = uri?.let {
                context.contentResolver.query(
                    it,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )
            }
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex: Int = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(columnIndex)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }


}
fun getImageResource(orderStatus: String): Int {
    return when(orderStatus) {
        OrderStatus.DELIVERED.status -> R.drawable.ic_baseline_done_24
        OrderStatus.CANCELED.status -> R.drawable.ic_baseline_cancel_24
        else -> R.drawable.ic_baseline_access_time_24
    }
}

fun getImageRequestBody(uri: Uri, context: Context): UserStepTwo.ImageInfo {

    val file = File(URIPathHelper().getPath(context, uri)!!)
    Log.i(TAG, "file selected : ${file.name}")
    val requestFile: RequestBody =
        file.asRequestBody("image/*".toMediaTypeOrNull())

    return UserStepTwo.ImageInfo(
        file.name,
        requestFile
    )
}
