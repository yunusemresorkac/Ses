package com.yeslabapps.ses.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import androidx.lifecycle.ViewModel
import com.yeslabapps.ses.databinding.FragmentProfileBinding
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.repo.FirebaseRepo


class FirebaseViewModel(application: Application) : AndroidViewModel(application) {
    private val repo: FirebaseRepo = FirebaseRepo()
    private val liveData: MutableLiveData<List<Voice>?> = repo.getAllVoices()


    fun checkForFollowers(userId: String){
        repo.checkForFollowers(userId)
    }


    fun getVoicesByTopic(topicName : String){
        repo.getVoicesByTopic(topicName)

    }

    fun getUserInfo(userId: String?, binding: FragmentProfileBinding){
        repo.getUserInfo(userId, binding)
    }

    fun getMyVoices(userId: String){
        repo.getMyVoices(userId)
    }



    fun getAllVoices():MutableLiveData<List<Voice>?>{
        return liveData
    }






}