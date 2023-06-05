package com.yeslabapps.ses.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.google.firebase.firestore.DocumentSnapshot
import com.yeslabapps.ses.databinding.ActivityProfileBinding
import com.yeslabapps.ses.databinding.FragmentProfileBinding
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.repo.FirebaseRepo


class FirebaseViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: FirebaseRepo = FirebaseRepo()
    private val liveData: MutableLiveData<List<Voice>?> = repo.getAllVoices()


    fun checkForFollowers(userId: String){
        repo.checkForFollowers(userId)
    }


    fun getVoicesByTag(tagName : String){
        repo.getVoicesByTag(tagName)

    }

    fun getVoicesByCountry(relatedCountry : String){
        repo.getVoicesByCountry(relatedCountry)
    }

    fun getVoicesByCountryMostListened(relatedCountry: String){
        repo.getVoicesByCountryMostListened(relatedCountry)
    }

    fun getVoicesByCountryMostLiked(relatedCountry: String){
        repo.getVoicesByCountryMostLiked(relatedCountry)
    }

    fun getUserInfo(userId: String?, binding: FragmentProfileBinding){
        repo.getUserInfo(userId, binding)
    }
    fun getUserInfoForActivity(myId : String, userId: String, binding: ActivityProfileBinding){
        repo.getUserInfoForActivity(myId,userId, binding)
    }

    fun getMyVoices(userId: String){
        repo.getMyVoices(userId)
    }

    fun getUserCountry(userId: String, callback: (String) -> Unit){
        return repo.getUserCountry(userId,callback)
    }


    fun getAllVoices():MutableLiveData<List<Voice>?>{
        return liveData
    }






}