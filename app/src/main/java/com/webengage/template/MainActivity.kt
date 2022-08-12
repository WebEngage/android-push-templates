package com.webengage.template

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
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