package com.yeslabapps.ses.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
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
                } else {
                    searchVoicesByTag(query)
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private suspend fun searchVoicesByTag(query: String) {
        val db = FirebaseFirestore.getInstance()
        val usersRef = db.collection("Voices")

        val querySnapshot = usersRef.whereGreaterThanOrEqualTo("tags", query)
            .whereLessThanOrEqualTo("tags", query + "\uf8ff")
            .get()
            .await()


        for (document in querySnapshot.documents) {
            val voice = document.toObject(Voice::class.java)
            if (voice != null) {
                voiceList?.add(voice)
            }
        }

        withContext(Dispatchers.Main) {
            voiceAdapter = VoiceAdapter(voiceList!!, requireContext(), this@SearchVoiceFragmentByTitle)
            binding?.recyclerView?.adapter = voiceAdapter
            voiceAdapter.notifyDataSetChanged()
        }
    }

    override fun pickVoice(voice: Voice) {
        DummyMethods.increaseViewedNumber(voice)

    }

    override fun seeLikers(voice: Voice) {
    }


}


