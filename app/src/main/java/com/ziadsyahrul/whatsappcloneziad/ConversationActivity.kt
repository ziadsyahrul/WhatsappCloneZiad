package com.ziadsyahrul.whatsappcloneziad

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.ziadsyahrul.whatsappcloneziad.adapter.ConversationAdapter
import com.ziadsyahrul.whatsappcloneziad.util.Message
import kotlinx.android.synthetic.main.activity_conversation.*

class ConversationActivity : AppCompatActivity() {

    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private val conversationAdapter = ConversationAdapter(arrayListOf(), userId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)
        rv_message.apply {
            setHasFixedSize(false)
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        //dummy
        conversationAdapter.addMessage(Message(userId, "Haloo", 2))
        conversationAdapter.addMessage(Message("everytime", "How are you?", 3))
        conversationAdapter.addMessage(Message(userId, "I'm good, how are you?", 4))
        conversationAdapter.addMessage(Message("everytime", "Me too", 5))
        imbtn_send.setOnClickListener {

        }
    }
}