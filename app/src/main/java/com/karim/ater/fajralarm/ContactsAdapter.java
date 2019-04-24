package com.karim.ater.fajralarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {
    // Adapter of contacts RecyclerView
    private ArrayList<Contact> contacts;
    private Context context;

    // Constructor
    ContactsAdapter(Context context, ArrayList<Contact> contacts) {
        this.context = context;
        this.contacts = contacts;
    }

    // Getter
    ArrayList<Contact> getContacts() {
        return contacts;
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_contact, parent, false);

        return new ContactViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, final int position) {
        holder.contact_Name.setText(contacts.get(position).getContactName());
        holder.contact_Number.setText(Utils.arTranslate(context, contacts.get(position).getContactNumber()));
        holder.contact_Call_Time.setText(Utils.arTranslate(context, contacts.get(position).getContactCallTime()));
    }


    @Override
    public int getItemCount() {
        return contacts.size();
    }

    // Undo deleting contact method
    void restoreContact(Contact contact, int position) {
        contacts.add(position, contact);
        // notify item added by position
        notifyItemInserted(position);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.reorder(contacts);
    }

    // Undo deleting contact method
    void deleteContact(String name, String number) {
//        contacts.remove(pos);
//        // notify item added by position
//        notifyItemRemoved(pos);
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        databaseHelper.deleteContact(name, number);
        notifyDataSetChanged();
    }

    // RecyclerView viewholder class
    class ContactViewHolder extends RecyclerView.ViewHolder {
        private TextView contact_Name;
        private TextView contact_Number;
        private TextView contact_Call_Time;
        View mView;

        ContactViewHolder(View view) {
            super(view);
            contact_Name = view.findViewById(R.id.contact_Name);
            contact_Number = view.findViewById(R.id.contact_Number);
            contact_Call_Time = view.findViewById(R.id.contact_Call_Time);
            mView = view;
        }

    }
}
