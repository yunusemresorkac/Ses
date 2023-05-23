package com.yeslabapps.ses.repo

import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.model.User

class SearchRepo() {
    private val firestore = FirebaseFirestore.getInstance()

    fun searchUsers(username: String, callback: (List<User>) -> Unit) {
        firestore.collection("Users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val users = querySnapshot.documents.map { documentSnapshot ->
                    val userData = documentSnapshot.toObject(User::class.java)
                    userData?.apply { userId = documentSnapshot.id }
                }.filterNotNull()
                callback(users)
            }
    }



}