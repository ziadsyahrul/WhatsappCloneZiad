package com.ziadsyahrul.whatsappcloneziad

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ziadsyahrul.whatsappcloneziad.util.DATA_USERS
import com.ziadsyahrul.whatsappcloneziad.util.User
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private val firebaseDB = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseListener = FirebaseAuth.AuthStateListener {
        val user = FirebaseAuth.getInstance().currentUser?.uid
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_sign_up)

        setTextChangedListener(edt_email, till_email_signup)
        setTextChangedListener(edt_password, till_password_signup)
        setTextChangedListener(edt_name, till_name)
        setTextChangedListener(edt_phone, till_phone)
        progress_layout_signup.setOnTouchListener { v, event -> true }

        btn_signup.setOnClickListener {
            onSignUp()
        }

        txt_login.setOnClickListener {
            onLogin()
        }
    }

    private fun setTextChangedListener(edt: TextInputEditText?, till: TextInputLayout?) {
        edt?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                till?.isErrorEnabled = false
            }
        })
    }

    private fun onSignUp() {
        var proceed = true
        if (edt_name.text.isNullOrEmpty()) {
            till_name.error = "Required Name"
            till_name.isErrorEnabled = true
            proceed = false
        }

        if (edt_phone.text.isNullOrEmpty()) {
            till_phone.error = "Required Phone Number"
            till_phone.isErrorEnabled = true
            proceed = false
        }

        if (edt_email_signup.text.isNullOrEmpty()) {
            till_email_signup.error = "Required Password"
            till_email_signup.isErrorEnabled = true
            proceed = false
        }

        if (edt_password_signup.text.isNullOrEmpty()) {
            till_password_signup.error = "Required Password"
            till_password_signup.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            progress_layout_signup.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(
                edt_email_signup.text.toString(),
                edt_password_signup.text.toString()
            )
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        progress_layout_signup.visibility = View.GONE
                        Toast.makeText(
                            this, "SignUp Error: ${it.exception?.localizedMessage}",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (firebaseAuth.uid != null) { // jika userId dalam activity saat ini null
                        val email = edt_email_signup.text.toString() // mengubah text yang di editText menjadi string
                        val phone = edt_phone.text.toString()
                        val name = edt_name.text.toString()
                        val user = User(email, phone, name, "", "Hello World! I'm new", "", "")

                        firebaseDB.collection(DATA_USERS) // mengakses database table Users
                            .document(firebaseAuth.uid!!).set(user) // menambahkan user dan datanya
                    }

                    progress_layout_signup.visibility = View.GONE


                }
                .addOnFailureListener {
                    progress_layout_signup.visibility = View.GONE
                    it.printStackTrace()
                }

        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseListener)
    }

    private fun onLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}