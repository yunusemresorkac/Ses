package com.yeslabapps.ses.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.yeslabapps.ses.databinding.TopicItemBinding
import com.yeslabapps.ses.model.Topic

class TopicAdapter(private val topicList : ArrayList<Topic>, val context: Context) : RecyclerView.Adapter<TopicAdapter.MyHolder>() {


    class MyHolder(val binding: TopicItemBinding) : RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val binding = TopicItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyHolder(binding)
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        val topic = topicList[position]

        holder.binding.topicName.text = topic.topicName
        holder.binding.topicImage.setImageResource(topic.topicImage)


    }




    override fun getItemCount(): Int {
        return topicList.size
    }

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }



}