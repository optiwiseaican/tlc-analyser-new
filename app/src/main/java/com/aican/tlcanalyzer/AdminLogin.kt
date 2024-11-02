package com.aican.tlcanalyzer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.aican.tlcanalyzer.utils.SharedPrefData
import com.aican.tlcanalyzer.utils.Source
import com.aican.tlcanalyzer.utils.Subscription

class AdminLogin : AppCompatActivity() {
    lateinit var back: ImageView
    lateinit var userID: EditText
    lateinit var password: EditText
    lateinit var btnAdminLogin: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        supportActionBar?.hide()

        back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            finish()
        }

        btnAdminLogin = findViewById(R.id.btnAdminLogin)
        password = findViewById(R.id.password)
        userID = findViewById(R.id.userID)

        if (SharedPrefData.getSavedData(this, Subscription.adminIDKey) != null &&
            SharedPrefData.getSavedData(this, Subscription.adminPassKey) != null
        ) {
            Subscription.adminID = SharedPrefData.getSavedData(this, Subscription.adminIDKey)
            Subscription.adminPass = SharedPrefData.getSavedData(this, Subscription.adminPassKey)
        }

        btnAdminLogin.setOnClickListener {
            checkCreds(userID.text.toString(), password.text.toString())
        }


    }

    private fun checkCreds(userIDTxt: String, passwordTxt: String) {
        if (userIDTxt == "" || passwordTxt == "") {
            if (userIDTxt == "") {
                userID.error = "Enter your ID"
            }
            if (passwordTxt == "") {
                password.error = "Enter your passcode"
            }
        } else {
            if (userIDTxt == Subscription.adminID && passwordTxt == Subscription.adminPass) {


                startActivity(Intent(this@AdminLogin, AllUsersMade::class.java))
                finish()
            } else {
                Source.toast(this@AdminLogin, "Invalid Credentials")
            }
        }
    }


}