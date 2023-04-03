package alpha.company.pc.utils

import alpha.company.pc.R
import alpha.company.pc.data.models.local.OrderStatus
import alpha.company.pc.data.models.network.Error
import alpha.company.pc.ui.fragments.UserStepTwo
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import id.zelory.compressor.Compressor
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.BufferedSink
import java.io.*

private const val TAG = "UtilsFolder"

//private const val ERROR404 = "Page introuvable"
//private const val ERROR500 = "Erreur interne du serveur"
//private const val ERROR401 = "Email ou password incorrect"
const val ANNONCES_AWS_S3_LINK = "https://pcfy.s3.eu-west-3.amazonaws.com/"
const val USERS_AWS_S3_LINK = "https://pcfy-profiles.s3.eu-west-3.amazonaws.com/"
const val DEMANDS_AWS_S3_LINK = "https://pcfy-demand.s3.eu-west-3.amazonaws.com/"
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

        val test = responseBody?.charStream()?.readText()
        Log.e(TAG, "JSONObject or msg : $test ")
        val error = Gson().fromJson(test, ErrorResponse::class.java)
        Log.e(TAG, "error: $error")

//        val jsonObj = responseBody?.charStream()?.readText()?.let { JSONObject(it) }
//        jsonObj?.getString("error")?.let { Error(it, code) }
        Error(error.error, code)
    } catch (e: Exception) {
        Log.e(TAG, "getError: $e.stackTrace")
        val error = e.message?.let { Error(it, code) }
        Log.e(TAG, "error parsing JSON error message: $error")
        return error
    }
}

data class ErrorResponse(
    @SerializedName("error")
    val error: String
)

fun getImageResource(orderStatus: String): Int {
    return when (orderStatus) {
        OrderStatus.DELIVERED.status -> R.drawable.ic_baseline_done_24
        OrderStatus.CANCELED.status -> R.drawable.ic_baseline_cancel_24
        else -> R.drawable.ic_baseline_access_time_24
    }
}

suspend fun getImageRequestBody(
    uri: Uri,
    context: Context,
): UserStepTwo.ImageInfo? {

    Log.d(TAG, "getImageRequestBody uri: $uri")

    val parcelFileDescriptor =
        context.contentResolver.openFileDescriptor(uri, "r", null) ?: return null

    val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
    val file = File(context.cacheDir, context.contentResolver.getFileName(uri))
    val outputStream = withContext(Dispatchers.IO) {
        FileOutputStream(file)
    }
    inputStream.copyTo(outputStream)
    Log.d(TAG, "file size : ${file.fileSize()}KB")


    val compressedImageFile = Compressor.compress(context, file, Dispatchers.Main)
    Log.d(TAG, "compressedImageFile size : ${compressedImageFile.fileSize()}KB")

//    progress_bar.progress = 0
    val body = UploadRequestBody(compressedImageFile, "image")

    parcelFileDescriptor.close()
    return UserStepTwo.ImageInfo(
        file.name,
        body
    )
}

fun defineField(textView: TextView, value: String?, context: Context, fillWith: String? = null) {
    if (value != null) {
        textView.text = value
    } else {
        if (fillWith == null) {
            textView.text = context.getString(R.string.no_defined)
        } else {
            textView.text = fillWith
        }
    }
}

fun ContentResolver.getFileName(fileUri: Uri): String {
    var name = ""
    val returnCursor = this.query(fileUri, null, null, null, null)
    if (returnCursor != null) {
        val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor.moveToFirst()
        name = returnCursor.getString(nameIndex)
        returnCursor.close()
    }
    return name
}

class UploadRequestBody(
    private val file: File,
    private val contentType: String,
//    private val callback: UploadCallback
) : RequestBody() {

    override fun contentType() = "$contentType/*".toMediaTypeOrNull()

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val length = file.length()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val fileInputStream = FileInputStream(file)
        var uploaded = 0L
        fileInputStream.use { inputStream ->
            var read: Int
            val handler = Handler(Looper.getMainLooper())
            while (inputStream.read(buffer).also { read = it } != -1) {
                handler.post(ProgressUpdater(uploaded, length))
                uploaded += read
                sink.write(buffer, 0, read)
            }
        }
    }

    interface UploadCallback {
        fun onProgressUpdate(percentage: Int)
    }

    inner class ProgressUpdater(
        private val uploaded: Long,
        private val total: Long
    ) : Runnable {
        override fun run() {
//            callback.onProgressUpdate((100 * uploaded / total).toInt())
        }
    }

    companion object {
        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}

fun readTextFile(idRes: Int, context: Context): String {
    try {
        var string: String? = ""
        val stringBuilder = StringBuilder()
        val `is`: InputStream = context.resources.openRawResource(idRes)
        val reader = BufferedReader(InputStreamReader(`is`))
        while (true) {
            try {
                if (reader.readLine().also { string = it } == null) break
            } catch (e: IOException) {
                e.printStackTrace()
            }
            stringBuilder.append(string).append("\n")
//            binding.userPolicyTv.text = stringBuilder
        }
        `is`.close()
        return stringBuilder.toString()
    } catch (e: Exception) {
        Log.e(TAG, "readTextFile: ${e.stackTrace}")
        return String()
    }

}

fun File.fileSize(): Int {
    return java.lang.String.valueOf(this.length() / 1024).toInt()
}
