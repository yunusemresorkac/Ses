package com.yeslabapps.ses.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.ProfileActivity
import com.yeslabapps.ses.adapter.UserAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.FragmentSearchUserBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchUserFragment : Fragment(),UserClick {

    private var binding: FragmentSearchUserBinding? = null
    private lateinit var userAdapter: UserAdapter
    private var firebaseUser : FirebaseUser? = null
    private var userList : ArrayList<User>? = null



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchUserBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userList = ArrayList()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)



        binding?.searchEt?.addTextChangedListener(textWatcher)

    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Değişiklik öncesi işlemler
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Değişiklik esnasında işlemler
            val username = s.toString().trim()
            if (username.length>1){
                searchUsers(username)

            }


        }

        override fun afterTextChanged(s: Editable?) {
            // Değişiklik sonrası işlemler
            userList!!.clear()

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun searchUsers(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Users")

            val querySnapshot = usersRef.whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + "\uf8ff")
                .get()
                .await()

            val tempList = ArrayList<User>() // Geçici liste oluştur

            for (document in querySnapshot.documents) {
                val user = document.toObject(User::class.java)
                if (user != null) {
                    tempList.add(user) // Geçici listeye kullanıcıları ekle
                }
            }

            withContext(Dispatchers.Main) {
                userList = tempList // userList'i geçici listeden al

                if (userList!!.isNotEmpty()) {
                    userAdapter = UserAdapter(userList!!, requireContext(), this@SearchUserFragment)
                    binding?.recyclerView?.adapter = userAdapter
                    userAdapter.notifyDataSetChanged()
                } else {
                    // Sonuç bulunamadığında yapılacak işlemler
                    searchUsersByFirstName(query)
                }
            }
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    private suspend fun searchUsersByFirstName(query: String) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("Users")

        val querySnapshot = usersRef.whereGreaterThanOrEqualTo("firstName", query)
            .whereLessThanOrEqualTo("firstName", query + "\uf8ff")
            .get()
            .await()

        val tempList = ArrayList<User>() // Geçici liste oluştur

        for (document in querySnapshot.documents) {
            val user = document.toObject(User::class.java)
            if (user != null) {
                tempList.add(user)
            }
        }

        withContext(Dispatchers.Main) {
            userList = tempList // userList'i geçici listeden al

            userAdapter = UserAdapter(userList!!, requireContext(), this@SearchUserFragment)
            binding?.recyclerView?.adapter = userAdapter
            userAdapter.notifyDataSetChanged()
        }
    }


    override fun followUser(user: User) {
        FollowManager().followUser(firebaseUser!!.uid,user.userId)
    }


    override fun goProfile(user: User) {
        val intent = Intent(context, ProfileActivity::class.java)
        intent.putExtra("userId",user.userId)
        startActivity(intent)
    }



}