package com.yeslabapps.sesly.fragment

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.sesly.activity.EditProfileActivity
import com.yeslabapps.sesly.activity.GetPremiumActivity
import com.yeslabapps.sesly.activity.LoginActivity
import com.yeslabapps.sesly.activity.MyLikesActivity
import com.yeslabapps.sesly.databinding.FragmentOptionsBinding


class OptionsFragment: Fragment() {

    private var binding: FragmentOptionsBinding? = null
    private var firebaseUser : FirebaseUser? = null
    private var firebaseAuth: FirebaseAuth? = null

    private  var sharedPreferences:SharedPreferences? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth=FirebaseAuth.getInstance()


        binding?.editProfileBtn?.setOnClickListener { startActivity(Intent(context,EditProfileActivity::class.java)) }

        binding?.signOutBtn?.setOnClickListener { signOut()}

        binding?.goLikesBtn?.setOnClickListener { startActivity(Intent(context,MyLikesActivity::class.java)) }

        binding?.goPremium?.setOnClickListener { startActivity(Intent(context,GetPremiumActivity::class.java)) }

        sharedPreferences = context?.getSharedPreferences("night",0);

        val booleanValue = sharedPreferences!!.getBoolean("night_mode", true)

        binding?.switchCompat?.isChecked = booleanValue
        binding?.switchCompat?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding!!.switchCompat.isChecked = true
                val editor = sharedPreferences!!.edit()
                editor.putBoolean("night_mode", true)
                editor.commit()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding!!.switchCompat.isChecked = false
                val editor = sharedPreferences!!.edit()
                editor.putBoolean("night_mode", false)
                editor.commit()
            }
        }


    }

    private fun signOut(){
        firebaseAuth!!.signOut()
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

}
