package com.yeslabapps.sesly.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.adapter.VoiceAdapter
import com.yeslabapps.sesly.controller.DownloadManager
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.controller.FollowManager
import com.yeslabapps.sesly.databinding.ActivityProfileBinding
import com.yeslabapps.sesly.interfaces.VoiceClick
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.util.NetworkChangeListener
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.HashMap

import android.widget.*


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

    private lateinit var pd : ProgressDialog
    private val networkChangeListener = NetworkChangeListener()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)



        pd = ProgressDialog(this, R.style.CustomDialog)
        pd.show()
        pd.setCancelable(false)


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

                if (dy > 0 && binding.followLay.visibility!=View.GONE) {
                    binding.followLay.visibility = View.GONE

                } else if (dy < 0 && binding.followLay.visibility!=View.VISIBLE ) {
                    binding.followLay.visibility = View.VISIBLE



                }
            }
        })

        followInfo()


        binding.followBtn.setOnClickListener {
            FollowManager().followUser(firebaseUser.uid, userId!!,this,viewModel)

        }


        binding.userActionsBtn.setOnClickListener { showActionsDialog() }


    }

    private fun showActionsDialog(){
        val popupMenu = PopupMenu(this@ProfileActivity, binding.userActionsBtn)
        popupMenu.menuInflater.inflate(R.menu.profile_menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {

            when (it.itemId) {
                R.id.reportUser -> {
                    reportUser()
                }

            }
            true
        }
        popupMenu.show()
    }

    private fun reportUser(){
        val dialog = Dialog(this,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.report_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        val button: Button = dialog.findViewById(R.id.sendReport)
        val radioGroup: RadioGroup = dialog.findViewById(R.id.reportVoice)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<View>(checkedId) as RadioButton
        }
        button.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                StyleableToast.makeText(
                    this,
                    "No reason has been selected",
                    R.style.customToast
                )
                    .show()
            } else {
                val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
                val map: HashMap<String, Any> = HashMap()
                val id = DummyMethods.generateRandomString(12)
                map["reportedUser"] = userId!!
                map["reportedUserName"] = binding.username.text.toString()
                map["reportedTime"] = System.currentTimeMillis()
                map["reportId"] = id
                map["reporterId"] = firebaseUser.uid
                map["reportReason"] = radioButton.text.toString()
                FirebaseFirestore.getInstance().collection("UserReports").document(userId!!)
                    .collection("Reports").document(id)
                    .set(map)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        StyleableToast.makeText(
                            this,
                            "Thanks for providing feedback!",
                            R.style.customToast
                        ).show()}

            }
        }
    }



    private fun followInfo(){
        if (firebaseUser.uid.equals(userId)){
            binding.followBtn.visibility = View.GONE
        }else{
            binding.followBtn.visibility = View.VISIBLE

        }
        FollowManager().updateFollowButton(firebaseUser.uid, userId!!,binding.followBtn)

        FollowManager().getFollowerCount(userId!!, binding.followers)
        FollowManager().getFollowingCount(userId!!, binding.followings)

    }


    private fun initRecycler() {
        voiceList = ArrayList()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, this,this)
        binding.recyclerView.adapter = voiceAdapter
    }


    private fun getUserInfo(){
        viewModel.getUserInfoForActivity(firebaseUser.uid, userId!!, binding, pd)
    }

    @SuppressLint("SetTextI18n")
    private fun getMyVoices() {
        viewModel.getMyVoices(userId!!)
        viewModel.getAllVoices().observe(this) { voices ->

            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
            if (voiceList?.size!! >0){
                binding.totalVoices.text = " Voices \n${voiceList?.size}"
            }else{
                binding.totalVoices.text = " Voices \n0"
            }
        }

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
            binding.playProfileVoiceBtn.visibility = View.VISIBLE
            binding.pauseProfileVoiceBtn.visibility = View.GONE
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
            val pd = ProgressDialog(this@ProfileActivity, R.style.CustomDialog).apply {
                setCancelable(false)
                show()
            }

            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()

            withContext(Dispatchers.IO) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(this@ProfileActivity, voiceUri)
                    setOnPreparedListener {
                        binding.mainPlayer.apply {
                            mainVoiceTitle.text = voice.voiceTitle
                            playBtn.visibility = View.GONE
                            pauseBtn.visibility = View.VISIBLE
                            root.visibility = View.VISIBLE
                        }
                        mediaPlayer!!.start()
                        initializeSeekBar()
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

        mediaPlayerProfile?.stop()
        mediaPlayerProfile?.reset()
        mediaPlayerProfile?.release()
        mediaPlayerProfile = null
        binding.mainPlayer.root.visibility = View.GONE
    }

    private fun reportDialog(voice: Voice){
        val dialog = Dialog(this,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.report_dialog)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()
        val button: Button = dialog.findViewById(R.id.sendReport)
        val radioGroup: RadioGroup = dialog.findViewById(R.id.reportVoice)
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val radioButton = group.findViewById<View>(checkedId) as RadioButton
        }

        button.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                StyleableToast.makeText(
                    this,
                    "No reason has been selected",
                    R.style.customToast
                )
                    .show()
            } else {
                val radioButton = radioGroup.findViewById<RadioButton>(selectedId)
                val map: HashMap<String, Any> = HashMap()
                val id = DummyMethods.generateRandomString(12)
                map["reportedVoiceId"] = voice.voiceId
                map["reportedVoiceTitle"] = voice.voiceTitle
                map["reportedVoicePublisher"] = voice.publisherId
                map["reportedTime"] = System.currentTimeMillis()
                map["reportId"] = id
                map["reporterId"] = firebaseUser.uid
                map["reportReason"] = radioButton.text.toString()

                FirebaseFirestore.getInstance().collection("Reports").document(voice.voiceId)
                    .collection("Reports").document(id).set(map)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        StyleableToast.makeText(
                            this,
                            "Thanks for providing feedback!",
                            R.style.customToast
                        ).show()}

            }
        }



    }


    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(this,R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_voice_actions)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val downloadVoice = dialog.findViewById<CardView>(R.id.downloadVoice)
        val reportVoice = dialog.findViewById<CardView>(R.id.reportVoice)


        downloadVoice.setOnClickListener {
            if (DummyMethods.validatePermission(this)){
                dialog.dismiss()
                DownloadManager.downloadVoice(this,voice.voiceUrl,voice.voiceTitle,viewModel,
                    firebaseUser.uid)
            }

        }

        reportVoice.setOnClickListener {
            dialog.dismiss()
            reportDialog(voice)
        }


    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
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