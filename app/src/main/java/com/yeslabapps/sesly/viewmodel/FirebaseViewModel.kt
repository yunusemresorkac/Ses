package com.yeslabapps.sesly.viewmodel


import android.app.Application
import android.app.ProgressDialog
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

import com.yeslabapps.sesly.databinding.ActivityProfileBinding
import com.yeslabapps.sesly.databinding.FragmentProfileBinding
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.repo.FirebaseRepo


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

    fun getUserInfo(userId: String?, binding: FragmentProfileBinding,pd : ProgressDialog){
        repo.getUserInfo(userId, binding,pd)
    }
    fun getUserInfoForActivity(myId : String, userId: String, binding: ActivityProfileBinding,pd : ProgressDialog){
        repo.getUserInfoForActivity(myId,userId, binding,pd)
    }

    fun getMyVoices(userId: String){
        repo.getMyVoices(userId)
    }

    fun getUserCountry(userId: String, callback: (String) -> Unit){
        return repo.getUserCountry(userId,callback)
    }

    fun updateLastSeen(userId : String){
        repo.updateLastSeen(userId)
    }

    fun updateToken(token: String,userId: String){
        repo.updateToken(token, userId)
    }

    fun getUserName(userId: String, callback: (String) -> Unit){
        repo.getUserName(userId, callback)
    }



//    fun getRecommendedUsers(){
//        repo.getRecommendedUsers()
//    }


    fun getUserType(userId: String, callback: (Int?) -> Unit){
        repo.getUserType(userId,callback)
    }

    fun getAllVoices():MutableLiveData<List<Voice>?>{
        return liveData
    }

//    fun getAllUsers():MutableLiveData<List<User>?>{
//        return liveDataUsers
//    }





}