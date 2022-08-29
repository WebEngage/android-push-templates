package com.webengage.template

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.webengage.sdk.android.WebEngage
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    val FILE_NAME = "PUSH_SHARED_PREFS"
    val CUID_KEY = "cuid"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews() {
        val loginButton: Button = findViewById(R.id.login_button)

        val loginEditText: EditText = findViewById(R.id.login_editText)

        loginButton.setOnClickListener {
            Log.d("Push","Logging in")
            loginEditText.error = null
            val cuidFromEditText = loginEditText.text.toString()
            if (!TextUtils.isEmpty(cuidFromEditText)) {
                writeCuidToSharedPreferences(applicationContext, cuidFromEditText)
                WebEngage.get().user().login(cuidFromEditText)
                loginButton.text = "Logged in"
                loginButton.isEnabled = false
            } else {
                loginEditText.error = "Enter valid CUID"
            }
        }
        val cuidFromEditText = getCuidFromSharedPrefs(applicationContext)
        if(!TextUtils.isEmpty(cuidFromEditText)){
            loginEditText.setText(cuidFromEditText)
            loginButton.isEnabled = false
            loginButton.text = "Logged in"
        }

    }

    private fun writeCuidToSharedPreferences(context: Context, cuid: String) {
        getSharedPrefs(context).edit().putString(CUID_KEY, cuid).apply()
    }

    private fun clearSharedPreferences(context: Context) {
        getSharedPrefs(context).edit().remove(CUID_KEY).apply()
    }

    private fun getCuidFromSharedPrefs(context: Context): String? {
        return getSharedPrefs(context).getString(CUID_KEY, null)
    }

    private fun getSharedPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    }

}