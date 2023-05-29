package com.yeslabapps.ses.controller

import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.yeslabapps.ses.R

class FollowManager {

    val db = FirebaseFirestore.getInstance()
    val usersCollection = db.collection("Users")

    // Kullanıcının takip durumunu dinlemek için ListenerRegistration
    var followListener: ListenerRegistration? = null

    // Kullanıcının takip durumunu güncelleyen ve buton metnini değiştiren fonksiyon
    fun updateFollowButton(userId: String, targetUserId: String, followButton: ImageView) {
        val userFollowersCollection = usersCollection.document(userId).collection("Followings")

        followListener?.remove() // Eski listener'ı kaldır

        followListener = userFollowersCollection.document(targetUserId).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                // Kullanıcı takip ediliyorsa
                followButton.setImageResource(R.drawable.user_remove_svgrepo_com)
            } else {
                // Kullanıcı takip etmiyorsa
                followButton.setImageResource(R.drawable.user_add_svgrepo_com__1_)
            }
        }
    }

    // Kullanıcıyı takip etme fonksiyonu
    fun followUser(userId: String, targetUserId: String) {
        val userFollowersCollection = usersCollection.document(targetUserId).collection("Followers")

        userFollowersCollection.document(userId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Kullanıcı zaten takip ediliyorsa, takipten çık
                userFollowersCollection.document(userId).delete().addOnSuccessListener {
                    FirebaseFirestore.getInstance().collection("Users")
                        .document(targetUserId).update("followers",FieldValue.increment(-1))
                }

            } else {
                // Kullanıcıyı takip et
                userFollowersCollection.document(userId).set(mapOf("followed" to true))
                FirebaseFirestore.getInstance().collection("Users")
                    .document(targetUserId).update("followers",FieldValue.increment(1))
            }
        }

        val followingCollection = usersCollection.document(userId).collection("Followings")

        followingCollection.document(targetUserId).get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                // Kullanıcı zaten takip ediliyorsa, takipten çık
                followingCollection.document(targetUserId).delete()
            } else {
                // Kullanıcıyı takip et
                followingCollection.document(targetUserId).set(mapOf("followed" to true))
            }
        }
    }




    fun getFollowingCount(userId: String,textView: TextView) {
        val followingCollection = usersCollection.document(userId).collection("Followings")

        followingCollection.get().addOnSuccessListener { querySnapshot ->
            val followerCount = querySnapshot.size()
            textView.text = "Following\n $followerCount"

        }
    }


    fun getFollowerCount(userId: String,textView: TextView) {

        val followersCollection = usersCollection.document(userId).collection("Followers")




        followersCollection.get().addOnSuccessListener { querySnapshot ->
            val followerCount = querySnapshot.size()
            textView.text = "Followers\n $followerCount"

            val map: HashMap<String, Any> = HashMap()
            map["followers"] = followerCount
            FirebaseFirestore.getInstance().collection("Users").document(userId)
                .update(map)

        }

    }


}