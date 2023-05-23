package com.yeslabapps.ses.interfaces

import com.yeslabapps.ses.model.Voice

interface VoiceClick {


    fun pickVoice(voice: Voice)

    fun seeLikers(voice: Voice)

    fun clickUser(voice : Voice)



}