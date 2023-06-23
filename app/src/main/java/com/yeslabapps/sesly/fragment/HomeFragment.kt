package com.yeslabapps.sesly.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
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
import com.yeslabapps.sesly.activity.LikedUsersActivity
import com.yeslabapps.sesly.activity.ProfileActivity
import com.yeslabapps.sesly.activity.SearchActivity
import com.yeslabapps.sesly.adapter.VoiceAdapter
import com.yeslabapps.sesly.controller.DownloadManager
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.controller.DummyMethods.Companion.increaseViewedNumber
import com.yeslabapps.sesly.databinding.FragmentHomeBinding
import com.yeslabapps.sesly.interfaces.VoiceClick
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import io.github.muddz.styleabletoast.StyleableToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.HashMap


class HomeFragment :Fragment(), VoiceClick {

    private var binding: FragmentHomeBinding? = null
    private var voiceList: ArrayList<Voice>? = null
    private var voiceAdapter: VoiceAdapter? = null
    private  var mediaPlayer: MediaPlayer? = null
    private var pause:Boolean = false
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private val firebaseViewModel by viewModel<FirebaseViewModel>()
    private lateinit var pd : ProgressDialog
    private lateinit var firebaseUser: FirebaseUser
    private var followersList: ArrayList<String>? = null





    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pd = ProgressDialog(context, R.style.CustomDialog)
        pd.show()
        pd.setCancelable(false)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        followersList = ArrayList()

        initRecycler()
        getVoices()

        binding?.goSearch?.setOnClickListener {
            destroyMedia()
            startActivity(Intent(context,SearchActivity::class.java))
        }

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




    }

//    private fun initRecyclerForUsers(){
//        userList = ArrayList()
//        binding?.recyclerViewUsers?.layoutManager = LinearLayoutManager(context,LinearLayoutManager.HORIZONTAL,false)
//        binding?.recyclerViewUsers?.setHasFixedSize(true)
//        userAdapter = UserAdapter(userList!!,requireContext(),this)
//        binding?.recyclerViewUsers?.adapter = userAdapter
//    }


    private fun initRecycler() {
        voiceList = ArrayList()
        binding?.recyclerView?.layoutManager = LinearLayoutManager(context)
        binding!!.recyclerView.setHasFixedSize(true)
        voiceAdapter = VoiceAdapter(voiceList!!, requireContext(),this)
        binding!!.recyclerView.adapter = voiceAdapter
    }

//    private fun getRecommendedUsers(){
//        firebaseViewModel.getAllUsers().observe(viewLifecycleOwner) { users ->
//
//            userList?.addAll(users!!)
//            userAdapter?.notifyDataSetChanged()
//            pd.dismiss()
//
//        }
//
//        firebaseViewModel.getRecommendedUsers()
//    }


    @SuppressLint("NotifyDataSetChanged")
    private fun getVoices() {
        firebaseViewModel.checkForFollowers(firebaseUser.uid)
        firebaseViewModel.getAllVoices().observe(viewLifecycleOwner) { voices ->

            voiceList?.addAll(voices!!)
            voiceAdapter?.notifyDataSetChanged()
            pd.dismiss()
            if (voiceList!!.size>0){
                binding?.welcomeInfoLay?.visibility   = View.GONE
            }else{
                binding?.welcomeInfoLay?.visibility   = View.VISIBLE
                pd.dismiss()

            }
        }

    }


//        CoroutineScope(Dispatchers.IO).launch {
//            FirebaseFirestore.getInstance().collection("Voices").get()
//                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
//                    if (!queryDocumentSnapshots.isEmpty) {
//                        val list = queryDocumentSnapshots.documents
//                        for (d in list) {
//                            val voice: Voice? = d.toObject(Voice::class.java)
//                            if (voice != null) {
//                                println("bilgiler ${voice.topicName}")
//                                voiceList?.add(voice)
//                            }
//                        }
//                        voiceAdapter?.notifyDataSetChanged()
//                    }
//                }.addOnFailureListener { }
//        }


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

    private fun reportDialog(voice: Voice){
        val dialog = Dialog(requireContext(),R.style.SheetDialog)
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
                    requireContext(),
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
                            requireContext(),
                            "Thanks for providing feedback!",
                            R.style.customToast
                        ).show()}

            }
        }



    }


    private fun showVoiceActions(voice: Voice){
        val dialog = Dialog(requireContext(),R.style.SheetDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_voice_actions)

        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
        dialog.show()

        val downloadVoice = dialog.findViewById<CardView>(R.id.downloadVoice)
        val reportVoice = dialog.findViewById<CardView>(R.id.reportVoice)


        downloadVoice.setOnClickListener {
            if (DummyMethods.validatePermission(requireContext())){
                dialog.dismiss()
                DownloadManager.downloadVoice(requireContext(),voice.voiceUrl,voice.voiceTitle,firebaseViewModel,
                    firebaseUser.uid)
            }

        }

        reportVoice.setOnClickListener { reportDialog(voice) }


    }


    override fun voiceActions(voice: Voice) {
        showVoiceActions(voice)
    }




    override fun pickVoice(voice: Voice) {
        CoroutineScope(Dispatchers.Main).launch {
            increaseViewedNumber(voice)
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