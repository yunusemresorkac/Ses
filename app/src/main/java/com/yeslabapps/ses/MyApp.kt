package com.yeslabapps.ses

import android.app.Application
import com.yeslabapps.ses.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MyApp : Application(){

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}