package com.yeslabapps.sesly.model

data class Voice(val voiceTitle : String="",
                 val voiceUrl : String="",
                 val voiceId : String="",
                 val publisherId : String="",
                 val time : Long=0,
                 val duration : Int = 0,
                 val tags: List<String>?= null,
                 var countOfLikes : Int = 0,
                 val relatedCountry : String = "",
                 var listened : Int = 0)

