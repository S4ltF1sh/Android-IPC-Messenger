package com.s4ltf1sh.messenger_motherapp.service

import android.os.Handler

abstract class IncomingHandler : Handler() {
    companion object {
        const val MSG_FROM_CLIENT = 1
        const val MSG_TO_CLIENT = 2
    }
}