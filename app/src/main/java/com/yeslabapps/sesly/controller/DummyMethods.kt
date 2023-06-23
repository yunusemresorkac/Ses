package com.yeslabapps.sesly.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.format.DateUtils
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.yeslabapps.sesly.model.Voice
import org.aviran.cookiebar2.CookieBar
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ln
import kotlin.math.pow
import kotlin.random.Random

class DummyMethods {

    companion object{

        private const val SECOND = 1
        private const val MINUTE = 60 * SECOND
        private const val HOUR = 60 * MINUTE
        private const val DAY = 24 * HOUR
        private const val MONTH = 30 * DAY
        private const val YEAR = 12 * MONTH


        fun generateRandomString(length: Int): String {
            val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
            return (1..length)
                .map { Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
        }


        fun getSelectedAudioDuration(context: Context, audioUri: Uri): Long? {
            context.contentResolver

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, audioUri)
                val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                return durationStr?.toLong()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                retriever.release()
            }

            return null
        }

        fun formatSecondsToMinutes(seconds: Int): String {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60

            return "$minutes.${"%02d".format(remainingSeconds)}"
        }


        fun getTimeAgo(date: String): String? {
            val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
            val fromTimeZone = "UTC"
            sdf.timeZone = TimeZone.getTimeZone(fromTimeZone)
            try {
                val time: Long = sdf.parse(date).time
                val now = System.currentTimeMillis()
                val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
                return ago.toString() + ""
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return ""
        }

        fun validatePermission(context: Context ): Boolean {
            var checkPermission = false
            Dexter.withActivity(context as Activity?)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        checkPermission = true
                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        checkPermission = false
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
            return checkPermission
        }

        fun withSuffix(count: Int): String {
            if (count < 1000) return "" + count
            val exp = (ln(count.toDouble()) / ln(1000.0)).toInt()
            return String.format(
                "%.1f %c",
                count / 1000.0.pow(exp.toDouble()),
                "kMGTPE"[exp - 1]
            )
        }


         fun increaseViewedNumber(voice: Voice){
            val map: HashMap<String, Any> = HashMap()
            map["listened"] = FieldValue.increment(1)

            FirebaseFirestore.getInstance().collection("Voices").document(voice.voiceId)
                .update(map).addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("MyVoices").document(voice.publisherId).collection("Voices").document(voice.voiceId)
                        .update(map)
                }
        }


        fun showCookie(context: Context,title : String,message : String){
            CookieBar.build(context as Activity?)
                .setTitle(title)
                .setMessage(message)
                .show()
        }


        fun toTimeAgo(time : Long): String {
            val now = System.currentTimeMillis()

            val diff = (now - time) / 1000

            return when {
                diff < MINUTE -> "Just now"
                diff < 2 * MINUTE -> "a minute ago"
                diff < 60 * MINUTE -> "${diff / MINUTE} minutes ago"
                diff < 2 * HOUR -> "an hour ago"
                diff < 24 * HOUR -> "${diff / HOUR} hours ago"
                diff < 2 * DAY -> "yesterday"
                diff < 30 * DAY -> "${diff / DAY} days ago"
                diff < 2 * MONTH -> "a month ago"
                diff < 12 * MONTH -> "${diff / MONTH} months ago"
                diff < 2 * YEAR -> "a year ago"
                else -> "${diff / YEAR} years ago"
            }
        }



    }




}