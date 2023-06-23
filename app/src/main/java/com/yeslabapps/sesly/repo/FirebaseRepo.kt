package com.yeslabapps.sesly.repo


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.yeslabapps.sesly.controller.DummyMethods
import com.yeslabapps.sesly.databinding.ActivityProfileBinding
import com.yeslabapps.sesly.databinding.FragmentProfileBinding
import com.yeslabapps.sesly.model.User
import com.yeslabapps.sesly.model.Voice
import com.yeslabapps.sesly.notify.Token
import com.yeslabapps.sesly.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class FirebaseRepo {

    private var mutableLiveData: MutableLiveData<List<Voice>?> = MutableLiveData()
    private var voiceList: ArrayList<Voice>? = null

    private var idlist: ArrayList<String>? = null

    private var userList: ArrayList<User>? = null

    private var mutableLiveDataUsers: MutableLiveData<List<User>?> = MutableLiveData()


    /**Method for get voices by following status*/

    fun checkForFollowers(userId: String) {
        idlist = ArrayList()
        val followersCollection =
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .collection("Followings")

        followersCollection.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val followerId = document.id
                idlist?.add(followerId)
            }
            getVoices()
        }
    }


//    fun getRecommendedUsers(){
//        CoroutineScope(Dispatchers.IO).launch {
//            userList = ArrayList()
//            try {
//                val querySnapshot = FirebaseFirestore.getInstance().collection("Users")
//                    .orderBy("followers", Query.Direction.DESCENDING)
//                    .get()
//                    .await()
//
//                if (!querySnapshot.isEmpty) {
//                    val documents = querySnapshot.documents
//
//                    for (document in documents) {
//                        val user = document.toObject(User::class.java)
//                        if (user != null ) {
//                            userList?.add(user)
//
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                // Hata durumunda işleme geçin
//            }
//
//            withContext(Dispatchers.Main) {
//                mutableLiveDataUsers.postValue(userList)
//            }
//        }
//    }


    private fun getVoices() {
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()
            try {
                val querySnapshot = FirebaseFirestore.getInstance().collection("Voices")
                    .orderBy("time", Query.Direction.DESCENDING)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    val documents = querySnapshot.documents

                    for (document in documents) {
                        val voice = document.toObject(Voice::class.java)
                        if (voice != null) {
                            for (id in idlist!!) {
                                println("liste ${idlist.toString()}")
                                if (voice.publisherId.equals(id)) {
                                    voiceList?.add(voice)
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Hata durumunda işleme geçin
            }

            withContext(Dispatchers.Main) {
                mutableLiveData.postValue(voiceList)
            }
        }


    }


    /**Method for get voices by tag*/


    fun getVoicesByTag(tagName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices")
                .orderBy("time", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.tags!!.contains(tagName)) {
                                    voiceList?.add(voice)
                                    println("taga göre liste ${voiceList.toString()}")
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }


    /**Method for get voices by country*/


    fun getVoicesByCountry(relatedCountry: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)

            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = Constants.HOURS_FOR_COUNTRY_FEED * 60 * 60 * 1000
            val oneHourAgo = currentTime - oneHourInMillis
            println("format ${sdf.format(oneHourAgo)}")
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices")
                .whereGreaterThan("time", sdf.format(oneHourAgo))
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->

                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.relatedCountry.equals(relatedCountry)) {
                                    voiceList?.add(voice)
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }


    /**Method for get voices by most listened in specified time*/


    fun getVoicesByCountryMostListened(relatedCountry: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)

            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = Constants.HOURS_FOR_COUNTRY_FEED * 60 * 60 * 1000
            val oneHourAgo = currentTime - oneHourInMillis
            println("format ${sdf.format(oneHourAgo)}")
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices")
                .orderBy("time", Query.Direction.DESCENDING)
                .orderBy("listened", Query.Direction.DESCENDING)
                .whereGreaterThan("time", sdf.format(oneHourAgo))
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->

                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.relatedCountry.equals(relatedCountry)) {
                                    voiceList?.add(voice)
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }


    /**Method for get voices by most liked in specified time*/


    fun getVoicesByCountryMostLiked(relatedCountry: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val sdf = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.ENGLISH)

            val currentTime = System.currentTimeMillis()
            val oneHourInMillis = Constants.HOURS_FOR_COUNTRY_FEED * 60 * 60 * 1000
            val oneHourAgo = currentTime - oneHourInMillis
            println("format ${sdf.format(oneHourAgo)}")
            voiceList = ArrayList()
            FirebaseFirestore.getInstance().collection("Voices")
                .orderBy("time", Query.Direction.DESCENDING)
                .orderBy("countOfLikes", Query.Direction.DESCENDING)
                .whereGreaterThan("time", sdf.format(oneHourAgo))
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->

                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                if (voice.relatedCountry.equals(relatedCountry)) {
                                    voiceList?.add(voice)
                                }
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }
    }


    @SuppressLint("SetTextI18n")
    fun getUserInfo(userId: String?, binding: FragmentProfileBinding, pd: ProgressDialog) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").document(userId!!)
                .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            binding.username.text = "@${user.username} "

                            binding.bioText.text = user.bio

                            if (user.userType==Constants.NORMAL_USER){
                                binding.verifiedTick.visibility = View.GONE
                            }else{
                                binding.verifiedTick.visibility = View.VISIBLE
                            }

                            if (user.countryVisibility) {
                                binding.country.visibility = View.VISIBLE
                                binding.country.text = user.country
                            } else {
                                binding.country.visibility = View.GONE
                            }

                            if (user.joinDateVisibility) {
                                binding.joinDate.visibility = View.VISIBLE
                                binding.joinDate.text =
                                    "Joined ${DummyMethods.toTimeAgo(user.registerDate)}"
                            } else {
                                binding.joinDate.visibility = View.GONE
                            }



                            if (user.profileVoice.isNotEmpty()) {
                                binding.playProfileVoiceBtn.visibility = View.VISIBLE
                                binding.profileVoice.text = user.profileVoice
                            } else {
                                binding.playProfileVoiceBtn.visibility = View.GONE
                            }
                            pd.dismiss()

                        }
                    }
                }
        }


    }

    @SuppressLint("SetTextI18n")
    fun getUserInfoForActivity(
        myId: String,
        userId: String,
        binding: ActivityProfileBinding,
        pd: ProgressDialog
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val user: User? = documentSnapshot.toObject(User::class.java)
                        if (user != null) {
                            binding.username.text = "@${user.username} "
                            binding.bioText.text = user.bio

                            if (user.countryVisibility) {
                                binding.country.visibility = View.VISIBLE
                                binding.country.text = user.country
                            } else {
                                binding.country.visibility = View.GONE
                            }

                            if (user.joinDateVisibility) {
                                binding.joinDate.visibility = View.VISIBLE
                                binding.joinDate.text =
                                    "Joined ${DummyMethods.toTimeAgo(user.registerDate)}"
                            } else {
                                binding.joinDate.visibility = View.GONE
                            }

                            if (user.userType==Constants.NORMAL_USER){
                                binding.verifiedTick.visibility = View.GONE
                            }else{
                                binding.verifiedTick.visibility = View.VISIBLE
                            }




                            FirebaseFirestore.getInstance().collection("Users").document(userId)
                                .collection("Followers").document(myId)
                                .addSnapshotListener { snapshot, _ ->
                                    if (snapshot != null) {
                                        if (snapshot.exists()) {
                                            binding.privateInfoText.visibility = View.GONE
                                            binding.recyclerView.visibility = View.VISIBLE
                                        } else {
                                            if (user.privateProfile) {
                                                binding.privateInfoText.visibility = View.VISIBLE
                                                binding.recyclerView.visibility = View.GONE
                                                binding.followers.isEnabled = false
                                                binding.followings.isEnabled = false

                                            } else {
                                                binding.privateInfoText.visibility = View.GONE
                                                binding.recyclerView.visibility = View.VISIBLE
                                            }
                                        }
                                    }
                                }


                            if (user.profileVoice.isNotEmpty()) {
                                binding.playProfileVoiceBtn.visibility = View.VISIBLE
                                binding.profileVoice.text = user.profileVoice
                            } else {
                                binding.playProfileVoiceBtn.visibility = View.GONE
                            }

                            pd.dismiss()
                        }
                    }
                }
        }


    }

    fun getMyVoices(userId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            voiceList = ArrayList()

            FirebaseFirestore.getInstance().collection("MyVoices").document(userId)
                .collection("Voices").orderBy("time", Query.Direction.DESCENDING).get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    if (!queryDocumentSnapshots.isEmpty) {
                        val list = queryDocumentSnapshots.documents
                        for (d in list) {
                            val voice: Voice? = d.toObject(Voice::class.java)
                            if (voice != null) {
                                voiceList?.add(voice)
                            }
                        }
                        mutableLiveData.postValue(voiceList)
                    }
                }.addOnFailureListener { }
        }


    }

    fun getUserCountry(userId: String, callback: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user: User? = documentSnapshot.toObject(User::class.java)
                    val country = user?.country ?: ""
                    callback(country)
                } else {
                    callback("")
                }
            }
    }

    fun getUserName(userId: String, callback: (String) -> Unit) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    val user: User? = documentSnapshot.toObject(User::class.java)
                    val username = user?.username ?: ""
                    callback(username)
                } else {
                    callback("")
                }
            }
    }


    fun getUserType(userId: String, callback: (Int?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val documentRef: DocumentReference = db.collection("Users").document(userId)

        documentRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document: DocumentSnapshot? = task.result
                if (document != null && document.exists()) {
                    val userType: Int? = document.getLong("userType")?.toInt()
                    callback(userType)
                } else {
                    callback(null)
                }
            } else {
                callback(null)
            }
        }
    }


    fun updateLastSeen(userId: String) {
        FirebaseFirestore.getInstance().collection("Users").document(userId)
            .update("lastSeen", System.currentTimeMillis())
    }


    fun updateToken(token: String, userId: String) {
        val ref = FirebaseFirestore.getInstance().collection("Tokens")
        val mToken = Token(token)
        ref.document(userId).set(mToken)
    }


    fun getAllVoices(): MutableLiveData<List<Voice>?> {
        return mutableLiveData
    }

    fun getAllUsers(): MutableLiveData<List<User>?> {
        return mutableLiveDataUsers
    }


}