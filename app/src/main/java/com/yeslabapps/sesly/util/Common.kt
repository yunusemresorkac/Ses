package com.yeslabapps.sesly.util

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

object Common {
    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivityManager.allNetworkInfo
        for (i in info.indices) {
            if (info[i].state == NetworkInfo.State.CONNECTED) {
                return true
            }
        }
        return false
    }
}
