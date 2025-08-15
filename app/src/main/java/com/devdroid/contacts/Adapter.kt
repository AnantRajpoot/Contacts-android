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

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_CONTACT = 1
class Adapter(
    private val context: Context,
    private var listItems: ArrayList<ListItem> // Use the ListItem sealed class
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }
    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val contactInitial: TextView = view.findViewById(R.id.contactInitial)
        val contactName: TextView = view.findViewById(R.id.contactName)
    }
    override fun getItemViewType(position: Int): Int {
        return when (listItems[position]) {
            is ListItem.HeaderItem -> VIEW_TYPE_HEADER
            is ListItem.ContactItem -> VIEW_TYPE_CONTACT
        }
    }

    // Creates and returns a ViewHolder object
    // for each item in the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.header_item, parent, false)
                HeaderViewHolder(view)
            }
            VIEW_TYPE_CONTACT -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.contact_rv_item, parent, false)
                ContactViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = listItems[position]) {
            is ListItem.HeaderItem -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.headerTitle.text = item.letter
            }
            is ListItem.ContactItem -> {
                val contactHolder = holder as ContactViewHolder
                val contact = item.contact

                contactHolder.contactName.text = contact.userName
                contactHolder.contactInitial.text = contact.userName.substring(0, 1).uppercase()

                contactHolder.itemView.setOnClickListener {
                    val intent = Intent(context, ContactDetailActivity::class.java).apply {
                        putExtra("name", contact.userName)
                        putExtra("contact", contact.contactNumber) // Assuming your Contacts class has this property
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    // Updates the contact list with a filtered
    // list and notifies the adapter
//    fun filterList(filterList: ArrayList<Contacts>) {
//        contactsArrayList = filterList
//
//        // Notify adapter about dataset change
//        notifyItemRangeChanged(0, itemCount)
//    }



    /**
     * Returns the total number of items in the list.
     */
    override fun getItemCount(): Int {
        return listItems.size
    }

    /**
     * ViewHolder class to hold and manage views
    for each RecyclerView item.
     */

    fun updateList(newList: ArrayList<ListItem>) {
        listItems = newList
        notifyDataSetChanged()
    }
}