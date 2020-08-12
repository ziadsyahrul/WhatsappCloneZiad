package com.ziadsyahrul.whatsappcloneziad

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ziadsyahrul.whatsappcloneziad.adapter.SectionPagerAdapter
import com.ziadsyahrul.whatsappcloneziad.fragments.ChatsFragment
import com.ziadsyahrul.whatsappcloneziad.listener.FailureCallback
import com.ziadsyahrul.whatsappcloneziad.util.DATA_USERS
import com.ziadsyahrul.whatsappcloneziad.util.DATA_USER_PHONE
import com.ziadsyahrul.whatsappcloneziad.util.PERMISSION_REQUEST_READ_CONTACT
import com.ziadsyahrul.whatsappcloneziad.util.REQUEST_NEW_CHATS
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.util.jar.Manifest

class MainActivity : AppCompatActivity(), FailureCallback {

    companion object {
        const val PARAM_NAME = "name"
        const val PARAM_PHONE = "phone"
    }

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var mSectionPagerAdapter: SectionPagerAdapter
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val chatsFragment = ChatsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chatsFragment.setFailureCallBackListener(this)

        setSupportActionBar(toolbaar)
        mSectionPagerAdapter =
            SectionPagerAdapter(
                supportFragmentManager
            )

        container.adapter = mSectionPagerAdapter
        container.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(container))
        resizeTabs()
        tabs.getTabAt(1)?.select()


        fab.setOnClickListener {
            onNewChat()
        }

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {  // ketika TabItem diklik atau TabItem aktif FloatingActionButton
                    0 -> fab.hide()    // akan hilang pada Tab pertama
                    1 -> fab.show()    // tetap ditampilkan pada Tab kedua
                    3 -> fab.hide()    // akan hilang pada Tab ketiga
                }
            }

        })
    }

    private fun onNewChat() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED
        ) {

            //jika tidak diberikan izin
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts permission")
                    .setMessage("This App Requires Access your Contacs to Initiation A Concersation")
                    .setPositiveButton("YES") { dialog, which ->
                        requestContactPermission()                // meminta izin membaca kontak untuk aplikasi
                    }
                    .setNegativeButton("NO") { dialog, which ->

                    }
                    .show()

            } else {
                requestContactPermission()
            }
        } else {
            startNewActivity()
        }

    }

    private fun startNewActivity() {
        startActivityForResult(Intent(this, ContactActivity::class.java), REQUEST_NEW_CHATS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NEW_CHATS -> {
                    val name = data?.getStringExtra(PARAM_NAME) ?: "" //elvis operator
                    val phone = data?.getStringExtra(PARAM_PHONE) ?: ""  //elvis operator
                    checkNewChatUser(name, phone)
                }
            }
        }
    }

    private fun checkNewChatUser(name: String, phone: String) {
        if (!name.isNullOrEmpty() && !phone.isNullOrEmpty()) {
            firebaseDb.collection(DATA_USERS)          // akses table user di pairbes
                .whereEqualTo(
                    DATA_USER_PHONE,
                    phone
                )  // buat bandingin data yg di database sama data yang di kontak
                .get()
                .addOnSuccessListener {
                    if (it.documents.size > 0) {
                        chatsFragment.newChat(it.documents[0].id)
                    } else {
                        AlertDialog.Builder(this).setTitle("User Not Found")
                            .setMessage("$name does not have an account. Send them an SMS to install this app.")

                            .setPositiveButton("OK") { dialog, which ->
                                val intent = Intent(Intent.ACTION_VIEW)
                                intent.data = Uri.parse("sms:$phone")
                                intent.putExtra(
                                    "sms_body",
                                    "Hi I'm using this new coll WhatsappClone app. You should install it too so we can chat there."
                                )
                                startActivity(intent)
                            }

                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "An error occured. Please try again later",
                        Toast.LENGTH_SHORT
                    ).show()
                    e.printStackTrace()
                }
        }
    }

    private fun requestContactPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_READ_CONTACT
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_READ_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startNewActivity()     // memulai activity yang lain dengan memakai intent
                }
            }
        }
    }

    private fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.4f
        layout.layoutParams = layoutParams
    }

    override fun onResume() {
        super.onResume()
        if (firebaseAuth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onProfile()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()

    }

    private fun onProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    override fun userError() {
        Toast.makeText(this, "User Not Found", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    /*class PlaceHolderFragment: Fragment() {
        companion object {
            private val ARG_SECTION_NUMBER = "section_number"

            fun newIntent(sectionNumber: Int): PlaceHolderFragment {
                val fragment = PlaceHolderFragment()
                val args = Bundle()
                args.putInt(ARG_SECTION_NUMBER, sectionNumber)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
            rootView.section_lable.text = "Hello World, from section ${arguments?.getInt(
                ARG_SECTION_NUMBER)}"
            return rootView
        }
    }*/
}