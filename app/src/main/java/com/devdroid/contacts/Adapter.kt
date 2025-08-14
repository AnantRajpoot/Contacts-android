package com.devdroid.contacts

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter class for handling the display of contacts in a RecyclerView.
// context -> The context of the calling activity.
// contactsArrayList -> List of contacts to be displayed.
class Adapter(
    private val context: Context,
    private var contactsArrayList: ArrayList<Contacts>
) : RecyclerView.Adapter<Adapter.ViewHolder>() {

    // Creates and returns a ViewHolder object
    // for each item in the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.contact_rv_item, parent, false)
        return ViewHolder(view)
    }

    // Updates the contact list with a filtered
    // list and notifies the adapter
    fun filterList(filterList: ArrayList<Contacts>) {
        contactsArrayList = filterList

        // Notify adapter about dataset change
        notifyItemRangeChanged(0, itemCount)
    }

    // Binds data to the ViewHolder for a specific position
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactsArrayList[position]
        holder.contactTV.text = contact.userName

        // Set click listener to open ContactDetailActivity
        // with selected contact details
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ContactDetailActivity::class.java).apply {
                putExtra("name", contact.userName)
                putExtra("contact", contact.contactNumber)
            }
            context.startActivity(intent)
        }
    }

    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int {
        return contactsArrayList.size
    }

    /**
     * ViewHolder class to hold and manage views
    for each RecyclerView item.
     */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val contactTV: TextView = itemView.findViewById(R.id.contactName)
    }
}