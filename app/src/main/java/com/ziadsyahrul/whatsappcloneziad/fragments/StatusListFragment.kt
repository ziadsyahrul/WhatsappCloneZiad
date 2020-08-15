package com.ziadsyahrul.whatsappcloneziad.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ziadsyahrul.whatsappcloneziad.R
import com.ziadsyahrul.whatsappcloneziad.StatusActivity
import com.ziadsyahrul.whatsappcloneziad.adapter.StatusListAdapter
import com.ziadsyahrul.whatsappcloneziad.listener.StatusItemClickListener
import com.ziadsyahrul.whatsappcloneziad.util.DATA_USERS
import com.ziadsyahrul.whatsappcloneziad.util.DATA_USER_CHATS
import com.ziadsyahrul.whatsappcloneziad.util.StatusListElement
import com.ziadsyahrul.whatsappcloneziad.util.User
import kotlinx.android.synthetic.main.fragment_list_status.*

class StatusListFragment : Fragment(), StatusItemClickListener {

    private val firebaseDb = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var statusListAdapter = StatusListAdapter(arrayListOf())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_status, container, false)
    }

    override fun onItemClicked(statusElement: StatusListElement) {
        startActivity(StatusActivity.getIntent(context, statusElement))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusListAdapter.setOnItemClickListener(this)
        rv_status_list.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = statusListAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        onVisible()

        fab_status_list.setOnClickListener {
            onVisible()
        }
    }

    private fun onVisible() {
        statusListAdapter.onRefresh()
        refreshList()
    }

    private fun refreshList() {
        firebaseDb.collection(DATA_USERS).document(userId!!)
            .get()  // ngambil data user, lalu ambil document berdasarkan id
            .addOnSuccessListener {
                if (it.contains(DATA_USER_CHATS)) {      //jika document menganduung document userChats
                    val partners =
                        it[DATA_USER_CHATS]  // document userChats ditampung di variable partners untuk dilooping
                    for (partner in (partners as HashMap<String, String>).keys) {  //dilooping untuk mengambil key dari hashmap
                        //dimana key kita gunakan untuk mendapatkan document userChatPartner
                        firebaseDb.collection(DATA_USERS).document(partner)
                            .get() // kita gunakan key tersebut disini untuk ambil datanya
                            .addOnSuccessListener { documentSnapshot ->
                                val partner =
                                    documentSnapshot.toObject(User::class.java)  // buat nampung data dari document userChatPartner ditampung di Model User
                                if (partner != null) {                                //jika partner (model user yang sekarang) tidak kosong
                                    if (!partner.status.isNullOrEmpty() || !partner.statusUrl.isNullOrEmpty()) {    //data status atau statusUrl di model tidak kosong
                                        val newElement = StatusListElement(
                                            partner.name,
                                            partner.imageUrl,
                                            partner.status,    //ngisi model statusListElement
                                            partner.statusUrl, //dengan data dari Model User
                                            partner.statusTime
                                        )
                                        statusListAdapter.addElement(newElement)
                                    }
                                }
                            }
                    }
                }
            }
    }

}