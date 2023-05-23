package com.yeslabapps.ses.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.repo.SearchRepo

class SearchViewModel(private val searchRepo:SearchRepo) : ViewModel() {
    val userListLiveData = MutableLiveData<ArrayList<User>>()

    fun searchUsers(username: String) {
        searchRepo.searchUsers(username) { users ->
            userListLiveData.postValue(users as ArrayList<User>?)
        }
    }
}

