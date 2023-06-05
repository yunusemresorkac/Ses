package com.yeslabapps.ses.fragment

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.LikedUsersActivity
import com.yeslabapps.ses.activity.ProfileActivity
import com.yeslabapps.ses.adapter.VoiceAdapter
import com.yeslabapps.ses.controller.DummyMethods
import com.yeslabapps.ses.databinding.FragmentSearchVoiceByTitleBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SearchVoiceFragmentByTitle : Fragment(),VoiceClick {

    private var binding: FragmentSearchVoiceByTitleBinding? = null
    private lateinit var voiceAdapter: VoiceAdapter
    private var firebaseUser : FirebaseUser? = null
    private var voiceList : ArrayList<Voice>? = null

    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchVoiceByTitleBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        voiceList = ArrayList()

        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)

        binding?.searchEt?.requestFocus()


        binding?.searchEt?.addTextChangedListener(textWatcher)

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




    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Değişiklik öncesi işlemler
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // Değişiklik esnasında işlemler
            val username = s.toString().trim()
            if (username.length>1){
                searchVoices(username)

            }


        }

        override fun afterTextChanged(s: Editable?) {
            // Değişiklik sonrası işlemler
            voiceList!!.clear()

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun searchVoices(query: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val db = FirebaseFirestore.getInstance()
            val usersRef = db.collection("Voices")


            val querySnapshot = usersRef.whereGreaterThanOrEqualTo("voiceTitle", query)
                .orderBy("voiceTitle") // İlk sıralama kuralı olarak "voiceTitle" alanını kullanın
                .orderBy("listened",Query.Direction.ASCENDING)
                .whereLessThanOrEqualTo("voiceTitle", query + "\uf8ff")
                .get()
                .await()

            val tempList = ArrayList<Voice>() // Geçici liste oluştur


            for (document in querySnapshot.documents) {
                val voice = document.toObject(Voice::class.java)
                if (voice != null) {
                    tempList.add(voice)
                }
            }

            withContext(Dispatchers.Main) {
                voiceList = tempList // userList'i geçici listeden al
                if (voiceList!!.isNotEmpty()) {
                    voiceAdapter = VoiceAdapter(voiceList!!, requireContext(), this@SearchVoiceFragmentByTitle)
                    binding?.recyclerView?.adapter = voiceAdapter
                    voiceAdapter.notifyDataSetChanged()
                }
            }
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

        runnable = Runnable {  binding?.mainPlayer?.seekBar?.progress = mediaPlayer!!.currentSeconds


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
                mediaPlayer?.start()

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

    override fun onDestroy() {
        super.onDestroy()
        destroyMedia()
    }

}


