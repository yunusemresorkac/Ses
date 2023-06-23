package com.yeslabapps.sesly.notify

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.yeslabapps.sesly.activity.OpeningActivity


class MyFirebaseMessaging : FirebaseMessagingService() {
    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val sp = getSharedPreferences("SP_USER", MODE_PRIVATE)
        val savedCurrentUser = sp.getString("Current_USERID", "None")
        val sent = remoteMessage.data["sent"]
        val user = remoteMessage.data["user"]
        val fUser = FirebaseAuth.getInstance().currentUser
        if (fUser != null && sent == fUser.uid) {
            if (savedCurrentUser != user) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendOAndAboveNotification(remoteMessage)
                } else {
                    sendNormalNotification(remoteMessage)
                }
            }
        }
    }

    private fun sendNormalNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val notification = remoteMessage.notification
        val i = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, OpeningActivity::class.java)
        //Bundle bundle=new Bundle();
        //bundle.putString("chatUserId",user);
        //intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT)
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(icon!!.toInt())
            .setContentText(body)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setSound(defSoundUri)
            .setShowWhen(true)
            .setContentIntent(pIntent)
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        var j = 0
        if (i > 0) {
            j = 1
        }
        notificationManager.notify(j, builder.build())
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun sendOAndAboveNotification(remoteMessage: RemoteMessage) {
        val user = remoteMessage.data["user"]
        val icon = remoteMessage.data["icon"]
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val notification = remoteMessage.notification
        val i = user!!.replace("[\\D]".toRegex(), "").toInt()
        val intent = Intent(this, OpeningActivity::class.java)
        //Bundle bundle=new Bundle();
        //bundle.putString("chatUserId",user);
        //intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT)
        val defSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification1 = OreoNotification(this)
        val builder: Notification.Builder =
            notification1.getONotifications(title, body, pIntent, defSoundUri, icon!!)
                .setShowWhen(true)
        var j = 0
        if (i > 0) {
            j = 1
        }
        notification1.manager?.notify(j, builder.build())
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            updateToken(s)
        }
    }

    private fun updateToken(tokenRefresh: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val ref = FirebaseFirestore.getInstance().collection("Tokens")

        val token = Token(tokenRefresh)
        ref.document(user!!.uid).set(token)
    }

    companion object {
        private const val ADMIN_CHANNEL_ID = "admin_channel"
    }
}