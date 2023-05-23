package com.yeslabapps.ses.di

import com.yeslabapps.ses.viewmodel.FirebaseViewModel
import com.yeslabapps.ses.viewmodel.LoginActivityViewModel
import com.yeslabapps.ses.viewmodel.MyLikesViewModel
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
