package com.yeslabapps.sesly.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.activity.VoicesByTagsActivity
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.controller.DummyMethods.Companion.formatSecondsToMinutes
import com.yeslabapps.sesly.controller.LikeManager
import com.yeslabapps.sesly.databinding.VoiceItemBinding
import com.yeslabapps.sesly.interfaces.VoiceClick
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*


class VoiceAdapter(private val voiceList : ArrayList<Voice>, val context: Context, val onClick: VoiceClick) : RecyclerView.Adapter<VoiceAdapter.MyHolder>() {

    private lateinit var firebaseUser: FirebaseUser

    class MyHolder(val binding: VoiceItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = VoiceItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        val voice = voiceList[position]

        holder.binding.voiceTitle.text = voice.voiceTitle
        holder.binding.voiceSeconds.text = formatSecondsToMinutes(voice.duration)

        holder.binding.publishTime.text = DummyMethods.toTimeAgo(voice.time)


        if (voice.tags!=null){
            val tagsText = StringBuilder()
            for (tag in voice.tags) {
                tagsText.append("#").append(tag).append(" ")
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val linearLayout = LinearLayout(context)
            linearLayout.layoutParams = layoutParams
            linearLayout.orientation = LinearLayout.HORIZONTAL

            for (tag in voice.tags) {
                val textView = TextView(context)
                textView.text = "#$tag"
                textView.setSingleLine()
                textView.ellipsize = TextUtils.TruncateAt.END
                textView.maxLines = 1
                textView.textSize = 18f
                textView.setTextColor(context.resources.getColor(R.color.appOrange))
                textView.layoutParams = layoutParams

                textView.setOnClickListener {
                    context.startActivity(Intent(context, VoicesByTagsActivity::class.java)
                        .putExtra("selectedTag",tag))
                }

                linearLayout.addView(textView)
            }

            holder.binding.tags.removeAllViews()
            holder.binding.tags.addView(linearLayout)
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

        holder.binding.voiceActions.setOnClickListener {
            onClick.voiceActions(voiceList[position])
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

        holder.binding.countOfLikes.text = voice.countOfLikes.toString()


//
//        likeManager.getLikesCountForVoice(voice.voiceId)
//            .addOnSuccessListener { likesCount ->
//                holder.binding.countOfLikes.text = likesCount.toString()
//            }
//            .addOnFailureListener {
//            }

        likeManager.startListening()

        holder.binding.likeVoiceBtn.setOnClickListener {
            likeManager.toggleLike(voice)
        }


        getUserInfoForVoiceAdapter(voice.publisherId,holder)



    }


    private fun getUserInfoForVoiceAdapter(userId: String, holder: VoiceAdapter.MyHolder) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val documentSnapshot = withContext(Dispatchers.IO) {
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(userId)
                        .get()
                        .await()
                }

                if (documentSnapshot.exists()) {
                    val user: User? = documentSnapshot.toObject(User::class.java)
                    user?.let {
                        holder.binding.username.text = "@${user.username}"
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda yapılacak işlemler
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