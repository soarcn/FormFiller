package com.cocosw.formfiller

import android.app.Application
import android.view.KeyEvent

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)

            FormFiller.Builder(this)
                .keyCode(KeyEvent.KEYCODE_F)
                .doubleTap()
                .enableScenariosSwitcher()
                .bullet {
                    id(R.id.username, "username")
                    id(R.id.password, "password")
                }
                .bullet("Unhappy") {
                    id(R.id.username, "wrong")
                    id(R.id.password, "wrong")
                }
                .build()
    }
}