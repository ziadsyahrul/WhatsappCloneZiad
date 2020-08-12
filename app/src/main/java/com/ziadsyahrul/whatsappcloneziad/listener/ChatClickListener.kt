package com.ziadsyahrul.whatsappcloneziad.listener

interface ChatClickListener {
    fun onChatClicked(name:String?, otherUserId:String?, chatsImageUrl:String?, chatsName:String?)
}