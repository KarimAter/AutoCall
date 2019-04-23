package com.karim.ater.fajralarm;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;

public class ContactsFragment extends Fragment implements Refresher {
    // Contacts Fragment class
    private View view = null;
    RecyclerView contactsRv;
    ContactsAdapter contactsAdapter;
    private Activity activity;
    ArrayList<Contact> contacts;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_contacts, container, false);
            contactsRv = view.findViewById(R.id.contactsRv);
            DatabaseHelper databaseHelper = new DatabaseHelper(activity);
            contacts = databaseHelper.loadContacts();
            contactsAdapter = new ContactsAdapter(activity, contacts);
            contactsRv.setAdapter(contactsAdapter);
            contactsRv.setLayoutManager(new LinearLayoutManager(activity));
            dragAndDrop();
            swipeToDelete();
        }

        return view;
    }

    // swiping to delete functionality
    private void swipeToDelete() {
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT,
                        new RecyclerItemTouchHelper.RecyclerItemTouchHelperListener() {
                            @Override
                            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                                if (viewHolder instanceof ContactsAdapter.ContactViewHolder) {
                                    // get the removed item name to display it in snack bar
                                    final int contactPosition = viewHolder.getAdapterPosition();
                                    ArrayList<Contact> contacts;
                                    contacts = contactsAdapter.getContacts();
                                    final Contact contact = contacts.get(contactPosition);
                                    // remove contact from list
                                    contacts.remove(contactPosition);

                                    // remove contact from database
                                    DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                                    String contactName = contact.getContactName();
                                    String contactNumber = contact.getContactNumber();

                                    databaseHelper.deleteContact(contactName, contactNumber);

                                    // notify list with deletion
                                    contactsAdapter.notifyItemRemoved(contactPosition);

                                    //Stop deleted contact alarm is deletion is performed after setting an alarm
                                    Utils.stopAlarms(activity, contactNumber, contact.getContactId());

                                    // showing snack bar with Undo option


                                    String snackMsg = String.format(" %s " + getString(R.string.DeleteContactAction), contactName);
                                    Snackbar snackbar = Snackbar
                                            .make(activity.findViewById(R.id.mCoordinatorLo),
                                                    snackMsg,
                                                    Snackbar.LENGTH_LONG);
                                    snackbar.getView().setTextDirection(View.TEXT_DIRECTION_LOCALE);

                                    snackbar.setAction(getString(R.string.UndoAction), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // undo is selected, restore the deleted item
                                            contactsAdapter.restoreContact(contact, contactPosition);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.YELLOW);
                                    snackbar.show();
                                }
                            }
                        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(contactsRv);
    }

    // reordering functionality
    private void dragAndDrop() {
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                ArrayList<Contact> contacts = contactsAdapter.getContacts();
                // Reordering contacts in main list and in database
                Collections.swap(contacts, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                DatabaseHelper databaseHelper = new DatabaseHelper(activity);
                databaseHelper.reorder(contacts);
                contactsAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }
        });
        itemTouchHelper.attachToRecyclerView(contactsRv);
    }

    @Override
    public void refresh(String name, String number) {
        contacts = new DatabaseHelper(activity).loadContacts();
        contactsAdapter = new ContactsAdapter(activity, contacts);
        contactsAdapter.deleteContact(name, number);
        contactsAdapter.notifyDataSetChanged();
        contactsRv.setAdapter(contactsAdapter);

    }
}
