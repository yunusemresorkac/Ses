package com.yeslabapps.ses.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.*
import android.widget.ProgressBar
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
import com.google.firebase.storage.FirebaseStorage
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.LikedUsersActivity
import com.yeslabapps.ses.activity.ProfileActivity
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.controller.DownloadManager
import com.yeslabapps.ses.controller.DummyMethods
import com.yeslabapps.ses.databinding.FragmentExploreBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.Voice
import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Runnable

class ExploreFragment: Fragment(),VoiceClick {

    private var binding: FragmentExploreBinding? = null
    private var voiceList: ArrayList<Voice>? = null
    private var voiceAdapter: VoiceAdapter? = null
    private val firebaseViewModel by viewModel<FirebaseViewModel>()
    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var firebaseUser : FirebaseUser? = null
    private lateinit var pd : ProgressDialog


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        pd = ProgressDialog(context, R.style.CustomDialog)
        pd.show()
        pd.setCancelable(false)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        initRecycler()

        getVoicesByCountry()

        binding?.mainPlayer?.playBtn?.setOnClickListener { play() }
        binding?.mainPlayer?.pauseBtn?.setOnClickListener { pause() }


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



        binding?.filterBtn?.setOnClickListener {
            showFilterDialog()
        }


    }

    private fun showFilterDialog(){
        val dialog = Dialog(requireContext(),R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_explore_filter)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val topRated = dialog.findViewById<CardView>(R.id.topRated)
        val mostListened = dialog.findViewById<CardView>(R.id.mostListened)

        mostListened.setOnClickListener {
            dialog.dismiss()
            getVoicesByCountryMostListened()
        }

        topRated.setOnClickListener {
            dialog.dismiss()
            getVoicesByCountryMostLiked()
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


    private fun initRecycler() {
        voiceList = ArrayList()
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding!!.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, requireContext(),this)
        binding!!.recyclerView.adapter = voiceAdapter
    }


    private fun getVoicesByCountry() {
        firebaseViewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->

            voiceList?.clear()
            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
            pd.dismiss()

            if (voiceList!!.size>0){
                binding?.exploreInfoLay?.visibility   = View.GONE
            }else{
                pd.dismiss()
                binding?.exploreInfoLay?.visibility   = View.VISIBLE
            }


        }

        firebaseViewModel.getUserCountry(firebaseUser!!.uid) { country ->
            firebaseViewModel.getVoicesByCountry(country)
            pd.dismiss()
            if (voiceList!!.size>0){
                binding?.exploreInfoLay?.visibility   = View.GONE
            }else{
                pd.dismiss()
                binding?.exploreInfoLay?.visibility   = View.VISIBLE
            }

        }
    }

    private fun getVoicesByCountryMostListened(){
        val progress = ProgressDialog(context,R.style.CustomDialog)
        progress.setCancelable(false)
        progress.show()
        firebaseViewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->
            voiceList?.clear()
            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()


        }

        firebaseViewModel.getUserCountry(firebaseUser!!.uid) { country ->
            firebaseViewModel.getVoicesByCountryMostListened(country)
            progress.dismiss()

        }
    }


    private fun getVoicesByCountryMostLiked(){
        val progress = ProgressDialog(context,R.style.CustomDialog)
        progress.setCancelable(false)
        progress.show()
        firebaseViewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->
            voiceList?.clear()
            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()


        }

        firebaseViewModel.getUserCountry(firebaseUser!!.uid) { country ->
            firebaseViewModel.getVoicesByCountryMostLiked(country)
            progress.dismiss()

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
        destroyMedia()

        val intent = Intent(context, ProfileActivity::class.java)
        intent.putExtra("userId",voice.publisherId)
        startActivity(intent)
    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
    }

    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(requireContext(),R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_voice_actions)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val shareVoice = dialog.findViewById<CardView>(R.id.shareVoice)
        val downloadVoice = dialog.findViewById<CardView>(R.id.downloadVoice)


        downloadVoice.setOnClickListener {
            if (DummyMethods.validatePermission(requireContext())){
                dialog.dismiss()
                DownloadManager.downloadVoice(requireContext(),voice.voiceUrl,voice.voiceTitle)
            }

        }


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