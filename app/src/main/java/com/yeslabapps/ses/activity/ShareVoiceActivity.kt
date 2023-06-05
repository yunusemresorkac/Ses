package com.yeslabapps.ses.activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.yeslabapps.ses.R
import com.yeslabapps.ses.controller.DummyMethods
import com.yeslabapps.ses.databinding.ActivityShareVoiceBinding
import com.yeslabapps.ses.util.Constants
import com.yeslabapps.ses.util.NetworkChangeListener
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


class ShareVoiceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityShareVoiceBinding


    private val PICK_AUDIO_REQUEST = 1

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private  var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var pause:Boolean = false
    private var audioUri: Uri? = null
    private lateinit var firebaseUser: FirebaseUser


    private var voiceTime : Long = 0
    private val MAX_LIST_SIZE = 5
    private val tagList = mutableListOf<String>()
    private lateinit var textContainer: LinearLayout
    private val firebaseViewModel by viewModel<FirebaseViewModel>()

    private val networkChangeListener = NetworkChangeListener()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShareVoiceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        textContainer = findViewById(R.id.textContainer)


        binding.voiceTitleEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    val remainingChars = Constants.LENGTH_OF_VOICE_TITLE - s.length
                    binding.characterCount.text = remainingChars.toString()
                }
            }
        })



        binding.sendVoiceBtn.setOnClickListener {
            if (binding.voiceTitleEt.text.isNotEmpty() && audioUri!=null  ){
                saveAudioToFirestore(audioUri!!,binding.voiceTitleEt.text.toString().trim())
            }else{
                DummyMethods.showCookie(this,"Please compelete all fields!","")
            }
        }


        binding.selectVoiceBtn.setOnClickListener {
            if (DummyMethods.validatePermission(this)){
                selectAudio()
            }
        }



        binding.playBtn.setOnClickListener{
            play()
        }

        // Pause the media player
        binding.pauseBtn.setOnClickListener {
            pause()
        }

        // Seek bar change listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer?.seekTo(i * 1000)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })



        binding.tagsEt.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                addItemToList()
                return@setOnKeyListener true
            }
            false
        }

        updateTextViews()

    }

    val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }
    // Creating an extension property to get media player current position in seconds
    val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }


    private fun pause(){
        if(mediaPlayer!!.isPlaying){
            mediaPlayer!!.pause()
            pause = true
            binding.playBtn.visibility = View.VISIBLE
            binding.pauseBtn.visibility = View.GONE
        }
    }

    private fun play(){
        if(pause){
            mediaPlayer?.seekTo(mediaPlayer!!.currentPosition)
            mediaPlayer?.start()
            pause = false
        }else{

            mediaPlayer = MediaPlayer.create(applicationContext,audioUri)
            mediaPlayer!!.start()

        }
        initializeSeekBar()
        binding.playBtn.visibility = View.GONE
        binding.pauseBtn.visibility = View.VISIBLE

        mediaPlayer?.setOnCompletionListener {
            binding.playBtn.visibility = View.VISIBLE
            binding.pauseBtn.visibility = View.GONE
        }
    }


    private fun initializeSeekBar() {
        binding.seekBar.max = mediaPlayer!!.seconds

        runnable = Runnable {
            binding.seekBar.progress = mediaPlayer!!.currentSeconds

            binding.tvPass.text = "${mediaPlayer!!.currentSeconds} sec"
            val diff = mediaPlayer!!.seconds - mediaPlayer!!.currentSeconds
            binding.tvDue.text = "$diff sec"

            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }


    private fun addItemToList() {
        val inputText = binding.tagsEt.text.toString().trim()
        if (inputText.isNotEmpty()) {
            tagList.add(0, inputText)
            if (tagList.size > MAX_LIST_SIZE) {
                tagList.removeAt(MAX_LIST_SIZE)
            }
            binding.tagsEt.text.clear()
            updateTextViews()
        }
    }

    private fun updateTextViews() {
        textContainer.removeAllViews()
        for (item in tagList) {
            val textView = TextView(this)
            textView.text = "$item (❌)"
            textView.textSize = 15F
            textView.setTextColor(resources.getColor(R.color.white))
            textView.setOnClickListener { view ->
                val clickedItem = (view as TextView).text.toString().removeSuffix(" (❌)")
                tagList.remove(clickedItem)
                updateTextViews()
            }
            textContainer.addView(textView)
        }
    }

    private fun selectAudio() {
        val intent = Intent()
        intent.type = "audio/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Audio"), PICK_AUDIO_REQUEST)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val duration: Long? = DummyMethods.getSelectedAudioDuration(this, data.data!!)
            val seconds = duration!! / 1000
            if (seconds <= Constants.MAXIMUM_SECOND){
                voiceTime = seconds
                audioUri = data.data
                binding.previewLayout.visibility = View.VISIBLE
                play()
            }
            else{
                recreate()
                DummyMethods.showCookie(this,"You can upload a maximum of 120 seconds of voice!","")
            }
        }


    }

    private fun saveMyVoices(voiceId: String, voice : Any){
        firestore.collection("MyVoices").document(firebaseUser.uid).collection("Voices").document(voiceId)
            .set(voice)
    }



    private fun saveAudioToFirestore(audioUri: Uri, voiceTitle: String) {
        val progressDialog = ProgressDialog(this@ShareVoiceActivity)
        progressDialog.show()
        progressDialog.setCancelable(false)
        // Storage referansını al
        val storageRef = storage.reference
        val randomString: String = DummyMethods.generateRandomString(12)

        val filePath = storageRef.child("Voices/$voiceTitle - $randomString")

        filePath.putFile(audioUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        val date = Date()
                        val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
                        val strDate: String = sdf.format(date)

                        firebaseViewModel.getUserCountry(firebaseUser.uid) { country ->

                            val voice = hashMapOf(
                                "voiceTitle" to voiceTitle,
                                "voiceUrl" to downloadUri.toString(),
                                "voiceId" to randomString,
                                "publisherId" to firebaseUser.uid,
                                "time" to strDate,
                                "duration" to voiceTime,
                                "tags" to tagList,
                                "countOfLikes" to 0,
                                "relatedCountry" to  country,
                                "listened" to 0
                            )
                            firestore.collection("Voices")
                                .document(randomString)
                                .set(voice)
                                .addOnSuccessListener {
                                    saveMyVoices(randomString,voice)
                                    progressDialog.dismiss()
                                    finish()
                                }
                                .addOnFailureListener {
                                }

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
                progressDialog.setMessage("Loading... $currentProgress%")
            }


    }



    private fun checkForSend(){
        binding.sendVoiceBtn.isEnabled = audioUri != null
    }



    override fun onResume() {
        super.onResume()
        checkForSend()

    }

    override fun onDestroy() {
        super.onDestroy()
        binding.seekBar.progress = 0
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        if (mediaPlayer!=null){
            handler.removeCallbacks(runnable)

        }
        mediaPlayer = null


    }

    override fun onStart() {
        checkForSend()
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeListener, intentFilter)
        super.onStart()
    }

    override fun onStop() {
        unregisterReceiver(networkChangeListener)
        super.onStop()
    }


}