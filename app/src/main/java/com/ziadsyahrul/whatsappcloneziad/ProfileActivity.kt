package com.ziadsyahrul.whatsappcloneziad

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.ziadsyahrul.whatsappcloneziad.util.*
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_profile.*
import org.w3c.dom.Text

class ProfileActivity : AppCompatActivity() {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseStorage = FirebaseStorage.getInstance().reference
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        imbtn_profile.setOnClickListener {        // if imageButton diklik maka akan meminta izin
            val intent = Intent(Intent.ACTION_PICK) //untuk mengakses gallery handphone
            intent.type =
                "image/*"               //syntax mengakses gallery dengan intent (berbentuk path)
            startActivityForResult(
                intent,
                REQUEST_CODE_PHOTO
            ) //setelah memilih gambar akan diteruskan dalam function onActivityResult
        }

        if (userId.isNullOrEmpty()) {  // jika userId null, profileactivity akan
            finish()                   // dihentikan dan akan dikembalikan ke MainActivity
        }

        progress_layout.setOnTouchListener { v, event -> true }
        btn_apply.setOnClickListener {
            onApply()
        }

        btn_delete_account.setOnClickListener {
            onDelete()
        }

        populateInfo()
    }

    private fun populateInfo() {
        progress_layout.visibility = View.VISIBLE
        firebaseDb.collection(DATA_USERS).document(userId!!).get() //membaca data table user
            .addOnSuccessListener {           // jika proses berhasil, data akan ditampung lalu
                val user = it.toObject(User::class.java) //dipasang di editText
                imageUrl = user?.imageUrl //menampung imageUrl dengan property imageUrl
                edt_name_profile.setText(user?.name, TextView.BufferType.EDITABLE)
                edt_email_profile.setText(user?.email, TextView.BufferType.EDITABLE)
                edt_phone_profile.setText(user?.phone, TextView.BufferType.EDITABLE)
                if (imageUrl != null) {
                    populateImage(this, user?.imageUrl, img_profile, R.drawable.ic_user)
                }
                progress_layout.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                finish()
            }
    }

    fun onApply() {
        progress_layout.visibility = View.VISIBLE
        val name = edt_name_profile.text.toString()
        val email = edt_email_profile.text.toString()
        val phone = edt_phone_profile.text.toString() //data text dalam editText akan diubah
        val map =
            HashMap<String, Any>()              // menjadi string lalu ditampung dalam variable
        map[DATA_USER_NAME] = name                    // yang nantinya akan dikoleksi di hashmap
        map[DATA_USER_EMAIL] = email                  // untuk nanti dikirim ke table user
        map[DATA_USER_PHONE] = phone                  // di database firebase sebagai pembaruan

        firebaseDb.collection(DATA_USERS).document(userId!!).update(map) //perintah update
            .addOnSuccessListener {
                Toast.makeText(this, "Update Successfull", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                progress_layout.visibility = View.GONE
            }
    }

    fun onDelete() {
        progress_layout.visibility = View.VISIBLE
        AlertDialog.Builder(this)  //ketika tombol DELETE diklik, Alert dialog akan muncul
            .setTitle("Delete Account")   // Title alert dialog
            .setMessage("This will delete your profile information. Are you sure?") //pesan info
            .setPositiveButton("YES") { dialog, which -> // button YES
//                firebaseDb.collection(DATA_USERS).document(userId!!).delete()
//                progress_layout.visibility = View.GONE
//                Toast.makeText(this, "Profile Deleted", Toast.LENGTH_SHORT).show()
//                finish()
                firebaseStorage.child(DATA_IMAGES).child(userId!!).delete()
                firebaseDb.collection(DATA_USERS).document(userId!!).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }
                    ?.addOnFailureListener {
                        finish()
                    }
            }
            .setNegativeButton("No") { dialog, which -> // Button No
                progress_layout.visibility = View.GONE
            }
            .setCancelable(false) // AlertDialog tidak dapat hilang kecuali memencet tombol yes/no
            .show() //memunculkan alertdialog
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storageImage(data?.data)   // method storeImage dijalankan setelah pengguna memilih gambar
        }
    }

    private fun storageImage(uri: Uri?) {
        if (uri != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            progress_layout.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userId!!)

            filePath.putFile(uri)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString()
                            firebaseDb.collection(DATA_USERS)
                                .document(userId)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(this, imageUrl, img_profile, R.drawable.ic_user)
                                }
                            progress_layout.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailured()
                        }
                }
                .addOnFailureListener {
                    onUploadFailured()
                }
        }
    }

    private fun onUploadFailured() {
        Toast.makeText(this, "Image Upload failed. Please try again later", Toast.LENGTH_SHORT)
            .show()
        progress_layout.visibility = View.GONE
    }
}
