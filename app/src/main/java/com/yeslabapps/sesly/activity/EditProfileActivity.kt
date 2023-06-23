package com.yeslabapps.sesly.activity

import android.app.ProgressDialog
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.databinding.ActivityEditProfileBinding
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.util.Constants
import com.yeslabapps.sesly.util.NetworkChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.HashMap

class EditProfileActivity : AppCompatActivity() {


    private lateinit var binding: ActivityEditProfileBinding
    private var firebaseUser : FirebaseUser? = null
    private var firebaseAuth : FirebaseAuth? = null
    private lateinit var progressDialog : ProgressDialog
    private val networkChangeListener = NetworkChangeListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        progressDialog = ProgressDialog(this,R.style.CustomDialog)
        progressDialog.setCancelable(false)
        progressDialog.show()

        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth = FirebaseAuth.getInstance()



        binding.bioEt.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val remainingChars = Constants.LENGTH_OF_BIO - s.length
                    binding.characterCount.text = remainingChars.toString()
                }
            }
        })


        getUserInfo()

        binding.saveBtn.setOnClickListener {
            updateProfile()
        }

        binding.deleteVoiceBtn.setOnClickListener {
            deleteVoice()
        }


    }

    private fun deleteVoice() = CoroutineScope(Dispatchers.Main).launch {
        val pd = ProgressDialog(this@EditProfileActivity,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()
        val map: HashMap<String, Any> = HashMap()

        map["profileVoice"] = ""

        try {
            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
                .update(map).addOnSuccessListener {
                    pd.dismiss()
                    DummyMethods.showCookie(this@EditProfileActivity, "Updated!", "Your Information Has Been Successfully Changed.")
                }.await()
        } catch (e: Exception) {
            // Handle exception
            pd.dismiss()
            finish()
        }
    }


    private fun getUserInfo() = CoroutineScope(Dispatchers.Main).launch {
        val userDocument = FirebaseFirestore.getInstance().collection("Users")
            .document(firebaseUser!!.uid).get().await()

        if (userDocument.exists()) {
            val user: User? = userDocument.toObject(User::class.java)

            user?.let {
                binding.usernameEt.text = it.username
                binding.bioEt.setText(it.bio)


                binding.privateProfile.isChecked = it.privateProfile
                binding.countryShow.isChecked = it.countryVisibility
                binding.showRegisterDate.isChecked = it.joinDateVisibility


                if (it.profileVoice.equals("")){
                    binding.deleteVoiceBtn.visibility = View.GONE
                }else{
                    binding.deleteVoiceBtn.visibility  =View.VISIBLE
                }


                val currentUser = firebaseAuth?.currentUser
                currentUser?.reload()

                val emailText = if (currentUser?.isEmailVerified == true) {
                    "${it.email} ✅"
                } else {
                    it.email
                }
                binding.emailEt.text = emailText
                progressDialog.dismiss()
            }
        }
    }

    private fun updateProfile() = CoroutineScope(Dispatchers.Main).launch {
        val pd = ProgressDialog(this@EditProfileActivity,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()
        val map: HashMap<String, Any> = HashMap()

        map["bio"] = binding.bioEt.text.toString().trim()
        map["privateProfile"] = binding.privateProfile.isChecked
        map["countryVisibility"] = binding.countryShow.isChecked
        map["joinDateVisibility"] = binding.showRegisterDate.isChecked

        try {
            FirebaseFirestore.getInstance().collection("Users").document(firebaseUser!!.uid)
                .update(map).addOnSuccessListener {
                    pd.dismiss()
                    DummyMethods.showCookie(this@EditProfileActivity, "Updated!", "Your Information Has Been Successfully Changed.")
                }.await()
        } catch (e: Exception) {
            // Handle exception
            pd.dismiss()
            finish()
        }
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