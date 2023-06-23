package com.yeslabapps.sesly.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.activity.FollowersActivity
import com.yeslabapps.sesly.activity.FollowingActivity
import com.yeslabapps.sesly.activity.LikedUsersActivity
import com.yeslabapps.sesly.adapter.VoiceAdapter
import com.yeslabapps.sesly.controller.DownloadManager
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.controller.FollowManager
import com.yeslabapps.sesly.databinding.FragmentProfileBinding
import com.yeslabapps.sesly.interfaces.VoiceClick
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileFragment: Fragment(),VoiceClick {

    private var binding: FragmentProfileBinding? = null
    private val viewModel by viewModel<FirebaseViewModel>()
    private lateinit var firebaseUser: FirebaseUser
    private var voiceList: ArrayList<Voice>? = null
    private var voiceAdapter: VoiceAdapter? = null
    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()

    private  var mediaPlayerProfile : MediaPlayer? = null

    private lateinit var progressDialog : ProgressDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        progressDialog = ProgressDialog(context,R.style.CustomDialog)
        progressDialog.setCancelable(false)
        progressDialog.show()


        initRecycler()

        getMyVoices()
        getUserInfo()


        binding?.mainPlayer?.playBtn?.setOnClickListener { play() }
        binding?.mainPlayer?.pauseBtn?.setOnClickListener { pause() }

        binding?.playProfileVoiceBtn?.setOnClickListener { playProfileVoice() }
        binding?.pauseProfileVoiceBtn?.setOnClickListener { pauseProfileVoice() }



        binding?.followers?.setOnClickListener {
            startActivity(Intent(context,FollowersActivity::class.java).putExtra("followersId",firebaseUser.uid))
            destroyMedia()
        }
        binding?.followings?.setOnClickListener {
            startActivity(Intent(context,FollowingActivity::class.java).putExtra("followingId",firebaseUser.uid))
            destroyMedia()
        }

        binding?.mainPlayer?.seekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mediaPlayer?.seekTo(i * 1000)

                    mediaPlayer?.setOnCompletionListener {
                        binding?.mainPlayer?.root?.visibility = View.GONE
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })


        val bottomMenu = requireActivity().findViewById<BottomNavigationView>(R.id.bottom)

        binding?.recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)



                if (dy > 0 && bottomMenu.visibility == View.VISIBLE ) {
                    if (voiceList?.size!! >4){
                        bottomMenu.visibility = View.GONE


                    }
                } else if (dy < 0 && bottomMenu.visibility != View.VISIBLE) {
                    bottomMenu.visibility = View.VISIBLE

                }
            }
        })

        followInfo()


    }

    private fun followInfo(){
        FollowManager().getFollowerCount(firebaseUser.uid,binding!!.followers)
        FollowManager().getFollowingCount(firebaseUser.uid,binding!!.followings)


    }


    private fun initRecycler() {
        voiceList = ArrayList()
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding!!.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, requireContext(),this)
        binding!!.recyclerView.adapter = voiceAdapter
    }


    private fun getUserInfo(){
        viewModel.getUserInfo(firebaseUser.uid, binding!!,progressDialog)
    }



    private fun getMyVoices() {
        viewModel.getMyVoices(firebaseUser.uid)
        viewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->
            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
            if (voiceList!!.size>0){
                binding?.totalVoices?.text = " Voices \n${voiceList?.size}"
            }else{
                binding?.totalVoices?.text = " Voices \n0"

            }
        }
        if (voiceList!!.size>0){
            binding?.totalVoices?.text = " Voices \n${voiceList?.size}"
        }else{
            binding?.totalVoices?.text = " Voices \n0"

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
        binding?.mainPlayer?.seekBar!!.max = mediaPlayer!!.seconds

        runnable = Runnable {
            binding?.mainPlayer?.seekBar?.progress = mediaPlayer!!.currentSeconds


            handler.postDelayed(runnable, 1000)
        }
        handler.postDelayed(runnable, 1000)
    }

    private fun playProfileVoice(){
        CoroutineScope(Dispatchers.Main).launch {
            val uri = binding?.profileVoice?.text.toString().toUri()
            if (mediaPlayerProfile == null) {
                mediaPlayerProfile = MediaPlayer.create(context, uri)
                mediaPlayerProfile?.setOnCompletionListener {
                    // Ses tamamlandığında tekrar başa sar
                    mediaPlayerProfile?.seekTo(0)
                    pauseProfileVoice()
                }
            }
            mediaPlayerProfile?.start()
            binding?.playProfileVoiceBtn?.visibility = View.GONE
            binding?.pauseProfileVoiceBtn?.visibility = View.VISIBLE

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
            binding?.mainPlayer?.playBtn?.visibility = View.GONE
            binding?.mainPlayer?.pauseBtn?.visibility = View.VISIBLE

            mediaPlayer?.setOnCompletionListener {
                binding?.mainPlayer?.playBtn?.visibility = View.VISIBLE
                binding?.mainPlayer?.pauseBtn?.visibility = View.GONE
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


    override fun pickVoice(voice: Voice) {
        CoroutineScope(Dispatchers.Main).launch {
            DummyMethods.increaseViewedNumber(voice)
            val pd = ProgressDialog(context, R.style.CustomDialog).apply {
                setCancelable(false)
                show()
            }

            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()

            withContext(Dispatchers.IO) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(requireContext(), voiceUri)
                    setOnPreparedListener {
                        binding?.mainPlayer?.apply {
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

        val intent = Intent(context, LikedUsersActivity::class.java)
        intent.putExtra("voiceIdForLikes",voice.voiceId)
        startActivity(intent)
    }

    override fun clickUser(voice: Voice) {
        println("Not necessary")
//        destroyMedia()
//
//        val intent = Intent(context,ProfileActivity::class.java)
//        intent.putExtra("userId",voice.publisherId)
//        startActivity(intent)
    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
    }



    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(requireContext(),R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.profile_voice_actions)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val deleteVoice = dialog.findViewById<CardView>(R.id.deleteVoice)
        val downloadVoice = dialog.findViewById<CardView>(R.id.downloadVoice)


        downloadVoice.setOnClickListener {
            if (DummyMethods.validatePermission(requireContext())){
                dialog.dismiss()
                DownloadManager.downloadVoice(requireContext(),voice.voiceUrl,voice.voiceTitle,viewModel,firebaseUser!!.uid)
            }

        }

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
                        Toast.makeText(context,"Deleted",Toast.LENGTH_SHORT).show()
                    }
            }
    }



    private fun destroyMedia(){
        binding?.mainPlayer?.seekBar?.progress = 0
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        if (mediaPlayer!=null){
            handler.removeCallbacks(runnable)

        }
        mediaPlayer = null
        binding?.mainPlayer?.root?.visibility = View.GONE
    }



    override fun onDestroy() {
        super.onDestroy()
        binding?.mainPlayer?.seekBar?.progress = 0
        mediaPlayer?.stop()
        mediaPlayer?.reset()
        mediaPlayer?.release()
        if (mediaPlayer!=null){
            handler.removeCallbacks(runnable)

        }
        mediaPlayer = null


    }




}