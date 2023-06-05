package com.yeslabapps.ses.activity

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.ActivityFollowingBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.util.NetworkChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class FollowingActivity : AppCompatActivity() ,UserClick{


    private lateinit var binding: ActivityFollowingBinding
    private var firebaseUser : FirebaseUser? = null
    private var idlist: ArrayList<String>? = null
    private var userList: ArrayList<User>? = null
    private var userAdapter: UserAdapter? = null
    private var userId : String? = null

    private val networkChangeListener = NetworkChangeListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        userId = intent.getStringExtra("followingId")

        firebaseUser = FirebaseAuth.getInstance().currentUser
        idlist = ArrayList()

        initRecycler()

        getFollowing(userId!!)

    }

    private fun initRecycler() {
        idlist = ArrayList()
        userList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        userAdapter = UserAdapter(userList!!, this,this)
        binding.recyclerView.adapter = userAdapter
    }

    private fun getFollowing(userId: String) {
        val followersCollection = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .collection("Followings")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = withContext(Dispatchers.IO) {
                    followersCollection.get().await()
                }

                val followerIds = mutableListOf<String>()

                for (document in querySnapshot.documents) {
                    val followerId = document.id
                    followerIds.add(followerId)
                    println("Kullanıcı $followerId, $userId kullanıcısını takip ediyor222.")
                }

                withContext(Dispatchers.Main) {
                    showFollowing(followerIds)
                }
            } catch (e: Exception) {
                // Hata durumunda yapılacak işlemler
            }
        }
    }

    private fun showFollowing(followerIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .whereIn("userId", followerIds)
                        .get()
                        .await()
                }

                for (document in querySnapshot.documents) {
                    val user: User? = document.toObject(User::class.java)
                    user?.let {
                        userList?.add(user)
                    }
                }

                withContext(Dispatchers.Main) {
                    // User listesini kullanarak gerekli işlemleri yapabilirsiniz
                    // Örneğin, RecyclerView güncellemesi veya verilerin gösterimi
                    userAdapter?.notifyDataSetChanged();

                }
            } catch (e: Exception) {
                // Hata durumunda yapılacak işlemler
            }
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



    override fun onStart() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeListener, intentFilter)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(networkChangeListener)
        super.onStop()
    }
}