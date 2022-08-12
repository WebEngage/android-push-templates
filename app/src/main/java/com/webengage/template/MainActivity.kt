package com.webengage.template

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.webengage.sdk.android.WebEngage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebengage()
    }

    private fun initWebengage() {
        WebEngage.get().user().login("Test1")
    }

}