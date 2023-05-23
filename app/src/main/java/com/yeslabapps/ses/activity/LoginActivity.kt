package com.yeslabapps.ses.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.databinding.ActivityLoginBinding
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.viewmodel.LoginActivityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding


    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel by viewModel<LoginActivityViewModel>()
    private var countryList: MutableList<String>? = null

    private lateinit var countryName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        if (firebaseAuth.currentUser != null) {
            val intent = Intent(this, StartActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }

        binding.showLogin.setOnClickListener {
            binding.loginLayout.visibility = View.VISIBLE
            binding.registerLayout.visibility = View.GONE
        }
        binding.showRegister.setOnClickListener {
            binding.loginLayout.visibility = View.GONE
            binding.registerLayout.visibility = View.VISIBLE
        }


        binding.btnRegister.setOnClickListener {
            if (binding.emailRegister.text.toString().trim().isNotEmpty() && binding.usernameRegister.text.toString().trim().isNotEmpty() && binding.passwordRegister.text.toString()
                    .trim().isNotEmpty() && binding.firstNameRegister.text.toString().trim().isNotEmpty() && binding.lastNameRegister.text.toString().trim().isNotEmpty()
                && countryName.isNotEmpty()
            ) {
                register()

            }
        }

        binding.btnLogin.setOnClickListener { login() }

        initCountrySpinner()

    }
    private fun initCountrySpinner() {

        countryList = ArrayList()

        countryList?.add("General")
        countryList?.add("Sport")
        countryList?.add("Political")
        countryList?.add("Music")
        countryList?.add("Art")
        countryList?.add("News")

        binding.countrySpinner.item = countryList as List<Any>?

        binding.countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {
                countryName = countryList!![position]
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {}
        }
    }


    private fun login() {
        if (binding.emailLogin.text.toString().trim().isNotEmpty() && binding.passwordLogin.text.toString()
                .trim().isNotEmpty()
        ) {
            viewModel.loginUser(
                this@LoginActivity,
                binding.emailLogin.text.toString().trim(),
                binding.passwordLogin.text.toString().trim(),
                firebaseAuth
            )
        }
    }

    @SuppressLint("HardwareIds")
    private fun register() {

        val pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)
        pd.show()
        val deviceId: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        val collectionRef = FirebaseFirestore.getInstance().collection("Users")
        val query: com.google.firebase.firestore.Query = collectionRef.whereEqualTo("username", binding.usernameRegister.text.toString().trim())
        query.get().addOnCompleteListener { task ->
            if (task.isSuccessful){
                if (task.result.isEmpty){

                    firebaseAuth.createUserWithEmailAndPassword(binding.emailRegister.text.toString().trim(),binding.passwordRegister.text.toString().trim()).addOnCompleteListener {

                        val userId = firebaseAuth.currentUser!!.uid

                        val user = User(binding.usernameRegister.text.toString().trim(),userId, binding.emailRegister.text.toString().trim(),
                            System.currentTimeMillis(), countryName,"",binding.firstNameRegister.text.toString().trim(),binding.lastNameRegister.text.toString().trim(),deviceId)


                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                            .set(user).addOnSuccessListener {

                                val intent = Intent(this, StartActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }.addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    }


                }else{
                    pd.dismiss()
                    Toast.makeText(this,"This username already used", Toast.LENGTH_SHORT).show()
                }
            }
        }







    }



}