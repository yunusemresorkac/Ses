package com.yeslabapps.ses.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
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
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.*
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.FragmentProfileBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import kotlinx.android.synthetic.main.fragment_profile.*
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




    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        firebaseUser = FirebaseAuth.getInstance().currentUser!!



        initRecycler()

        getMyVoices()
        getUserInfo()

        binding?.seeMyLikes?.setOnClickListener {  startActivity(Intent(context,MyLikesActivity::class.java)) }

        binding?.mainPlayer?.playBtn?.setOnClickListener { play() }
        binding?.mainPlayer?.pauseBtn?.setOnClickListener { pause() }

        binding?.playProfileVoiceBtn?.setOnClickListener { playProfileVoice() }
        binding?.pauseProfileVoiceBtn?.setOnClickListener { pauseProfileVoice() }



        binding?.followers?.setOnClickListener { startActivity(Intent(context,FollowersActivity::class.java).putExtra("followersId",firebaseUser.uid)) }
        binding?.followings?.setOnClickListener { startActivity(Intent(context,FollowingActivity::class.java).putExtra("followingId",firebaseUser.uid)) }

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
        viewModel.getUserInfo(firebaseUser.uid, binding!!)
    }

    private fun getMyVoices() {
        viewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->

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
            val pd = ProgressDialog(context,R.style.CustomDialog)
            pd.setCancelable(false)
            pd.show()
            mediaPlayer?.release()
            val voiceUri = voice.voiceUrl.toUri()

            withContext(Dispatchers.IO) {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(requireContext(), voiceUri)
                    setOnPreparedListener {
                        // MediaPlayer hazır olduğunda yapılacak işlemler
                        binding?.mainPlayer?.mainVoiceTitle?.text = voice.voiceTitle
                        mediaPlayer!!.start()
                        initializeSeekBar()
                        binding?.mainPlayer?.playBtn?.visibility = View.GONE
                        binding?.mainPlayer?.pauseBtn?.visibility = View.VISIBLE
                        binding?.mainPlayer?.root?.visibility = View.VISIBLE
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
        destroyMedia()

        val intent = Intent(context,ProfileActivity::class.java)
        intent.putExtra("userId",voice.publisherId)
        startActivity(intent)
    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
    }

    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.profile_voice_actions)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.window?.setBackgroundDrawableResource(R.color.white)
        dialog.show()

        val deleteVoice = dialog.findViewById<CardView>(R.id.deleteVoice)
        val shareVoice = dialog.findViewById<CardView>(R.id.shareVoice)

        shareVoice.setOnClickListener {  shareVoice(voice.voiceUrl.toUri(),requireContext())}

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


    private fun shareVoice(fileUri: Uri, context: Context) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "audio/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        context.startActivity(shareIntent)
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