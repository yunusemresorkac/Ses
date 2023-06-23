package com.yeslabapps.sesly.activity

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.databinding.ActivityStartBinding
import com.yeslabapps.sesly.fragment.ExploreFragment
import com.yeslabapps.sesly.fragment.HomeFragment
import com.yeslabapps.sesly.fragment.OptionsFragment
import com.yeslabapps.sesly.fragment.ProfileFragment
import com.yeslabapps.sesly.util.Constants
import com.yeslabapps.sesly.util.NetworkChangeListener
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var selectorFragment: Fragment? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseUser : FirebaseUser? = null
    private val networkChangeListener = NetworkChangeListener()
    private val firebaseViewModel by viewModel<FirebaseViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseUser = FirebaseAuth.getInstance().currentUser
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
        binding.bottom.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> selectorFragment = HomeFragment()
                R.id.navAdd -> showAddDialog()

                R.id.navExplore -> selectorFragment = ExploreFragment()
                R.id.navProfile -> selectorFragment = ProfileFragment()
                R.id.navOptions -> selectorFragment = OptionsFragment()

            }
            if (selectorFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectorFragment!!).commit()
            }
            true
        }


        firebaseViewModel.updateLastSeen(firebaseUser!!.uid)
        firebaseViewModel.updateToken(FirebaseInstanceId.getInstance().token!!, firebaseUser!!.uid)

    }



    private fun showAddDialog() {
        val dialog = Dialog(this,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_add)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()


        val addVoice = dialog.findViewById<CardView>(R.id.addNewVoice)
        val addProfileVoice = dialog.findViewById<CardView>(R.id.addProfileVoice)

        addVoice.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this,ShareVoiceActivity::class.java))
        }


        addProfileVoice.setOnClickListener{
            if (DummyMethods.validatePermission(this)){
                dialog.dismiss()
                selectAudio()
            }

        }



    }
    private fun selectAudio() {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Audio"),1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            val duration: Long? = DummyMethods.getSelectedAudioDuration(this, data.data!!)
            val seconds = duration!! / 1000
            if (seconds <= Constants.MAXIMUM_SECOND_FOR_PROFILE){
                uploadProfileVoice(data.data!!)
            }
            else{
                DummyMethods.showCookie(this,"Maximum 30 seconds!","" )

            }
        }


    }

    private fun uploadProfileVoice(audioUri: Uri){
        val progressDialog = ProgressDialog(this@StartActivity)
        progressDialog.show()
        progressDialog.setCancelable(false)
        val storageRef = storage.reference
        val randomString: String = DummyMethods.generateRandomString(12)

        val filePath = storageRef.child("ProfileVoices/$randomString")
        filePath.putFile(audioUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val map: HashMap<String, Any> = HashMap()
                        map["profileVoice"] = downloadUri.toString()

                        FirebaseFirestore.getInstance().collection("Users")
                            .document(firebaseUser!!.uid)
                            .update(map)
                            .addOnSuccessListener {
                                progressDialog.dismiss()
                            }
                            .addOnFailureListener {
                            }
                    }
                    .addOnFailureListener {
                    }
            }
            .addOnFailureListener {
            }.addOnProgressListener {
                val progress: Double =
                    100.0 * it.bytesTransferred / it.totalByteCount
                val currentProgress = progress.toInt()
                progressDialog.setMessage("Loading: $currentProgress%")
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