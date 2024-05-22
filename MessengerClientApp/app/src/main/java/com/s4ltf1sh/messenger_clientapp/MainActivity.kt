package com.s4ltf1sh.messenger_clientapp

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.s4ltf1sh.messenger_clientapp.service.IncomingHandler
import com.s4ltf1sh.messenger_clientapp.ui.theme.MessengerClientAppTheme

class MainActivity : ComponentActivity(), ServiceConnection {
    private var mServiceMessenger: Messenger? = null

    private var mClientMessenger: Messenger? = null

    // Flag indicating whether we have called bind on the service
    private var mBound: Boolean = false

    private val mIncomingHandler = object : IncomingHandler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (!handleMessageFromService(msg))
                super.handleMessage(msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MessengerClientAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(modifier = Modifier.padding(innerPadding)) {
                        Button(onClick = { bindToService() }) {
                            Text(text = "Bind to service")
                        }

                        Button(onClick = { unbindFromService() }) {
                            Text(text = "Unbind service")
                        }

                        Button(
                            modifier = Modifier,
                            onClick = { sendMessageToServer("Hello from client") }
                        ) {
                            Text(text = "Send message to server")
                        }
                    }
                }
            }
        }
    }

    private fun handleMessageFromService(msg: Message): Boolean {
        // Handle message from service
        return when (msg.what) {
            IncomingHandler.MSG_FROM_SERVER -> {
                val message = msg.data.getString("message")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                true
            }

            else -> false
        }
    }

    private fun sendMessageToServer(content: String) {
        if (!mBound) return
        // Create and send a message to the service, using a supported 'what' value
        val msg: Message = Message.obtain(mIncomingHandler, IncomingHandler.MSG_TO_SERVER)
        msg.data = Bundle().apply {
            putString("message", content)
        }

        msg.replyTo = mClientMessenger

        try {
            mServiceMessenger?.send(msg)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun bindToService() {
        mClientMessenger = Messenger(mIncomingHandler)
        // Bind to the service using an explicit intent
        Intent("connect_from_client").also { intent ->
            intent.`package` = "com.s4ltf1sh.messenger_motherapp"
            applicationContext?.bindService(intent, this, BIND_AUTO_CREATE)
        }
    }

    private fun unbindFromService() {
        if (mBound) {
            applicationContext.unbindService(this)
            mBound = false
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        // 4. Clients use the IBinder to instantiate the Messenger (that references the service's Handler),
        // which the client uses to send Message objects to the service.

        // All that a client needs to do is create a Messenger based on the IBinder
        // returned by the service and send a message using send()
        mServiceMessenger = Messenger(service)
        mBound = true
        Toast.makeText(this@MainActivity, "Remote Service connected", Toast.LENGTH_SHORT).show()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        // This is called when the connection with the service has been unexpectedly disconnected -- that is, its process crashed.
        mServiceMessenger = null
        mBound = false
        Toast.makeText(this@MainActivity, "Remote Service disconnected", Toast.LENGTH_SHORT)
            .show()
    }
}