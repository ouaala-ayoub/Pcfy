package com.example.pc.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.viewModelScope
import com.example.pc.R
import com.example.pc.data.models.local.TokenRequest
import com.example.pc.data.models.network.BodyX
import com.example.pc.data.models.network.User
import com.example.pc.ui.activities.FullOrdersActivity
import com.example.pc.ui.viewmodels.AuthModel
import com.example.pc.utils.LocalStorage
import com.example.pc.utils.getError
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val CHANNEL_ID = "ORDER NOTIFICATIONS"
private const val TAG = "PushNotificationService"

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "onNewToken: $token")

        setNewFirebaseToken(token)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationTitle = getString(R.string.new_order)
        val notificationBody = getString(R.string.notification_body, message.data["annonceName"])
        val orderId = message.data["orderId"].toString()
        val sellerId = message.data["sellerId"].toString()

        Log.i(TAG, "onMessageReceived sellerId: $sellerId and orderId: $orderId")

        createNotificationChannel()
        sendNotification(
            notificationTitle,
            notificationBody,
            sellerId,
            orderId
        )

    }

    private fun sendNotification(
        textTitle: String,
        textContent: String,
        sellerId: String,
        orderId: String
    ) {

        val ordersIntent = Intent(baseContext, FullOrdersActivity::class.java).apply {
            putExtra("id", sellerId)
            putExtra("orderId", orderId)
        }

        val pendingIntent: PendingIntent? =
            TaskStackBuilder.create(applicationContext).run {
                addNextIntentWithParentStack(ordersIntent)
                getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }

        val builder = NotificationCompat.Builder(baseContext, CHANNEL_ID)
            //to change with app icon
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(baseContext)) {
            val notificationId = System.currentTimeMillis().toInt()
            Log.i(TAG, "notificationId : $notificationId")
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setNewFirebaseToken(token: String) {

        val tokens = LocalStorage.getTokens(baseContext)
        val retrofitService = RetrofitService.getInstance()

        retrofitService.auth(tokens).enqueue(object : Callback<BodyX?> {
            override fun onResponse(call: Call<BodyX?>, response: Response<BodyX?>) {
                val res = response.body()
                if (res != null) {
                    val userId = res.id

                    retrofitService.putFireBaseToken(userId, TokenRequest(token))
                        .enqueue(object : Callback<User> {
                            override fun onResponse(call: Call<User>, response: Response<User>) {
                                if (response.isSuccessful && response.body() != null) {
                                    Log.i(TAG, "putFireBaseToken isSuccessful")
                                } else {
                                    try {
                                        val error =
                                            getError(response.errorBody()!!, response.code())
                                        Log.e(TAG, "putFireBaseToken : ${error?.message}")
                                    } catch (e: Throwable) {
                                        Log.e(TAG, "getError : ${e.message}")
                                    }
                                }
                            }

                            override fun onFailure(call: Call<User>, t: Throwable) {
                                Log.e(TAG, "putFireBaseToken : ${t.message}")
                            }
                        })

                } else {
                    try {
                        val error = getError(response.errorBody()!!, response.code())
                        Log.e(TAG, "putFireBaseToken : ${error?.message}")
                    } catch (e: Throwable) {
                        Log.e(TAG, "getError : ${e.message}")
                    }
                }
            }

            override fun onFailure(call: Call<BodyX?>, t: Throwable) {
                Log.e(TAG, "onFailure set New Token: ")
            }

        })
    }
}
