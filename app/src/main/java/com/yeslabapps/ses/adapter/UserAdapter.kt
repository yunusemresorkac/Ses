package com.yeslabapps.ses.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.ses.controller.FollowManager
import com.yeslabapps.ses.databinding.UserItemBinding
import com.yeslabapps.ses.interfaces.UserClick
import com.yeslabapps.ses.model.User
import kotlinx.coroutines.CoroutineScope


class UserAdapter(private val userList: ArrayList<User>, val context: Context,
                  val userClick: UserClick
) : RecyclerView.Adapter<UserAdapter.MyHolder>() {

    private var firebaseUser : FirebaseUser? = null


    class MyHolder(val binding: UserItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = UserItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val user = userList[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser?.uid.equals(user.userId)){
            holder.binding.followBtn.visibility = View.GONE
        }else{
            holder.binding.followBtn.visibility = View.VISIBLE
        }


        holder.binding.username.text = user.username

        holder.binding.followBtn.setOnClickListener {
            userClick.followUser(userList[position])
        }

        FollowManager().updateFollowButton(firebaseUser!!.uid,user.userId,holder.binding.followBtn)



        holder.itemView.setOnClickListener {
            userClick.goProfile(userList[position])
        }


    }






    override fun getItemCount(): Int {
        return userList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}