package com.yeslabapps.ses.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.yeslabapps.ses.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding : ActivityProfileBinding
    private var userId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        userId = intent.getStringExtra("userId")


    }
}