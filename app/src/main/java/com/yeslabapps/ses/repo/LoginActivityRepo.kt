package com.yeslabapps.ses.repo

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.yeslabapps.ses.R
import com.yeslabapps.ses.activity.StartActivity
import com.yeslabapps.ses.model.User


class LoginActivityRepo {
    private val errorMessage: MutableLiveData<String> = MutableLiveData()

    fun loginUser(context: Activity, email: String?, password: String?, auth: FirebaseAuth) {
        val pd = ProgressDialog(context, R.style.CustomDialog)
        pd.setCanceledOnTouchOutside(false)
        pd.show()
        auth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener(
                context
            ) { task ->
                if (task.isSuccessful) {
                    pd.dismiss()
                    val intent = Intent(context, StartActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                } else {
                    pd.dismiss()
                    Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show()
                }
            }
    }

    fun createUser(deviceId: String?, context: Activity, username: String, email: String, password: String, userId :String, country : String ,bio :String ,firstName : String,lastName :String) {
        val pd = ProgressDialog(context, R.style.CustomDialog)
        pd.setCanceledOnTouchOutside(false)
        pd.show()
        if (deviceId != null) {
            val collectionRef = FirebaseFirestore.getInstance().collection("Users")
            val query: com.google.firebase.firestore.Query = collectionRef.whereEqualTo("username", username)
            query.get().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    if (task.result.isEmpty){

                        val user = User(username,
                            userId, email, System.currentTimeMillis(), country,bio,firstName,lastName
                            ,"","",0)

                        FirebaseFirestore.getInstance().collection("Users").document(userId)
                            .set(user).addOnSuccessListener {
                                val intent = Intent(context, StartActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                context.startActivity(intent)
                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Something went wrong.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }



                    }else{
                        pd.dismiss()
                        Toast.makeText(context,"This username already used",Toast.LENGTH_SHORT).show()
                    }
                }
            }



        }
    }




    val error: LiveData<String>
        get() = errorMessage
}