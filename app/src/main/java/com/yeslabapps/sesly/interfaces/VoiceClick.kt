package com.yeslabapps.sesly.interfaces

import com.yeslabapps.sesly.model.Voice

interface VoiceClick {


    fun pickVoice(voice: Voice)

    fun seeLikers(voice: Voice)

    fun clickUser(voice : Voice){

    }

    fun voiceActions(voice: Voice){

    }



}