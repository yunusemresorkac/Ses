package com.yeslabapps.ses.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.yeslabapps.ses.R
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.ActivityFollowersBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FollowersActivity : AppCompatActivity(), UserClick {

    private lateinit var binding: ActivityFollowersBinding
    private var firebaseUser : FirebaseUser? = null
    private var idlist: ArrayList<String>? = null
    private var userList: ArrayList<User>? = null
    private var userAdapter: UserAdapter? = null
    private var userId : String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("followersId")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        idlist = ArrayList()

        initRecycler()

        getFollowers(userId!!)


    }

    private fun initRecycler() {
        idlist = ArrayList()
        userList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        userAdapter = UserAdapter(userList!!, this,this)
        binding.recyclerView.adapter = userAdapter
    }



    private fun getFollowers(userId: String) {
        val followersCollection = FirebaseFirestore.getInstance().collection("Users").
        document(userId).collection("Followers")

        followersCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val followerId = document.id
                idlist?.add(followerId)
                println("Kullanıcı $followerId, $userId kullanıcısını takip ediyor.")
            }
            showFollowers()
        }
    }


    private fun showFollowers(){
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

    override fun goProfile(user: User) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId",user.userId)
        startActivity(intent)
    }
}