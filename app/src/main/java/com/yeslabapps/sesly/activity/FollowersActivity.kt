package com.yeslabapps.sesly.activity

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.adapter.UserAdapter
import com.yeslabapps.sesly.controller.FollowManager
import com.yeslabapps.sesly.databinding.ActivityFollowersBinding
import com.yeslabapps.sesly.interfaces.UserClick
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.util.NetworkChangeListener
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class FollowersActivity : AppCompatActivity(), UserClick {

    private lateinit var binding: ActivityFollowersBinding
    private var firebaseUser : FirebaseUser? = null
    private var idlist: ArrayList<String>? = null
    private var userList: ArrayList<User>? = null
    private var userAdapter: UserAdapter? = null
    private var userId : String? = null
    private val networkChangeListener = NetworkChangeListener()

    private val firebaseViewModel by viewModel<FirebaseViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }


        userId = intent.getStringExtra("followersId")

        firebaseUser = FirebaseAuth.getInstance().currentUser

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
        val followersCollection = FirebaseFirestore.getInstance()
            .collection("Users")
            .document(userId)
            .collection("Followers")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = withContext(Dispatchers.IO) {
                    followersCollection.get().await()
                }

                val followerIds = mutableListOf<String>()

                for (document in querySnapshot.documents) {
                    val followerId = document.id
                    followerIds.add(followerId)
                    println("Kullanıcı $followerId, $userId kullanıcısını takip ediyor.")
                }

                withContext(Dispatchers.Main) {
                    showFollowers(followerIds)
                }
            } catch (e: Exception) {
                // Hata durumunda yapılacak işlemler
            }
        }
    }

    private fun showFollowers(followerIds: List<String>) {
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
        FollowManager().followUser(firebaseUser!!.uid,user.userId,this, firebaseViewModel)
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