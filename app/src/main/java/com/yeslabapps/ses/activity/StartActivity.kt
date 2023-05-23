package com.yeslabapps.ses.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.yeslabapps.ses.R
import com.yeslabapps.ses.databinding.ActivityStartBinding
import com.yeslabapps.ses.fragment.ExploreFragment
import com.yeslabapps.ses.fragment.HomeFragment
import com.yeslabapps.ses.fragment.ProfileFragment


class StartActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStartBinding
    private var selectorFragment: Fragment? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

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


    }



}