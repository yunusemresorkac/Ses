package com.yeslabapps.ses

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yeslabapps.ses.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private  var mediaPlayer: MediaPlayer? = null
    private lateinit var runnable:Runnable
    private var handler: Handler = Handler()
    private var selectorFragment: Fragment? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)








    }








//    override fun pickVoice(voice: Voice) {
//        binding.currentVoice.text = voice.title
//        mediaPlayer = MediaPlayer()
//        mediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
//        try {
//
//            mediaPlayer!!.setDataSource(voice.voiceUrl)
//            mediaPlayer!!.prepare()
//            mediaPlayer!!.start()
//            initializeSeekBar()
//        }catch (e:IOException){
//            e.printStackTrace()
//        }
//
//
//
//
//        Toast.makeText(this,voice.title,Toast.LENGTH_SHORT).show()
//    }
//
//    private fun currentInfo(){
//        if (mediaPlayer?.isPlaying == true){
//            binding.statusBtn.setImageResource(R.drawable.ic_play)
//        }else{
//            binding.statusBtn.setImageResource(R.drawable.ic_pause)
//
//        }
//    }
//
//    private fun initializeSeekBar() {
//        binding.mainSeekBar.max = mediaPlayer!!.seconds
//
//        runnable = Runnable {
//            binding.mainSeekBar.progress = mediaPlayer!!.currentSeconds
//
//            binding.duration.text = "${mediaPlayer!!.currentSeconds} sec"
//
//
//            handler.postDelayed(runnable, 1000)
//        }
//        handler.postDelayed(runnable, 1000)
//    }
//

    private val MediaPlayer.seconds:Int
        get() {
            return this.duration / 1000
        }
    // Creating an extension property to get media player current position in seconds
    private val MediaPlayer.currentSeconds:Int
        get() {
            return this.currentPosition/1000
        }


    private fun playAndStop(){
        if (mediaPlayer?.isPlaying == true){
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.reset()
            handler.removeCallbacks(runnable)

        }else{
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        }
    }


}