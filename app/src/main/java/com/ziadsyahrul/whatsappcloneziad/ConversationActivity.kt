package com.ziadsyahrul.whatsappcloneziad

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.ziadsyahrul.whatsappcloneziad.adapter.ConversationAdapter
import com.ziadsyahrul.whatsappcloneziad.util.*
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)

    private val firebaseDb = FirebaseFirestore.getInstance()
    private var chatId: String? = null
    private var imageUrl: String? = null
    private var otherUserId: String? = null
    private var chatName: String? = null

    companion object {
        private val PARAM_CHAT_ID = "Chat_id"
        private val PARAM_IMAGE_URL = "Image_url"
        private val PARAM_OTHER_USER_ID = "Other_user_id"
        private val PARAM_CHAT_NAME = "Chat_name"

        fun newIntent(
            context: Context?,
            chatId: String?,
            imageUrl: String?,
            otherUserId: String?,
            chatName: String?
        ): Intent {
            val intent = Intent(context, ConversationActivity::class.java)
            intent.putExtra(PARAM_CHAT_ID, chatId)
            intent.putExtra(PARAM_IMAGE_URL, imageUrl)
            intent.putExtra(PARAM_OTHER_USER_ID, otherUserId)
            intent.putExtra(PARAM_CHAT_NAME, chatName)
            return intent
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        setSupportActionBar(toolbar_conversation)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_conversation.setNavigationOnClickListener { onBackPressed() }

        chatId = intent.extras?.getString(PARAM_CHAT_ID)
        imageUrl = intent.extras?.getString(PARAM_IMAGE_URL)
        chatName = intent.extras?.getString(PARAM_CHAT_NAME)
        otherUserId = intent.extras?.getString(PARAM_OTHER_USER_ID)

        if (chatId.isNullOrEmpty() || userId.isNullOrEmpty()) {
            Toast.makeText(this, "Chat Room Error", Toast.LENGTH_SHORT).show()
            finish()
        }

        populateImage(this, imageUrl, img_toolbar, R.drawable.ic_user)
        txt_toolbar.text = chatName
        rv_message.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        firebaseDb.collection(DATA_CHATS)
            .document(chatId!!)
            .collection(DATA_CHAT_MESSAGE)
            .orderBy(DATA_CHAT_MESSAGE_TIME)
            .addSnapshotListener { value, error ->
                if (error != null) {                // kalo ada error
                    error.printStackTrace()        // error dicetak/printStackTrace
                    return@addSnapshotListener     // dicetak di logcat dengan format menumpuk
                } else {                   //jika else
                    if (value != null) {    // jika value tidak null/ ada datanya
                        for (change in value.documentChanges) {  // maka dia looping dari DATA_CHAT_MESSAGE
                            when (change.type) {                 // di looping ketika ada perubahan
                                DocumentChange.Type.ADDED -> {  // kalau perubahannya berupa penambahan
                                    val message =
                                        change.document.toObject(Message::class.java)    // data ditampung
                                    if (message != null) {  // kalo message nya ada
                                        conversationAdapter.addMessage(message)  //adapter nambahin data pesan
                                        rv_message.post {     //adapter udah nambah, di post lah ke recycler view, sekian terimagaji
                                            rv_message.smoothScrollToPosition(conversationAdapter.itemCount - 1)
                                        }
                                    }
                                }
                                DocumentChange.Type.REMOVED -> {
                                } // kalau perubahannya berupa penghapusan
                            }
                        }
                    }
                }
            }


        imbtn_send.setOnClickListener {
            if (!edt_message.text.isNullOrEmpty()) {
                val message =
                    Message(userId, edt_message.text.toString(), System.currentTimeMillis())

                firebaseDb.collection(DATA_CHATS)
                    .document(chatId!!)
                    .collection(DATA_CHAT_MESSAGE)
                    .document()
                    .set(message)
                edt_message.setText("", TextView.BufferType.EDITABLE) // add editText
            }
        }
    }
}