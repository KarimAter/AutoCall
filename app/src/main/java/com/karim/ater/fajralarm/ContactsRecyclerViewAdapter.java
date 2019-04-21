package com.karim.ater.fajralarm;

import android.content.Context;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;


public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ContactViewHolder> {
    ArrayList<Contact> selectedContacts;
    Context context;
    ContactViewHolder contactViewHolder;

    ContactsRecyclerViewAdapter(Context context) {
//        this.selectedContacts = selectedContacts;
        this.context = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.selectedContacts = databaseHelper.loadContacts();

    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);
        contactViewHolder = new ContactViewHolder(view);
        return contactViewHolder;
    }

    @Override
    public void onBindViewHolder(ContactViewHolder holder, final int position) {
        holder.contact_Name.setText(selectedContacts.get(position).getContactName());
        holder.contact_Number.setText(selectedContacts.get(position).getContactNumber());
        holder.contact_Call_Time.setText(selectedContacts.get(position).getContactCallTime());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    @Override
    public int getItemCount() {
        return selectedContacts.size();
    }


    void restoreContact(Contact contact, int position) {
        selectedContacts.add(position, contact);
        // notify item added by position
        notifyItemInserted(position);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.reorder(selectedContacts);
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView contact_Name;
        final TextView contact_Number;
        final TextView contact_Call_Time;

        ContactViewHolder(View view) {
            super(view);
            mView = view;
            contact_Name = view.findViewById(R.id.contact_Name);
            contact_Number = view.findViewById(R.id.contact_Number);
            contact_Call_Time = view.findViewById(R.id.contact_Call_Time);
        }

    }
}
