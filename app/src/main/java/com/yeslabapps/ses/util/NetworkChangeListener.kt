package com.yeslabapps.ses.util

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.widget.AppCompatButton;
import com.yeslabapps.ses.R
import com.yeslabapps.ses.util.Common.isConnectedToInternet


class NetworkChangeListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (!isConnectedToInternet(context!!)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            val dialog: View =
                LayoutInflater.from(context).inflate(R.layout.check_internet_dialog, null)
            builder.setView(dialog)
            val btnRetry: AppCompatButton = dialog.findViewById(R.id.checkNetBtn)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            alertDialog.setCancelable(false)
            alertDialog.window?.setGravity(Gravity.CENTER)
            btnRetry.setOnClickListener {
                alertDialog.dismiss()
                onReceive(context, intent)
            }
        }
    }
}