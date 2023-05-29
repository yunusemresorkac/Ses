package com.yeslabapps.ses.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.VoicesByTagsActivity
import com.yeslabapps.ses.controller.DummyMethods.Companion.formatSecondsToMinutes
import com.yeslabapps.ses.controller.LikeManager
import com.yeslabapps.ses.databinding.VoiceItemBinding
import com.yeslabapps.ses.interfaces.VoiceClick
import com.yeslabapps.ses.model.User
import com.yeslabapps.ses.model.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.ParseException
import java.text.SimpleDateFormat
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
        holder.binding.publishTime.text = getTimeAgo(voice.time)
        holder.binding.voiceSeconds.text = formatSecondsToMinutes(voice.voiceTime)

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
                textView.setTextColor(context.resources.getColor(R.color.black))
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
    private fun getTimeAgo(date: String): String {
        val sdf = SimpleDateFormat("EE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH)
        val fromTimeZone = "UTC"
        sdf.timeZone = TimeZone.getTimeZone(fromTimeZone)
        try {
            val time: Long = sdf.parse(date)!!.time
            val now = System.currentTimeMillis()
            val ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS)
            return ago.toString() + ""
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return ""
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