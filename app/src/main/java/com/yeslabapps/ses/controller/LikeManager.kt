package com.yeslabapps.ses.controller

import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.fragment.HomeFragment

class LikeManager(postId: String, userId: String , private val listener: LikeStatusListener) {
    private val likesCollection = FirebaseFirestore.getInstance().collection("Likes")
    private val postLikesDocument = likesCollection.document(postId)
    private val userLikeDocument = postLikesDocument.collection("Users").document(userId)

    private val myLikesCollection = FirebaseFirestore.getInstance().collection("MyLikes")
    private val myPostLikesDocument = myLikesCollection.document(userId)
    private val myLikeDocument = myPostLikesDocument.collection("Voices").document(postId)


    // Beğeni durumunu dinlemeye başlayan fonksiyon
    fun startListening() {
        userLikeDocument.addSnapshotListener { snapshot, error ->
            if (error != null) {
                println("Beğeni durumu dinlenirken bir hata oluştu: $error")

                return@addSnapshotListener
            }

            val liked = snapshot?.get("liked") as? Boolean
            listener.onLikeStatusChanged(liked == true)
        }
    }

    // Beğeni durumunu değiştiren fonksiyon
    fun toggleLike() {
        userLikeDocument.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Kullanıcı beğeniyi daha önce eklemiş, bu yüzden beğeniyi kaldırın
                    userLikeDocument.delete()
                        .addOnSuccessListener {
                            myLikeDocument.delete()
                            println("Beğeni kaldırıldı.")
                        }
                        .addOnFailureListener { e ->
                            println("Beğeni kaldırılırken bir hata oluştu: $e")
                        }
                } else {
                    // Kullanıcı beğeniyi daha önce eklememiş, bu yüzden beğeniyi ekle
                    userLikeDocument.set(mapOf("liked" to true))
                        .addOnSuccessListener {
                            myLikeDocument.set(mapOf("liked" to true))
                            println("Beğeni eklendi.")
                        }
                        .addOnFailureListener { e ->
                            println("Beğeni eklenirken bir hata oluştu: $e")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Beğeni durumu kontrol edilirken bir hata oluştu: $e")
            }
    }

    fun getLikesCountForVoice(postId: String): Task<Int> {
        // Likes koleksiyonuna ve belirli gönderi belgesine referans oluşturun
        val postLikesDocument = likesCollection.document(postId)

        // Belirli gönderiyi beğenen kullanıcıların sayısını bulmak için koleksiyonu sorgulayın ve Task döndürün
        return postLikesDocument.collection("Users")
            .get()
            .continueWith { result ->
                result.result?.size() ?: 0
            }

    }



    interface LikeStatusListener {
        fun onLikeStatusChanged(liked: Boolean)
    }


}
