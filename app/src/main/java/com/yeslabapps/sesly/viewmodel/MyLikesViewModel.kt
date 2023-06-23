package com.yeslabapps.sesly.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.repo.MyLikesRepo

class MyLikesViewModel (application: Application) : AndroidViewModel(application) {

    private val repo: MyLikesRepo = MyLikesRepo()
    private val liveData: MutableLiveData<List<Voice>?> = repo.getMyAllLikes()


    fun checkForFollowers(userId: String){
        repo.checkIsMyLike(userId)
    }



    fun getMyAllLikes():MutableLiveData<List<Voice>?>{
        return liveData
    }



}