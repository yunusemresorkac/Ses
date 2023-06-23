package com.yeslabapps.sesly.di

import com.yeslabapps.sesly.viewmodel.FirebaseViewModel
import com.yeslabapps.sesly.viewmodel.LoginActivityViewModel
import com.yeslabapps.sesly.viewmodel.MyLikesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    viewModel{
        FirebaseViewModel(get())
    }
    viewModel{
        LoginActivityViewModel(get())
    }

    viewModel {
        MyLikesViewModel(get())
    }

}
