package com.s4ltf1sh.messenger_clientapp.service

import android.os.Handler
import android.os.Looper

abstract class IncomingHandler(looper: Looper) : Handler(looper) {
    companion object {
        const val MSG_TO_SERVER = 1
        const val MSG_FROM_SERVER = 2
    }
}