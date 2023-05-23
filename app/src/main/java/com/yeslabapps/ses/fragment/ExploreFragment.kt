package com.yeslabapps.ses.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.yeslabapps.ses.R
import com.yeslabapps.ses.adapter.TopicAdapter
import com.yeslabapps.ses.databinding.FragmentExploreBinding
import com.yeslabapps.ses.model.Topic

class ExploreFragment: Fragment() {

    private var binding: FragmentExploreBinding? = null
    private var topicList: ArrayList<Topic>? = null
    private var topicAdapter: TopicAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        initRecycler()

    }


    private fun initRecycler() {
        topicList = ArrayList()
        binding?.recyclerView?.layoutManager = GridLayoutManager(context,2)
        binding!!.recyclerView.setHasFixedSize(true)
        topicAdapter = TopicAdapter(topicList!!, requireContext())
        binding!!.recyclerView.adapter = topicAdapter

        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Political", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))
        topicList!!.add(Topic("Art", R.drawable.add_circle_svgrepo_com))

    }


}