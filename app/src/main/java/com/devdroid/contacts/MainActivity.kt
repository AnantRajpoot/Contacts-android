package com.devdroid.contacts

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MainActivity : AppCompatActivity() {
    // List to store contacts
    private var contactsArrayList: ArrayList<Contacts> = ArrayList()

    // RecyclerView for displaying contacts
    private lateinit var contactRV: RecyclerView

    // Adapter for RecyclerView
    private lateinit var adapter: Adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        contactRV = findViewById(R.id.rv)

        // Floating Action Button to add new contact
        val addNewContactFAB: FloatingActionButton = findViewById(R.id.addButton)

        // Setup RecyclerView and request necessary permissions
        prepareContactRV()
        requestPermissions()

        // Handle click event on FloatingActionButton to open CreateNewContactActivity
        addNewContactFAB.setOnClickListener {
            startActivity(Intent(this, CreateNewContactActivity::class.java))
        }
    }

    // Initialize RecyclerView with adapter and layout manager
    private fun prepareContactRV() {
        adapter = Adapter(this, contactsArrayList)
        contactRV.layoutManager = LinearLayoutManager(this)
        contactRV.adapter = adapter
    }

    // Request necessary permissions using Dexter
    private fun requestPermissions() {
        Dexter.withActivity(this)
            .withPermissions(
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.WRITE_CONTACTS
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Toast.makeText(this@MainActivity, "All permissions granted", Toast.LENGTH_SHORT).show()
                        getContacts()
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                        showSettingsDialog()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>, token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .withErrorListener {
                Toast.makeText(applicationContext, "Error occurred!", Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
    }

    // Show a dialog directing the user to app settings if permissions are denied permanently
    private fun showSettingsDialog() {
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Need Permissions")
            .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
            .setPositiveButton("GOTO SETTINGS") { dialog, _ ->
                dialog.cancel()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivityForResult(intent, 101)
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    // Fetch contacts from the device's contacts list
    private fun getContacts() {
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                // Check if the contact has a phone number
                val hasPhoneNumber = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))
                if (hasPhoneNumber > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(contactId),
                        null
                    )

                    phoneCursor?.use { pc ->
                        if (pc.moveToNext()) {
                            val phoneNumber = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            contactsArrayList.add(Contacts(displayName, phoneNumber))
                        }
                    }
                }
            }
        }

        // Notify the adapter about dataset changes
        adapter.notifyItemRangeChanged(0, contactsArrayList.size)
    }
}