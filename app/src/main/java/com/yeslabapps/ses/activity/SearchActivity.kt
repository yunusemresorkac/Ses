package com.yeslabapps.ses.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.databinding.ActivitySearchBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class SearchActivity : AppCompatActivity(),UserClick {

    private lateinit var binding: ActivitySearchBinding
    private lateinit var userAdapter: UserAdapter

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)


        binding.searchUserEt.addTextChangedListener(textWatcher)
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Değişiklik öncesi işlemler
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Değişiklik esnasında işlemler
            val username = s.toString().trim()
            if (username.length>2){
                searchUsers(username)

            }

        }

        override fun afterTextChanged(s: Editable?) {
            // Değişiklik sonrası işlemler
        }
    }


    private fun searchUsers(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Users")

            usersRef.whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = ArrayList<User>()
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            userList.add(user)
                        }
                    }

                    // Eğer kullanıcı adına göre eşleşen kullanıcılar varsa, userList'e eklenir
                    if (userList.isNotEmpty()) {
                        userAdapter = UserAdapter(userList, this@SearchActivity,this@SearchActivity)
                        binding.recyclerView.adapter = userAdapter
                        userAdapter.notifyDataSetChanged()
                    } else {
                        // Kullanıcı adına göre eşleşen kullanıcı yoksa, firstName alanına göre arama yaparız
                        searchUsersByFirstName(query)
                    }
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda işleme geçin
                }
        }


    }

    private fun searchUsersByFirstName(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Users")

            usersRef.whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    val userList = ArrayList<User>()
                    for (document in querySnapshot.documents) {
                        val user = document.toObject(User::class.java)
                        if (user != null) {
                            userList.add(user)
                        }
                    }

                    // firstName alanına göre eşleşen kullanıcılar
                    userAdapter = UserAdapter(userList, this@SearchActivity,this@SearchActivity)
                    binding.recyclerView.adapter = userAdapter
                    userAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    // Hata durumunda işleme geçin
                }
        }


    }



    override fun followUser(user: User) {

    }


    override fun goProfile(user: User) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("userId",user.userId)
        startActivity(intent)
    }

}