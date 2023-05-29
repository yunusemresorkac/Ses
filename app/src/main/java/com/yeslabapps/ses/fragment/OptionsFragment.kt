package com.yeslabapps.ses.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.ses.activity.LoginActivity
import com.yeslabapps.ses.activity.MyLikesActivity
import com.yeslabapps.ses.databinding.FragmentOptionsBinding


class OptionsFragment: Fragment() {

    private var binding: FragmentOptionsBinding? = null
    private var firebaseUser : FirebaseUser? = null
    private var firebaseAuth: FirebaseAuth? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOptionsBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseAuth=FirebaseAuth.getInstance()

        binding?.signOutBtn?.setOnClickListener { signOut()}

        binding?.goLikesBtn?.setOnClickListener { startActivity(Intent(context,MyLikesActivity::class.java)) }

    }

    private fun signOut(){
        firebaseAuth!!.signOut()
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }


}
