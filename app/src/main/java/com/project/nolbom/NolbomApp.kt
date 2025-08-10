package com.project.nolbom

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import com.project.nolbom.data.local.TokenStore

class NolbomApp : Application() {
    override fun onCreate() {
        super.onCreate()
        TokenStore.init(this)

        // 카카오맵 SDK 초기화 (네이티브 앱 키 사용)
        KakaoMapSdk.init(this, "43dc42fa64bbd715fef5f7cffc8cceda")
    }
}
