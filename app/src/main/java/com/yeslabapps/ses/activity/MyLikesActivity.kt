package com.yeslabapps.ses.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.ses.R
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.databinding.ActivityMyLikesBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import com.yeslabapps.ses.viewmodel.MyLikesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyLikesBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
                binding?.mainPlayer?.playBtn?.visibility = View.VISIBLE
                binding?.mainPlayer?.pauseBtn?.visibility = View.GONE
            }
        }

    }


    private fun initRecycler() {
        voiceList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, this,this)
        binding.recyclerView.adapter = voiceAdapter
        val dividerItemDecoration = DividerItemDecoration(binding.recyclerView.context, DividerItemDecoration.VERTICAL)
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
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
        mediaPlayer?.release()
        val voiceUri = voice.voiceUrl.toUri()
        binding.mainPlayer.mainVoiceTitle.text = voice.voiceTitle
        mediaPlayer = MediaPlayer.create(this,voiceUri)
        mediaPlayer!!.start()
        initializeSeekBar()
        binding.mainPlayer.playBtn.visibility = View.GONE
        binding.mainPlayer.pauseBtn.visibility = View.VISIBLE
        binding.mainPlayer.root.visibility = View.VISIBLE


    }

    override fun seeLikers(voice: Voice) {
        destroyMedia()

        val intent = Intent(this, LikedUsersActivity::class.java)
        intent.putExtra("voiceIdForLikes",voice.voiceId)
        startActivity(intent)
    }

    override fun clickUser(voice: Voice) {
        val intent = Intent(this,ProfileActivity::class.java)
        intent.putExtra("userId",voice.publisherId)
        startActivity(intent)
    }


}