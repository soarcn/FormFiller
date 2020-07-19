package com.cocosw.formfiller

import android.app.Application
import android.view.KeyEvent
import com.mooveit.library.Fakeit

class DemoApp : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            Fakeit.init()
            FormFiller.Builder(this)
                .keyCode(KeyEvent.KEYCODE_F)
                .doubleTap()
                .enableScenariosSwitcher()
                .bullet {
                    id(R.id.username, "username")
                    id(R.id.password, "password")
                    id(R.id.confimrpassword, "password")
                    id(R.id.name, "Form filler")
                    id(R.id.dob, "19/07/2020")
                    id(R.id.address, "Sydney,Australia")

                }
                .bullet("Unhappy") {
                    id(R.id.username, "wrong")
                    id(R.id.password, "wrong")
                    id(R.id.confimrpassword, "wrong")
                    id(R.id.name) {
                        it.setText(Fakeit.name().lastName())
                    }
                }
                .build()
    }
}