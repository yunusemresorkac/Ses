package com.yeslabapps.ses.model

data class User(val username : String = "",
                var userId : String = "",
                val email : String = "",
                val registerDate : Long = 0,
                val country : String = "",
                val bio : String = "",
                val firstName : String = "",
                val lastName : String = "",
                val deviceId : String = "",
                val profileVoice : String = "",
                var followers : Int = 0)
