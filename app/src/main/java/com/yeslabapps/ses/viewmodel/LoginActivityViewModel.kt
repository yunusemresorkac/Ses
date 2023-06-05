package com.yeslabapps.ses.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.yeslabapps.ses.repo.LoginActivityRepo


class LoginActivityViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepo: LoginActivityRepo = LoginActivityRepo()
    val errorMessage: LiveData<String> = userRepo.error

//    fun createUser(
//        deviceId: String?,
//        context: Activity?,
//        username: String?,
//        email: String?,
//        password: String?,
//        userId : String,
//        country : String,
//        bio :String,
//        firstName : String,
//        lastName : String
//    ) {
//        userRepo.createUser(deviceId, context!!, username!!,
//             email!!, password!!, userId,country,bio,firstName,lastName)
//    }

    fun loginUser(context: Activity?, email: String?, password: String?, auth: FirebaseAuth?) {
        userRepo.loginUser(context!!, email, password, auth!!)
    }
}