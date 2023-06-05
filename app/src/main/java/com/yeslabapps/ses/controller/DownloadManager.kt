package com.yeslabapps.ses.controller

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadManager {

    companion object{

        fun downloadVoice(context : Context,voiceUrl: String, voiceName: String) {
            GlobalScope.launch(Dispatchers.Main) {
                withContext(Dispatchers.IO) {
                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(voiceUrl)
                    storageReference.metadata
                        .addOnSuccessListener {
                            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                            val uri: Uri = Uri.parse(voiceUrl)

                            val request = DownloadManager.Request(uri)

                            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "SesApp/$voiceName.mp3")
                            downloadManager.enqueue(request)
                        }.addOnSuccessListener {
                            Toast.makeText(context, "Download Completed.", Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener { e ->
                            Toast.makeText(context, "Something went wrong! " + e.message, Toast.LENGTH_SHORT).show()
                        }
                }


            }

        }



    }
}