package com.yeslabapps.ses.activity

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.SeekBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.R
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.ActivityProfileBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

        firebaseUser = FirebaseAuth.getInstance().currentUser!!


        userId = intent.getStringExtra("userId")


        initRecycler()

        getMyVoices()
        getUserInfo()

        binding.seeMyLikes.setOnClickListener {  startActivity(Intent(this,MyLikesActivity::class.java)) }

        binding.mainPlayer.playBtn.setOnClickListener { play() }
        binding.mainPlayer.pauseBtn.setOnClickListener { pause() }

        binding.playProfileVoiceBtn.setOnClickListener { playProfileVoice() }
        binding.pauseProfileVoiceBtn.setOnClickListener { pauseProfileVoice() }



        binding.followers.setOnClickListener { startActivity(Intent(this,FollowersActivity::class.java)) }
        binding.followings.setOnClickListener { startActivity(Intent(this,FollowingActivity::class.java)) }

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


        val bottomMenu =findViewById<BottomNavigationView>(R.id.bottom)

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0 && bottomMenu.visibility == View.VISIBLE) {
                    bottomMenu.visibility = View.GONE
                } else if (dy < 0 && bottomMenu.visibility != View.VISIBLE) {
                    bottomMenu.visibility = View.VISIBLE

                }
            }
        })

        followInfo()


    }

    private fun followInfo(){
        FollowManager().getFollowerCount(firebaseUser.uid, binding.followers)
        FollowManager().getFollowingCount(firebaseUser.uid, binding.followings)

    }


    private fun initRecycler() {
        voiceList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, this,this)
        binding.recyclerView.adapter = voiceAdapter
    }


    private fun getUserInfo(){
        viewModel.getUserInfoForActivity(firebaseUser.uid, binding)
    }

    private fun getMyVoices() {
        viewModel.getAllVoices().observe(this) { voices ->

            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
        }

        viewModel.getMyVoices(firebaseUser.uid)


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
            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()
            binding.mainPlayer.mainVoiceTitle?.text = voice.voiceTitle
            mediaPlayer = MediaPlayer.create(this@ProfileActivity,voiceUri)
            mediaPlayer!!.start()
            initializeSeekBar()
            binding.mainPlayer.playBtn.visibility = View.GONE
            binding.mainPlayer.pauseBtn.visibility = View.VISIBLE
            binding.mainPlayer.root.visibility = View.VISIBLE
        }


    }

    override fun seeLikers(voice: Voice) {
        val intent = Intent(this@ProfileActivity, LikedUsersActivity::class.java)
        intent.putExtra("voiceIdForLikes",voice.voiceId)
        startActivity(intent)
    }

    override fun clickUser(voice: Voice) {
        val intent = Intent(this@ProfileActivity,ProfileActivity::class.java)
        intent.putExtra("userId",voice.publisherId)
        startActivity(intent)
    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
    }

    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(this@ProfileActivity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.profile_voice_actions)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawableResource(R.color.white)
        dialog.show()

        val deleteVoice = dialog.findViewById<CardView>(R.id.deleteVoice)
        val shareVoice = dialog.findViewById<CardView>(R.id.shareVoice)

        shareVoice.setOnClickListener {  shareVoice(voice.voiceUrl.toUri(),this@ProfileActivity)}

        deleteVoice.setOnClickListener {
            deleteVoice(voice)
            dialog.dismiss()
        }

    }


    private fun deleteVoice(voice: Voice){
        FirebaseFirestore.getInstance().collection("Voices").document(voice.voiceId)
            .delete().addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("MyVoices").document(firebaseUser.uid).collection("Voices").document(voice.voiceId)
                    .delete().addOnSuccessListener {
                        Toast.makeText(this@ProfileActivity,"Deleted", Toast.LENGTH_SHORT).show()
                    }
            }
    }


    private fun shareVoice(fileUri: Uri, context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        context.startActivity(shareIntent)
    }


}