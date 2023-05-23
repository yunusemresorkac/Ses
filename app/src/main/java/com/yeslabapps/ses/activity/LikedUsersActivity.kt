package com.yeslabapps.ses.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.ActivityLikedUsersBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LikedUsersActivity : AppCompatActivity(),UserClick {

    private lateinit var binding: ActivityLikedUsersBinding
    private var userList: ArrayList<User>? = null
    private var userAdapter: UserAdapter? = null
    private var idlist: ArrayList<String>? = null
    private var firebaseUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLikedUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val voiceId = intent.getStringExtra("voiceIdForLikes")

        initRecycler()
        getLikedUsersOfVoice(voiceId!!)

    }

    private fun initRecycler() {
        idlist = ArrayList()
        userList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        userAdapter = UserAdapter(userList!!, this,this)
        binding.recyclerView.adapter = userAdapter
    }

    private fun getLikedUsersOfVoice(voiceId: String) {
        // Likes koleksiyonuna ve belirli gönderi belgesine referans oluşturun
        val likesCollection = FirebaseFirestore.getInstance().collection("Likes")
        val postLikesDocument = likesCollection.document(voiceId)

        // Gönderiyi beğenen kullanıcıların koleksiyonunu alın
        postLikesDocument.collection("Users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val userId = document.id
                    idlist?.add(userId)
                    println("Beğenen kullanıcı ID: $userId")
                }
                showUsers()
            }
            .addOnFailureListener { e ->
                println("Beğenen kullanıcıları alırken bir hata oluştu: $e")
            }
    }

    private fun showUsers(){
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val user: User? = d.toObject(User::class.java)
                            if (user != null) {
                                for (id in idlist!!) {
                                    if (user.userId.equals(id)) {
                                        userList?.add(user)

                                    }
                                }
                            }
                        }
                        userAdapter?.notifyDataSetChanged();

                    }
                }.addOnFailureListener { }
        }
    }

    override fun followUser(user: User) {
        FollowManager().followUser(firebaseUser!!.uid,user.userId)

    }





}