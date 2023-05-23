package com.yeslabapps.ses.controller

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.text.format.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DummyMethods {

    companion object{
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



    }




}