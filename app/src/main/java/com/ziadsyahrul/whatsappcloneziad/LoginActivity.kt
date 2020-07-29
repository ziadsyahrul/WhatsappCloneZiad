package com.ziadsyahrul.whatsappcloneziad

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btn_login.setOnClickListener {
            onLogin()
        }
        txt_singup.setOnClickListener {
            onSignup()
        }
    }

    private fun onLogin() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun onSignup() {
        startActivity(Intent(this, SignUpActivity::class.java))
    }
}