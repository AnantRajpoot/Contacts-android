package com.devdroid.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
Activity to display contact details and
provide call and messaging functionalities.
 */
class ContactDetailActivity : AppCompatActivity() {
    // Variables to store contact details
    private lateinit var contactName: String
    private lateinit var contactNumber: String

    // UI elements
    private lateinit var contactTV: TextView
    private lateinit var nameTV: TextView
    private lateinit var contactIV: ImageView
    private lateinit var callIV: ImageView
    private lateinit var messageIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)

        // Retrieve contact details from intent extras
        contactName = intent.getStringExtra("name").toString()
        contactNumber = intent.getStringExtra("contact").toString()

        // Initialize UI elements
        nameTV = findViewById(R.id.name)
        contactTV = findViewById(R.id.number)
        contactIV = findViewById(R.id.profileImage)
        callIV = findViewById(R.id.callButton)
        messageIV = findViewById(R.id.messageButton)

        // Set contact details in UI
        nameTV.text = contactName
        contactTV.text = contactNumber

        // Handle call button click
        callIV.setOnClickListener {
            makeCall(contactNumber)
        }

        // Handle message button click
        messageIV.setOnClickListener {
            sendMessage(contactNumber)
        }
    }

    /**
     * Opens the default messaging app with the contact number pre-filled.
     *
     * @param contactNumber The phone number to send a message to.
     */
    private fun sendMessage(contactNumber: String?) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$contactNumber"))
        intent.putExtra("sms_body", "Enter your message")
        startActivity(intent)
    }

    /**
     * Initiates a phone call to the given contact number.
     * Checks for CALL_PHONE permission before making the call.
     *
     * @param contactNumber The phone number to call.
     */
    private fun makeCall(contactNumber: String?) {
        val callIntent = Intent(Intent.ACTION_CALL)
        callIntent.data = Uri.parse("tel:$contactNumber")

        // Check if CALL_PHONE permission is granted before making the call
        if (ActivityCompat.checkSelfPermission(
                this@ContactDetailActivity,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        startActivity(callIntent)
    }
}