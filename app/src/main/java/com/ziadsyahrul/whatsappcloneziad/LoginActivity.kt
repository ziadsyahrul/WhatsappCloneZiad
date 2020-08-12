package com.ziadsyahrul.whatsappcloneziad

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
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_login)

        setTextChangedListener(edt_email, till_email)
        setTextChangedListener(edt_password, till_password)
        progress_layout.setOnTouchListener { v, event -> true }

        btn_login.setOnClickListener { // ketika button login diklik, menjalankan fungsi onLogin
            onLogin()
        }
        txt_singup.setOnClickListener { // ketika text signup diklik, menjalankan fungsi onSignup
            onSignup()
        }
    }

    private fun setTextChangedListener(edt: TextInputEditText?, till: TextInputLayout?) {
        edt?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // fungsi ketika text dalam editText setelah diubah

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // fungsi ketika text dalam editText sebelum diubah
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // fungsi ketika text dalam editText sedang diubah
                // TextInputLayout tidak menunjukkan pesan error
                till?.isErrorEnabled = false
            }
        })

    }

    private fun onLogin() {
        var proceed = true
        if (edt_email.text.isNullOrEmpty()) { // check jika EditText kosong
            till_email.error = "Required Email" // TextInputLayout(till) menampilkan pesan
            till_email.isErrorEnabled = true // mengubah state till yang sebelumnya tidak
            proceed = false // menampilkan error sekarang menampilkan
        }

        if (edt_password.text.isNullOrEmpty()) {
            till_password.error = "Required Password"
            till_password.isErrorEnabled = true
            proceed = false
        }

        if (proceed) {
            progress_layout.visibility = View.VISIBLE // menampilkan ProgressBar
            firebaseAuth.signInWithEmailAndPassword( // untuk menunjukkan bahwa ada proses yang sedang dilakukan
                edt_email.text.toString(),
                edt_password.text.toString() // mengubah data dalam editText jadi string
            ).addOnCompleteListener { task -> // jika proses sebelumnya selesai dilaksanakan
                if (!task.isSuccessful) { // jika proses yang selesai dilaksanakan hasilnya kegagalan
                    progress_layout.visibility = View.GONE // ProgressBar dihilangkan
                    Toast.makeText( // ditampilkan pesan error melalui Toast
                        this, "Login Error: ${task.exception?.localizedMessage}"
                        , Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { // jika proses sebelumnya tidak dilaksanakan
                progress_layout.visibility = View.GONE // ProgressBar dihilangkan
                it.printStackTrace() // menampilkan log errornya
            }
        }
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)
    }

    private fun onSignup() {
        startActivity(Intent(this, SignUpActivity::class.java))
        finish()
    }


}

