package com.yeslabapps.sesly.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.adapter.VoiceAdapter
import com.yeslabapps.sesly.databinding.ActivityMyLikesBinding
import com.yeslabapps.sesly.interfaces.VoiceClick
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.util.NetworkChangeListener
import com.yeslabapps.sesly.viewmodel.MyLikesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

class MyLikesActivity : AppCompatActivity(),VoiceClick {

    private lateinit var binding : ActivityMyLikesBinding

    private val viewModel  by viewModel<MyLikesViewModel>()
    private var voiceList: ArrayList<Voice>? = null
    private var voiceAdapter: VoiceAdapter? = null
    private var firebaseUser : FirebaseUser? = null
    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private val networkChangeListener = NetworkChangeListener()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLikesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }


        firebaseUser = FirebaseAuth.getInstance().currentUser


        initRecycler()
        getMyLikes()


        binding.mainPlayer.playBtn.setOnClickListener { play() }
        binding.mainPlayer.pauseBtn.setOnClickListener { pause() }

        binding.mainPlayer.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer?.seekTo(i * 1000)

                    mediaPlayer?.setOnCompletionListener {
                        binding.mainPlayer.root.visibility = View.GONE
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })


    }

    val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }
    val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }


    private fun initializeSeekBar() {
        binding.mainPlayer.seekBar.max = mediaPlayer!!.seconds

        runnable = Runnable {
            binding.mainPlayer.seekBar.progress = mediaPlayer!!.currentSeconds


            handler.postDelayed(runnable, 1000)

        }
        handler.postDelayed(runnable, 1000)
    }

    private fun play(){
        CoroutineScope(Dispatchers.Main).launch {
            if(pause){
                mediaPlayer?.seekTo(mediaPlayer!!.currentPosition)
                mediaPlayer?.start()
                pause = false
            }else{

                if (mediaPlayer!=null){
                    mediaPlayer!!.stop()
                    mediaPlayer!!.release()
                }
                mediaPlayer!!.start()

            }
            binding.mainPlayer.playBtn.visibility = View.GONE
            binding.mainPlayer.pauseBtn.visibility = View.VISIBLE

            mediaPlayer?.setOnCompletionListener {
                binding.mainPlayer.playBtn.visibility = View.VISIBLE
                binding.mainPlayer.pauseBtn.visibility = View.GONE
            }
        }

    }

    private fun pause(){
        CoroutineScope(Dispatchers.Main).launch {
            if(mediaPlayer!!.isPlaying){

                mediaPlayer!!.pause()
                pause = true
                binding.mainPlayer.playBtn.visibility = View.VISIBLE
                binding.mainPlayer.pauseBtn.visibility = View.GONE
            }
        }

    }

    private fun initRecycler() {
        voiceList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, this,this)
        binding.recyclerView.adapter = voiceAdapter
    }



    @SuppressLint("NotifyDataSetChanged")
    private fun getMyLikes() {
        viewModel.getMyAllLikes().observe(this) { voices ->

            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
        }

        viewModel.checkForFollowers(firebaseUser!!.uid)
    }

    private fun destroyMedia(){
        binding.mainPlayer.seekBar.progress = 0
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        if (mediaPlayer!=null){
            handler.removeCallbacks(runnable)

        }
        mediaPlayer = null
        binding.mainPlayer.root.visibility = View.GONE
    }




    override fun pickVoice(voice: Voice) {
        CoroutineScope(Dispatchers.Main).launch {
            val pd = ProgressDialog(this@MyLikesActivity,R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()
            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()

            withContext(Dispatchers.IO) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@MyLikesActivity, voiceUri)
                    setOnPreparedListener {
                        // MediaPlayer hazır olduğunda yapılacak işlemler
                        binding.mainPlayer.mainVoiceTitle.text = voice.voiceTitle
                        mediaPlayer!!.start()
                        initializeSeekBar()
                        binding.mainPlayer.playBtn.visibility = View.GONE
                        binding.mainPlayer.pauseBtn.visibility = View.VISIBLE
                        binding.mainPlayer.root.visibility = View.VISIBLE
                        pd.dismiss()

                    }
                    prepareAsync()

                }
            }
        }


    }

    override fun seeLikers(voice: Voice) {
        destroyMedia()

        val intent = Intent(this, LikedUsersActivity::class.java)
        intent.putExtra("voiceIdForLikes",voice.voiceId)
        startActivity(intent)
    }

    override fun clickUser(voice: Voice) {
        destroyMedia()

        val intent = Intent(this,ProfileActivity::class.java)
        intent.putExtra("userId",voice.publisherId)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyMedia()
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