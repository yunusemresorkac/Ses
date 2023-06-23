package com.yeslabapps.sesly.activity

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.yeslabapps.sesly.databinding.ActivityOpeningBinding
import kotlinx.android.synthetic.main.fragment_options.*


class OpeningActivity : AppCompatActivity() {

    private lateinit var binding : ActivityOpeningBinding

    private  var sharedPreferences:SharedPreferences? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOpeningBinding.inflate(layoutInflater)
        setContentView(binding.root)


        sharedPreferences = getSharedPreferences("night",0)
        val booleanValue = sharedPreferences!!.getBoolean("night_mode", false)

        if (booleanValue){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            val editor = sharedPreferences!!.edit()
            editor.putBoolean("night_mode", true)
            editor.commit()
        }else{
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            val editor = sharedPreferences!!.edit()
            editor.putBoolean("night_mode", false)
            editor.commit()
        }

        checkRemoteConfigValue()


    }
    private fun checkRemoteConfigValue() {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        remoteConfig.fetch().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                remoteConfig.activate().addOnCompleteListener { activationTask ->
                    if (activationTask.isSuccessful) {
                        val isAppOpen = remoteConfig.getBoolean("isAppOpen")
                        if (isAppOpen) {
                            if (FirebaseAuth.getInstance().currentUser!=null){
                                val intent = Intent(this, StartActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }else{
                                val intent = Intent(this, LoginActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                            }

                        }
                    } else {
                        // Aktivasyon başarısız oldu
                        // Hata durumunu ele alabilirsiniz
                    }
                }
            } else {
                // Uzaktan yapılandırma getirme işlemi başarısız oldu
                // Hata durumunu ele alabilirsiniz
            }
        }
    }




}