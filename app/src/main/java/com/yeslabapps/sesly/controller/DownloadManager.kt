package com.yeslabapps.sesly.controller

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.yeslabapps.sesly.activity.GetPremiumActivity
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DownloadManager {


    companion object{
        fun downloadVoice(context : Context,voiceUrl: String, voiceName: String,viewModel: FirebaseViewModel,userId : String) {
            viewModel.getUserType(userId){
                if (it==0){
                    context.startActivity(Intent(context,GetPremiumActivity::class.java))
                }else{
                    GlobalScope.launch(Dispatchers.Main) {
                        withContext(Dispatchers.IO) {
                            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(voiceUrl)
                            storageReference.metadata
                                .addOnSuccessListener {
                                    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

                                    val uri: Uri = Uri.parse(voiceUrl)

                                    val request = DownloadManager.Request(uri)

                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, "Sesly/$voiceName.mp3")
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
    }
}
