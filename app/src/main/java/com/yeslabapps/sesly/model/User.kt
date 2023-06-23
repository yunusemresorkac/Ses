package com.yeslabapps.sesly.model

data class User(val username : String = "",
                var userId : String = "",
                val email : String = "",
                val registerDate : Long = 0,
                val country : String = "",
                val bio : String = "",
                val profileVoice : String = "",
                var followers : Int = 0,
                val privateProfile : Boolean = false,
                val lastSeen : Long = 0,
                val userType : Int = 0,
                val followerNotify : Boolean = true,
                val likeNotify : Boolean = true,
                val joinDateVisibility : Boolean = true,
                val countryVisibility : Boolean = true)
