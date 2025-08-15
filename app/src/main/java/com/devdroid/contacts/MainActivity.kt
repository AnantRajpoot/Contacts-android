package com.devdroid.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.Locale
import java.util.Locale.filter

class MainActivity : AppCompatActivity() , SearchView.OnQueryTextListener{

    //A display list that you give to the adapter. This is the list you will clear and repopulate with filtered results.
    private var displayList: ArrayList<Contacts> = ArrayList()
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

        // Search View---
        val searchView: SearchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(this)

        prepareContactRV()

        if (arePermissionsAlreadyGranted()) {
            // If permissions are already granted, get contacts directly without the toast.
            getContacts()
        } else {
            // If permissions are NOT granted, call your original request function.
            requestPermissions()
        }

        // Handle click event on FloatingActionButton to open CreateNewContactActivity
        addNewContactFAB.setOnClickListener {
            startActivity(Intent(this, CreateNewContactActivity::class.java))
        }
    }
    // 3. ADD THIS: Implement the filtering logic
    private fun filter(text: String) {
        // 1. Clear the display list
        displayList.clear()

        // 2. Check if the search text is empty
        if (text.isEmpty()) {
            // If it's empty, show all original contacts
            displayList.addAll(contactsArrayList)
        } else {
            // 3. Loop through the untouchable master list
            for (item in contactsArrayList) {
                // BONUS FIX: Add a null check for the name to prevent another common crash
                if (item.userName != null && item.userName.lowercase(Locale.getDefault()).contains(text.lowercase(Locale.getDefault()))) {
                    // 4. Add matching items to the display list
                    displayList.add(item)
                }
            }
        }

        // 5. Notify the adapter of the changes
        adapter.notifyDataSetChanged()
    }

    // Setup RecyclerView and request necessary permissions----------
    // This is the MODIFIED code

    // Initialize RecyclerView with adapter and layout manager
    private fun prepareContactRV() {
//        adapter = Adapter(this, contactsArrayList)
//        contactRV.layoutManager = LinearLayoutManager(this)
//        contactRV.adapter = adapter

        // CHANGE 2: Initialize the adapter with the 'displayList'.
        adapter = Adapter(this, displayList)
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
    // ADD THIS HELPER FUNCTION
    private fun arePermissionsAlreadyGranted(): Boolean {
        val permissions = listOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.WRITE_CONTACTS
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
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
    // Replace your old getContacts function with this one
    private fun getContacts() {
        // Clear the list BEFORE the loop to avoid duplicates if this function is called again.
        contactsArrayList.clear()

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val displayName = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
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
                            // The loop correctly populates contactsArrayList here
                            contactsArrayList.add(Contacts(displayName, phoneNumber))
                        }
                    }
                }
            }
        } // The cursor?.use block ends here

        // --- THIS IS THE CORRECTED LOGIC ---
        // 1. Now that contactsArrayList is full, clear the displayList.
        displayList.clear()
        // 2. Copy all the freshly loaded contacts into the displayList.
        displayList.addAll(contactsArrayList)
        // 3. Notify the adapter that the data is ready.
        adapter.notifyDataSetChanged()
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        // This method is called when the user presses the search button
        // You can choose to do nothing here or call the filter method
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        // This method is called every time the user types a character
        if (newText != null) {
            filter(newText)
        }
        return true
    }
}