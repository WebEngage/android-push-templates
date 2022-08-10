package com.webengage.pushtemplates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.webengage.sdk.android.WebEngage

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
        initWebengage()
    }

    private fun initWebengage() {
        WebEngage.get().user().login("PushTemplate")
    }

    private fun initViews() {
        val editText_type = findViewById<EditText>(R.id.editText_type)
        editText_type.setText("bar")
    }
}