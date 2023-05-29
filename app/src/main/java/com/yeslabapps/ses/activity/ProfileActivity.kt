package com.yeslabapps.ses.activity

import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.ses.R
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.controller.DummyMethods
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.ActivityProfileBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileActivity : AppCompatActivity(), VoiceClick {

    private lateinit var binding : ActivityProfileBinding
    private var userId : String? = null
    private val viewModel by viewModel<FirebaseViewModel>()
    private lateinit var firebaseUser: FirebaseUser
    private var voiceList: ArrayList<Voice>? = null
    private var voiceAdapter: VoiceAdapter? = null
    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()

    private  var mediaPlayerProfile : MediaPlayer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
        binding.toolbar.setNavigationOnClickListener { finish() }



        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        userId = intent.getStringExtra("userId")


        initRecycler()

        getMyVoices()
        getUserInfo()


        binding.mainPlayer.playBtn.setOnClickListener { play() }
        binding.mainPlayer.pauseBtn.setOnClickListener { pause() }

        binding.playProfileVoiceBtn.setOnClickListener { playProfileVoice() }
        binding.pauseProfileVoiceBtn.setOnClickListener { pauseProfileVoice() }



        binding.followers.setOnClickListener { startActivity(Intent(this,FollowersActivity::class.java).putExtra("followersId",userId)) }
        binding.followings.setOnClickListener { startActivity(Intent(this,FollowingActivity::class.java).putExtra("followingId",userId)) }

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


        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 ) {
                    if (voiceList?.size!! >4){
                        binding.followLay.visibility = View.GONE

                    }
                } else if (dy < 0 ) {
                    binding.followLay.visibility = View.VISIBLE



                }
            }
        })

        followInfo()


    }

    private fun followInfo(){
        FollowManager().getFollowerCount(userId!!, binding.followers)
        FollowManager().getFollowingCount(userId!!, binding.followings)

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


    private fun getUserInfo(){
        viewModel.getUserInfoForActivity(userId, binding)
    }

    private fun getMyVoices() {
        viewModel.getAllVoices().observe(this) { voices ->

            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()

            if (voiceList?.size!! >0){
                binding.totalVoices.text = " Voices \n${voiceList?.size}"
            }else{
                binding.totalVoices.text = " Voices \n0"
            }
        }

        viewModel.getMyVoices(userId!!)
        if (voiceList?.size!! >0){
            binding.totalVoices.text = " Voices \n${voiceList?.size}"
        }else{
            binding.totalVoices.text = " Voices \n0"
        }

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

    private fun playProfileVoice(){
        CoroutineScope(Dispatchers.Main).launch {
            val uri = binding.profileVoice.text.toString().toUri()
            if (mediaPlayerProfile == null) {
                mediaPlayerProfile = MediaPlayer.create(this@ProfileActivity, uri)
                mediaPlayerProfile?.setOnCompletionListener {
                    // Ses tamamlandığında tekrar başa sar
                    mediaPlayerProfile?.seekTo(0)
                    pauseProfileVoice()
                }
            }
            mediaPlayerProfile?.start()
            binding.playProfileVoiceBtn.visibility = View.GONE
            binding.pauseProfileVoiceBtn.visibility = View.VISIBLE

        }

    }

    private fun pauseProfileVoice(){
        CoroutineScope(Dispatchers.Main).launch {
            mediaPlayerProfile?.pause()
            mediaPlayerProfile?.seekTo(0)
            binding?.playProfileVoiceBtn?.visibility = View.VISIBLE
            binding?.pauseProfileVoiceBtn?.visibility = View.GONE
        }

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





    override fun pickVoice(voice: Voice) {
        CoroutineScope(Dispatchers.Main).launch {
            DummyMethods.increaseViewedNumber(voice)

            val pd = ProgressDialog(this@ProfileActivity,R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()
            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()

            withContext(Dispatchers.IO) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@ProfileActivity, voiceUri)
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
        val intent = Intent(this@ProfileActivity, LikedUsersActivity::class.java)
        intent.putExtra("voiceIdForLikes",voice.voiceId)
        startActivity(intent)
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



    override fun onDestroy() {
        super.onDestroy()
        binding.mainPlayer.seekBar.progress = 0
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        if (mediaPlayer!=null){
            handler.removeCallbacks(runnable)

        }
        mediaPlayer = null


    }


}