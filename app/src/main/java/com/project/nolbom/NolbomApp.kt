package com.project.nolbom

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.project.nolbom.data.local.TokenStore

class NolbomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)

        // 카카오 SDK 초기화
        KakaoSdk.init(this, getString(R.string.kakao_native_app_key))
    }
}
