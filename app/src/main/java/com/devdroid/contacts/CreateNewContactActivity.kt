package com.devdroid.contacts

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Activity to create a new contact and save it to the device's contact list.
 */
class CreateNewContactActivity : AppCompatActivity() {

    // UI elements
    private lateinit var nameEdt: EditText
    private lateinit var phoneEdt: EditText
    private lateinit var emailEdt: EditText
    private lateinit var addContactBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_contact)

        // Initialize UI elements
        nameEdt = findViewById(R.id.enterName)
        phoneEdt = findViewById(R.id.enterNumber)
        emailEdt = findViewById(R.id.enterEmail)
        addContactBtn = findViewById(R.id.saveButton)

        // Set click listener on the "Add Contact" button
        addContactBtn.setOnClickListener {
            val name = nameEdt.text.toString().trim()
            val phone = phoneEdt.text.toString().trim()
            val email = emailEdt.text.toString().trim()

            // Check if any field is empty before proceeding
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter data in all fields.", Toast.LENGTH_SHORT).show()
            } else {
                addContact(name, phone, email)
            }
        }
    }

    /**
     * Opens the contacts app to add a new contact with the provided details.
     *
     * @param name The contact's name.
     * @param phone The contact's phone number.
     * @param email The contact's email address.
     */
    private fun addContact(name: String, phone: String, email: String) {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION).apply {
            type = ContactsContract.RawContacts.CONTENT_TYPE
            putExtra(ContactsContract.Intents.Insert.NAME, name)
            putExtra(ContactsContract.Intents.Insert.PHONE, phone)
            putExtra(ContactsContract.Intents.Insert.EMAIL, email)
        }
        startActivityForResult(contactIntent, REQUEST_CODE_ADD_CONTACT)
    }

    /**
     * Handles the result after attempting to add a contact.
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_ADD_CONTACT) {
            when (resultCode) {
                RESULT_OK -> {
                    Toast.makeText(this, "Contact has been added.", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
                RESULT_CANCELED -> {
                    Toast.makeText(this, "Cancelled adding contact.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Navigates back to the main activity after successfully adding a contact.
     */
    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    companion object {
        private const val REQUEST_CODE_ADD_CONTACT = 1
    }
}