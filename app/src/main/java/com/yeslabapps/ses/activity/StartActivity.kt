package com.yeslabapps.ses.activity

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yeslabapps.ses.R
import com.yeslabapps.ses.controller.DummyMethods
import com.yeslabapps.ses.databinding.ActivityStartBinding
import com.yeslabapps.ses.fragment.ExploreFragment
import com.yeslabapps.ses.fragment.HomeFragment
import com.yeslabapps.ses.fragment.ProfileFragment
import com.yeslabapps.ses.util.Constants
import java.text.SimpleDateFormat
import java.util.*


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var selectorFragment: Fragment? = null
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private var firebaseUser : FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                HomeFragment()
            ).commit()
        }
        binding.bottom.itemIconTintList = null
        binding.bottom.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navHome -> selectorFragment = HomeFragment()
                R.id.navAdd -> showAddDialog()

                R.id.navExplore -> selectorFragment = ExploreFragment()
                R.id.navProfile -> selectorFragment = ProfileFragment()

            }
            if (selectorFragment != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, selectorFragment!!).commit()
            }
            true
        }


    }

    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_add)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawableResource(R.color.white)
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
                recreate()
                Toast.makeText(this,"en fazla 30 sn pls", Toast.LENGTH_SHORT).show()
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
                progressDialog.setMessage("Yüklendi: $currentProgress%")
            }
    }


}