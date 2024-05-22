package com.s4ltf1sh.messenger_motherapp.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import com.s4ltf1sh.messenger_motherapp.Event
import com.s4ltf1sh.messenger_motherapp.SimpleEventBus

class MessengerService : Service() {
    //1. The service implements a Handler that receives a callback for each call from a client.
    private var mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            //5. The service receives each Message in its Handler—specifically, in the handleMessage() method.
            if (handleClientMessage(msg).not())
                super.handleMessage(msg)
        }
    }

    private fun handleClientMessage(msg: Message): Boolean {
        //The handleMessage() method in the Handler is where the service receives the incoming Message
        // and decides what to do, based on the what member.
        return when (msg.what) {
            IncomingHandler.MSG_FROM_CLIENT -> {
                val message = msg.data.getString("message", "Error: No message received")
                SimpleEventBus.post(Event(message))

                //2. The service uses the Handler to create a Messenger object (which is a reference to the Handler).
                sendMessagesToClient(msg, "Message received: $message")

                true
            }

            else -> false
        }
    }

    private fun sendMessagesToClient(msg: Message, content: String) {
        val message = Message.obtain(mHandler, IncomingHandler.MSG_TO_CLIENT)
        message.data = Bundle().apply {
            putString("message", content)
        }

        // replyTo is client's messenger which reference to client's Handler
        // which handle the message from server
        msg.replyTo.send(message)
    }

    override fun onBind(intent: Intent?): IBinder? {
        // 3. The Messenger creates an IBinder that the service returns to clients from onBind().
        val messenger = Messenger(mHandler)
        return messenger.binder
    }
}