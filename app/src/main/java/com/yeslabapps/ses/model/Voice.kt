package com.yeslabapps.ses.model

data class Voice(val voiceTitle : String="",
                val voiceUrl : String="",
                val voiceId : String="",
                val publisherId : String="",
                val time : String="",
                val voiceTime : Int = 0,
                val tags: List<String>?= null,
                var countOfLikes : Int = 0)

