package com.yeslabapps.ses.repo


import android.annotation.SuppressLint
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yeslabapps.ses.databinding.ActivityProfileBinding
import com.yeslabapps.ses.databinding.FragmentProfileBinding
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class FirebaseRepo {

    private var mutableLiveData: MutableLiveData<List<Voice>?> = MutableLiveData()
    private var voiceList: ArrayList<Voice>? = null

    private var  idlist : ArrayList<String>? = null

    fun checkForFollowers(userId: String,){
        idlist = ArrayList()
        val followersCollection = FirebaseFirestore.getInstance().collection("Users").
        document(userId).collection("Followings")

        followersCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val followerId = document.id
                idlist?.add(followerId)
            }
            getVoices()
        }
    }

    private fun getVoices() {
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Voices")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documents = querySnapshot.documents

                    for (document in documents) {
                        val voice = document.toObject(Voice::class.java)
                        if (voice != null ) {
                            for (id in idlist!!) {
                                println("liste ${idlist.toString()}")
                                if (voice.publisherId.equals(id)) {
                                    voiceList?.add(voice)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda işleme geçin
            }

            withContext(Dispatchers.Main) {
                mutableLiveData.postValue(voiceList)
            }
        }


    }

    fun getVoicesByTag(tagName : String){
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices").orderBy("time",Query.Direction.DESCENDING).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.tags!!.contains(tagName)){
                                    voiceList?.add(voice)
                                    println("taga göre liste ${voiceList.toString()}")
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }

    fun getVoicesByCountry(relatedCountry : String){
        CoroutineScope(Dispatchers.IO).launch {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)

            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = 180 * 60 * 60 * 1000 // 1 saatin milisaniye cinsinden değeri
            val oneHourAgo = currentTime - oneHourInMillis
            println("format ${sdf.format(oneHourAgo)}")
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices")
                .whereGreaterThan("time",sdf.format(oneHourAgo))
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->

                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.relatedCountry.equals(relatedCountry)){
                                    voiceList?.add(voice)
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }






    fun getUserInfo(userId: String?, binding: FragmentProfileBinding) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").document(userId!!)
                .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            binding.username.text = "@${user.username} "
                            binding.fullName.text = user.firstName + " "  + user.lastName

                            if (user.profileVoice.isNotEmpty()){
                                binding.playProfileVoiceBtn.visibility = View.VISIBLE
                                binding.profileVoice.text = user.profileVoice
                            }else{
                                binding.playProfileVoiceBtn.visibility = View.GONE
                            }

                        }
                    }
                }
        }


    }

    @SuppressLint("SetTextI18n")
    fun getUserInfoForActivity(userId: String?, binding: ActivityProfileBinding) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").document(userId!!)
                .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            binding.username.text = "@${user.username} "
                            binding.fullName.text = user.firstName + " "  + user.lastName

                            if (user.profileVoice.isNotEmpty()){
                                binding.playProfileVoiceBtn.visibility = View.VISIBLE
                                binding.profileVoice.text = user.profileVoice
                            }else{
                                binding.playProfileVoiceBtn.visibility = View.GONE
                            }

                        }
                    }
                }
        }


    }

    fun getMyVoices(userId: String){
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()

            FirebaseFirestore.getInstance().collection("MyVoices").document(userId)
                .collection("Voices").orderBy("time",Query.Direction.DESCENDING).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                voiceList?.add(voice)
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }


    }

    fun getUserCountry(userId: String, callback: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user: User? = documentSnapshot.toObject(User::class.java)
                    val country = user?.country ?: ""
                    callback(country)
                } else {
                    callback("")
                }
            }
    }




    fun getAllVoices(): MutableLiveData<List<Voice>?> {
        return mutableLiveData
    }



}