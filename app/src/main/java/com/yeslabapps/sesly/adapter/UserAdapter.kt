package com.yeslabapps.sesly.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.yeslabapps.sesly.controller.FollowManager
import com.yeslabapps.sesly.databinding.UserItemBinding
import com.yeslabapps.sesly.interfaces.UserClick
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.util.Constants


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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val user = userList[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser?.uid.equals(user.userId)){
            holder.binding.followBtn.visibility = View.GONE
        }else{
            holder.binding.followBtn.visibility = View.VISIBLE
        }

        if (user.userType==Constants.NORMAL_USER){
            holder.binding.verifiedTick.visibility = View.GONE
        }else{
            holder.binding.verifiedTick.visibility = View.VISIBLE
        }

        holder.binding.username.text = "@${user.username}"
        holder.binding.followersCount.text = "${user.followers} Followers"

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