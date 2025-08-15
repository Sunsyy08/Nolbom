// NolbomApplication.kt - 앱 전체 초기화
package com.project.nolbom

import android.app.Application
import android.util.Log
import com.project.nolbom.data.local.TokenStore

class NolbomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // 🔥 TokenStore 초기화
        TokenStore.init(this)

        // 🔥 앱 시작시 STT 상태 로깅 (디버깅용)
        TokenStore.logSTTStatus()

        Log.d("NolbomApplication", "앱 초기화 완료")
    }
}