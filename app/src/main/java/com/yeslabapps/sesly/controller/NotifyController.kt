package com.yeslabapps.sesly.controller

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.android.volley.Response
import com.android.volley.toolbox.Volley

import com.yeslabapps.sesly.R
import com.yeslabapps.sesly.notify.Data
import com.yeslabapps.sesly.notify.Sender
import com.yeslabapps.sesly.notify.Token
import com.yeslabapps.sesly.util.Constants
import org.json.JSONException
import org.json.JSONObject

class NotifyController {

    private var requestQueue: RequestQueue? = null

     fun sendNotification(receiver: String, message: String, title: String, sender: String,context : Context) {
         requestQueue = Volley.newRequestQueue(context)

         val db = FirebaseFirestore.getInstance()
        val tokensCollection = db.collection("Tokens")
        val query = tokensCollection.whereEqualTo(FieldPath.documentId(), receiver)

        query.get().addOnSuccessListener { querySnapshot ->
            for (documentSnapshot in querySnapshot) {
                val token = documentSnapshot.toObject(Token::class.java).token

                val data = Data(
                    sender,
                    message,
                    title,
                    receiver,
                   R.mipmap.ic_launcher_round
                )
                val senderData = Sender(token,data)

                try {
                    val senderJsonObj = JSONObject(Gson().toJson(senderData))
                    val jsonObjectRequest = object : JsonObjectRequest(
                        "https://fcm.googleapis.com/fcm/send",
                        senderJsonObj,
                        Response.Listener { response ->
                            Log.d("JSON_RESPONSE 0", "onResponseSuccess: $response")
                        },
                        Response.ErrorListener { error ->
                            Log.d("JSON_RESPONSE 1", "onResponseError: $error")
                        }) {
                        @Throws(AuthFailureError::class)
                        override fun getHeaders(): Map<String, String> {
                            val headers = HashMap<String, String>()

                            headers["Content-Type"] = "application/json"
                            headers["Authorization"] = "key=${Constants.FCM_KEY}"
                            return headers
                        }
                    }
                    requestQueue!!.add(jsonObjectRequest)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }.addOnFailureListener { error ->
            Log.d("QUERY_ERROR", "Failed to query tokens collection: $error")
        }
    }


}