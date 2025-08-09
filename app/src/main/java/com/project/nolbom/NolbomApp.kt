package com.project.nolbom

import android.app.Application
import com.project.nolbom.data.local.TokenStore

class NolbomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)
    }
}
