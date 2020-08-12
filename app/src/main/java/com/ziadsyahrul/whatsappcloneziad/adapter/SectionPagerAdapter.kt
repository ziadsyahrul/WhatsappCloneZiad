package com.ziadsyahrul.whatsappcloneziad.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.ziadsyahrul.whatsappcloneziad.MainActivity
import com.ziadsyahrul.whatsappcloneziad.fragments.ChatsFragment
import com.ziadsyahrul.whatsappcloneziad.fragments.StatusListFragment
import com.ziadsyahrul.whatsappcloneziad.fragments.StatusUpdateFragment

class SectionPagerAdapter(fm: FragmentManager): FragmentPagerAdapter(fm) {

    private val chatsFragment = ChatsFragment()
    private val statusUpdateFragment = StatusUpdateFragment()
    private val statusFragment = StatusListFragment()

    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> statusUpdateFragment // menempatkan StatusUpdateFragment di posisi pertama
            1 -> chatsFragment        // ChatsFragment posisi kedua dalam adapter
            2 -> statusFragment       // StatusListFragment posisi ketiga dalam adapter
            else -> chatsFragment
        }
    }

    override fun getCount(): Int {
        return 3
    }
}