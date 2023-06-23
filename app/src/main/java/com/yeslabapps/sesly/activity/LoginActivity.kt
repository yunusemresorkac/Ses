package com.yeslabapps.sesly.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.databinding.ActivityLoginBinding
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.util.NetworkChangeListener
import com.yeslabapps.sesly.viewmodel.LoginActivityViewModel
import org.aviran.cookiebar2.CookieBar
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding


    private lateinit var firebaseAuth: FirebaseAuth

    private val viewModel by viewModel<LoginActivityViewModel>()
    private var countryList: MutableList<String>? = null

    private lateinit var countryName : String

    private val networkChangeListener = NetworkChangeListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()



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
                    .trim().isNotEmpty()
                && countryName.isNotEmpty()
            ) {
                register()

            }
        }

        binding.btnLogin.setOnClickListener { login() }

        binding.forgotPassword.setOnClickListener {
            val dialog = Dialog(this@LoginActivity)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.bottom_forgot_password)

            dialog.show()
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.GRAY))
            dialog.window?.setGravity(Gravity.BOTTOM)

            val editText: EditText = dialog.findViewById(com.yeslabapps.sesly.R.id.forgotPasswordEt)
            val button: Button = dialog.findViewById(com.yeslabapps.sesly.R.id.sendPasswordLink)

            button.setOnClickListener {
                val mail = editText.text.toString().trim { it <= ' ' }
                if (mail.length > 1) {
                    firebaseAuth.sendPasswordResetEmail(mail)
                        .addOnSuccessListener {
                            dialog.dismiss()
                            CookieBar.build(this@LoginActivity)
                                .setTitle("Please check your mail and spam box!")
                                .setCookiePosition(CookieBar.TOP) // Cookie will be displayed at the bottom
                                .show()
                        }
                        .addOnFailureListener {
                            dialog.dismiss()
                        }
                }
            }

        }



        initCountrySpinner()

    }
    private fun initCountrySpinner() {

        countryList = ArrayList()

        countryList?.add("Afghanistan")
        countryList?.add("Albania")
        countryList?.add("Algeria")
        countryList?.add("Andorra")
        countryList?.add("Angola")
        countryList?.add("Antigua and Barbuda")
        countryList?.add("Argentina")
        countryList?.add("Armenia")
        countryList?.add("Australia")
        countryList?.add("Austria")
        countryList?.add("Azerbaijan")
        countryList?.add("Bahamas")
        countryList?.add("Bahrain")
        countryList?.add("Bangladesh")
        countryList?.add("Barbados")
        countryList?.add("Belarus")
        countryList?.add("Belgium")
        countryList?.add("Belize")
        countryList?.add("Benin")
        countryList?.add("Bhutan")
        countryList?.add("Bolivia")
        countryList?.add("Bosnia and Herzegovina")
        countryList?.add("Botswana")
        countryList?.add("Brazil")
        countryList?.add("Brunei")
        countryList?.add("Bulgaria")
        countryList?.add("Burkina Faso")
        countryList?.add("Burundi")
        countryList?.add("Cabo Verde")
        countryList?.add("Cambodia")
        countryList?.add("Cameroon")
        countryList?.add("Canada")
        countryList?.add("Central African Republic")
        countryList?.add("Chad")
        countryList?.add("Chile")
        countryList?.add("China")
        countryList?.add("Colombia")
        countryList?.add("Comoros")
        countryList?.add("Congo, Democratic Republic of the")
        countryList?.add("Congo, Republic of the")
        countryList?.add("Costa Rica")
        countryList?.add("Côte d'Ivoire")
        countryList?.add("Croatia")
        countryList?.add("Cuba")
        countryList?.add("Cyprus")
        countryList?.add("Czech Republic")
        countryList?.add("Denmark")
        countryList?.add("Djibouti")
        countryList?.add("Dominica")
        countryList?.add("Dominican Republic")
        countryList?.add("East Timor")
        countryList?.add("Ecuador")
        countryList?.add("Egypt")
        countryList?.add("El Salvador")
        countryList?.add("Equatorial Guinea")
        countryList?.add("Eritrea")
        countryList?.add("Estonia")
        countryList?.add("Eswatini")
        countryList?.add("Ethiopia")
        countryList?.add("Fiji")
        countryList?.add("Finland")
        countryList?.add("France")
        countryList?.add("Gabon")
        countryList?.add("Gambia")
        countryList?.add("Georgia")
        countryList?.add("Germany")
        countryList?.add("Ghana")
        countryList?.add("Greece")
        countryList?.add("Grenada")
        countryList?.add("Guatemala")
        countryList?.add("Guinea")
        countryList?.add("Guinea-Bissau")
        countryList?.add("Guyana")
        countryList?.add("Haiti")
        countryList?.add("Honduras")
        countryList?.add("Hungary")
        countryList?.add("Iceland")
        countryList?.add("India")
        countryList?.add("Indonesia")
        countryList?.add("Iran")
        countryList?.add("Iraq")
        countryList?.add("Ireland")
        countryList?.add("Israel")
        countryList?.add("Italy")
        countryList?.add("Jamaica")
        countryList?.add("Japan")
        countryList?.add("Jordan")
        countryList?.add("Kazakhstan")
        countryList?.add("Kenya")
        countryList?.add("Kiribati")
        countryList?.add("Korea, North")
        countryList?.add("Korea, South")
        countryList?.add("Kosovo")
        countryList?.add("Kuwait")
        countryList?.add("Kyrgyzstan")
        countryList?.add("Laos")
        countryList?.add("Latvia")
        countryList?.add("Lebanon")
        countryList?.add("Lesotho")
        countryList?.add("Liberia")
        countryList?.add("Libya")
        countryList?.add("Liechtenstein")
        countryList?.add("Lithuania")
        countryList?.add("Luxembourg")
        countryList?.add("Madagascar")
        countryList?.add("Malawi")
        countryList?.add("Malaysia")
        countryList?.add("Maldives")
        countryList?.add("Mali")
        countryList?.add("Malta")
        countryList?.add("Marshall Islands")
        countryList?.add("Mauritania")
        countryList?.add("Mauritius")
        countryList?.add("Mexico")
        countryList?.add("Micronesia")
        countryList?.add("Moldova")
        countryList?.add("Monaco")
        countryList?.add("Mongolia")
        countryList?.add("Montenegro")
        countryList?.add("Morocco")
        countryList?.add("Mozambique")
        countryList?.add("Myanmar")
        countryList?.add("Namibia")
        countryList?.add("Nauru")
        countryList?.add("Nepal")
        countryList?.add("Netherlands")
        countryList?.add("New Zealand")
        countryList?.add("Nicaragua")
        countryList?.add("Niger")
        countryList?.add("Nigeria")
        countryList?.add("North Macedonia")
        countryList?.add("Norway")
        countryList?.add("Oman")
        countryList?.add("Pakistan")
        countryList?.add("Palau")
        countryList?.add("Panama")
        countryList?.add("Papua New Guinea")
        countryList?.add("Paraguay")
        countryList?.add("Peru")
        countryList?.add("Philippines")
        countryList?.add("Poland")
        countryList?.add("Portugal")
        countryList?.add("Qatar")
        countryList?.add("Romania")
        countryList?.add("Russia")
        countryList?.add("Rwanda")
        countryList?.add("Saint Kitts and Nevis")
        countryList?.add("Saint Lucia")
        countryList?.add("Saint Vincent and the Grenadines")
        countryList?.add("Samoa")
        countryList?.add("San Marino")
        countryList?.add("Sao Tome and Principe")
        countryList?.add("Saudi Arabia")
        countryList?.add("Senegal")
        countryList?.add("Serbia")
        countryList?.add("Seychelles")
        countryList?.add("Sierra Leone")
        countryList?.add("Singapore")
        countryList?.add("Slovakia")
        countryList?.add("Slovenia")
        countryList?.add("Solomon Islands")
        countryList?.add("Somalia")
        countryList?.add("South Africa")
        countryList?.add("South Sudan")
        countryList?.add("Spain")
        countryList?.add("Sri Lanka")
        countryList?.add("Sudan")
        countryList?.add("Suriname")
        countryList?.add("Sweden")
        countryList?.add("Switzerland")
        countryList?.add("Syria")
        countryList?.add("Taiwan")
        countryList?.add("Tajikistan")
        countryList?.add("Tanzania")
        countryList?.add("Thailand")
        countryList?.add("Togo")
        countryList?.add("Tonga")
        countryList?.add("Trinidad and Tobago")
        countryList?.add("Tunisia")
        countryList?.add("Turkey")
        countryList?.add("Turkmenistan")
        countryList?.add("Tuvalu")
        countryList?.add("Uganda")
        countryList?.add("Ukraine")
        countryList?.add("United Arab Emirates")
        countryList?.add("United Kingdom")
        countryList?.add("United States")
        countryList?.add("Uruguay")
        countryList?.add("Uzbekistan")
        countryList?.add("Vanuatu")
        countryList?.add("Vatican City")
        countryList?.add("Venezuela")
        countryList?.add("Vietnam")
        countryList?.add("Yemen")
        countryList?.add("Zambia")
        countryList?.add("Zimbabwe")

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
        val pd = ProgressDialog(this@LoginActivity,R.style.CustomDialog)
        pd.setCancelable(false)
        pd.show()



        firebaseAuth.createUserWithEmailAndPassword(binding.emailRegister.text.toString().trim(),binding.passwordRegister.text.toString().trim()).addOnCompleteListener {

            val collectionRef = FirebaseFirestore.getInstance().collection("Users")
            val query: com.google.firebase.firestore.Query = collectionRef.whereEqualTo("username", binding.usernameRegister.text.toString().trim())
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (task.result.isEmpty){

                        val userId = firebaseAuth.currentUser!!.uid

                        val user = User(binding.usernameRegister.text.toString().trim(),userId, binding.emailRegister.text.toString().trim(),
                            System.currentTimeMillis(), countryName,"",
                           "",0,false,0,0
                        , followerNotify = true,
                            likeNotify = true,
                            joinDateVisibility = true,
                            countryVisibility = true
                        )


                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                            .set(user).addOnSuccessListener {

                                pd.dismiss()
                                sendEmailVerification()
                            }.addOnFailureListener {
                                pd.dismiss()
                                Toast.makeText(
                                    this,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                    }else{
                        pd.dismiss()
                        DummyMethods.showCookie(this,"This username is already taken.","")
                    }


                }
            }
        }







    }

    private fun sendEmailVerification() {
        val user = firebaseAuth.currentUser
        user!!.sendEmailVerification().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.loginLayout.visibility = View.VISIBLE
                binding.registerLayout.visibility = View.GONE
                val email = binding.emailRegister.text.toString().trim()
                val pass  = binding.passwordRegister.text.toString().trim()
                binding.emailLogin.setText(email)
                binding.passwordLogin.setText(pass)
                DummyMethods.showCookie(this@LoginActivity,"Verification email sent to " + user.email,"")


            } else {
                Toast.makeText(this@LoginActivity, "Failed to send verification email.", Toast.LENGTH_SHORT).show()

            }


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