package com.yeslabapps.ses.interfaces

import com.google.android.material.button.MaterialButton
import com.yeslabapps.ses.model.User

interface UserClick {

    fun followUser(user : User)

    fun goProfile(user : User){

    }

}