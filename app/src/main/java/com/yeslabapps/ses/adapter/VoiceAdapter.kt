package com.yeslabapps.ses.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.R
import com.yeslabapps.ses.controller.DummyMethods.Companion.formatSecondsToMinutes
import com.yeslabapps.ses.controller.DummyMethods.Companion.getTimeAgo
import com.yeslabapps.ses.controller.LikeManager
import com.yeslabapps.ses.databinding.VoiceItemBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class VoiceAdapter(private val voiceList : ArrayList<Voice>, val context: Context, val onClick: VoiceClick) : RecyclerView.Adapter<VoiceAdapter.MyHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    class MyHolder(val binding: VoiceItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = VoiceItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val voice = voiceList[position]

        holder.binding.voiceTitle.text = voice.voiceTitle
        holder.binding.voiceTime.text = getTimeAgo(voice.time)
        holder.binding.voiceSeconds.text = formatSecondsToMinutes(voice.voiceTime)
        if (voice.tags!=null){
            val tagsText = StringBuilder()
            for (tag in voice.tags) {
                tagsText.append("#").append(tag).append(" ")
            }

            holder.binding.tags.text = tagsText.toString()
        }


        holder.binding.countOfLikes.setOnClickListener {
            onClick.seeLikers(voiceList[position])
        }



        holder.itemView.setOnClickListener {
            onClick.pickVoice(voiceList[position])
        }

        holder.binding.username.setOnClickListener {
            onClick.clickUser(voiceList[position])
        }


        val likeManager = LikeManager(voice.voiceId, firebaseUser.uid, object : LikeManager.LikeStatusListener {
            override fun onLikeStatusChanged(liked: Boolean) {
                if (liked) {
                    // Beğenildiğinde yapılacak işlemler
                    println("Gönderi beğenildi.")
                    holder.binding.likeVoiceBtn.setImageResource(R.drawable.ic_favorite)
                    // Simgeyi değiştirme veya diğer işlemleri burada gerçekleştirin
                } else {
                    // Beğenilmediğinde yapılacak işlemler
                    holder.binding.likeVoiceBtn.setImageResource(R.drawable.ic_favorite_border)

                    println("Gönderi beğenilmedi.")
                    // Simgeyi değiştirme veya diğer işlemleri burada gerçekleştirin
                }
            }
        })

        likeManager.getLikesCountForVoice(voice.voiceId)
            .addOnSuccessListener { likesCount ->
                holder.binding.countOfLikes.text = likesCount.toString()
            }
            .addOnFailureListener {
            }

        likeManager.startListening()

        holder.binding.likeVoiceBtn.setOnClickListener {
            likeManager.toggleLike()
        }

        getUserInfoForVoiceAdapter(voice.publisherId,holder)


    }


    private fun getUserInfoForVoiceAdapter(userId: String, holder : VoiceAdapter.MyHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            holder.binding.username.text = user.username

                        }
                    }
                }
        }


    }


    override fun getItemCount(): Int {
        return voiceList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }


}