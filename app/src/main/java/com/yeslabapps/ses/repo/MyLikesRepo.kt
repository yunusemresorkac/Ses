package com.yeslabapps.ses.repo

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.yeslabapps.ses.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyLikesRepo {

    private var mutableLiveData: MutableLiveData<List<Voice>?> = MutableLiveData()
    private var voiceList: ArrayList<Voice>? = null

    private var  idlist : ArrayList<String>? = null


    fun checkIsMyLike(userId :String){
        idlist = ArrayList()
        val myLikesCollection = FirebaseFirestore.getInstance().collection("MyLikes").document(userId).collection("Voices")

        myLikesCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val likeId = document.id
                idlist?.add(likeId)
            }
            getMyLikedVoices()
        }
    }

    private fun getMyLikedVoices() {
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices").orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                for (id in idlist!!) {
                                    if (voice.voiceId.equals(id)){
                                        println("liste ${idlist.toString()}")
                                        voiceList?.add(voice)
                                    }


                                }

                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }

    }

    fun getMyAllLikes(): MutableLiveData<List<Voice>?> {
        return mutableLiveData
    }

}