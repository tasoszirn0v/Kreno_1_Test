package com.example.kreno_1_test

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val channelId = "notification_channel"
const val channelName = "com.example.kreno_1_test"

class MyFirebaseMessagingService : FirebaseMessagingService(){

    // 1. Create notification
    // 2. Attach the notification created with the custom layout
    // 3. Show the notification

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null){
            generateNotification(remoteMessage.notification!!.title!!, remoteMessage.notification!!.body!!)
        }

    }

    override fun onNewToken(token:String){
        super.onNewToken(token)
        Log.e("newToken", token)
        //add token in my shapereference
        getSharedPreferences("_", MODE_PRIVATE).edit().putString("fcm_token", token).apply()
    }


    companion object{
        //Whenever you need FCM token, just call this static method to get it.
        fun getToken(context: Context): String{
            return context.getSharedPreferences("_", MODE_PRIVATE).getString("fcm_token", "empty")!!
        }
    }




    private fun getRemoteView(title: String, message: String): RemoteViews{
        val remoteView = RemoteViews("com.example.kreno_1_test", R.layout.push_notification)

        remoteView.setTextViewText(R.id.title, title)
        remoteView.setTextViewText(R.id.message, message)
        remoteView.setImageViewResource(R.id.fcm_logo, R.drawable.eap_fcm)

        return remoteView
    }


    private fun generateNotification(title: String, message: String) {

        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        //  channel id, channel name
        var builder: NotificationCompat.Builder =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.eap_fcm)
                .setAutoCancel(true)
                .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(0, builder.build())

    }


}