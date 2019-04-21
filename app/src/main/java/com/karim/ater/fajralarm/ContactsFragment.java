package com.karim.ater.fajralarm;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

public class ContactsFragment extends Fragment {

    View view = null;
    RecyclerView contactsRv;
    ContactsRecyclerViewAdapter contactsRecyclerViewAdapter;
    CoordinatorLayout mCoordinatorLo;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            context = getContext();
            view = inflater.inflate(R.layout.fragment_contacts, container, false);
            contactsRv = view.findViewById(R.id.contactsRv);
            mCoordinatorLo = view.findViewById(R.id.mCoordinatorLo);
            contactsRecyclerViewAdapter = new ContactsRecyclerViewAdapter(context);
            contactsRv.setAdapter(contactsRecyclerViewAdapter);
            contactsRv.setLayoutManager(new LinearLayoutManager(context));
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
                                if (viewHolder instanceof ContactsRecyclerViewAdapter.ContactViewHolder) {
                                    // get the removed item name to display it in snack bar
                                    final int contactPosition = viewHolder.getAdapterPosition();
                                    ArrayList<Contact> selectedContacts;
                                    selectedContacts = contactsRecyclerViewAdapter.selectedContacts;
                                    final Contact contact = selectedContacts.get(contactPosition);
                                    // remove contact from list
                                    selectedContacts.remove(contactPosition);
                                    DatabaseHelper databaseHelper = new DatabaseHelper(context);
                                    String contactName = contact.getContactName();
                                    String contactNumber = contact.getContactNumber();
                                    // remove contact from database
                                    databaseHelper.deleteContact(contactName, contactNumber);
                                    contactsRv.getAdapter().notifyItemRemoved(contactPosition);
                                    Utils.stopAlarms(context, contactNumber, contact.getContactId());

                                    // showing snack bar with Undo option

                                    Snackbar snackbar = Snackbar
                                            .make(getActivity().findViewById(R.id.mCoordinatorLo),
                                                    contactName + " " + context.getResources().getString(R.string.DeleteContactAction),
                                                    Snackbar.LENGTH_LONG);
                                    snackbar.setAction(R.string.stopAlarmButtonText, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            // undo is selected, restore the deleted item
                                            ((ContactsRecyclerViewAdapter) contactsRv.getAdapter()).
                                                    restoreContact(contact, contactPosition);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.YELLOW);
                                    snackbar.show();
                                }
                            }
                        });
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(contactsRv);
    }

    // change position functionality
    private void dragAndDrop() {

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                        ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                ArrayList<Contact> selectedContacts;
                selectedContacts = contactsRecyclerViewAdapter.selectedContacts;
                Collections.swap(selectedContacts, viewHolder.getAdapterPosition(), target.getAdapterPosition());
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.reorder(selectedContacts);
                contactsRv.getAdapter().notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

            }
        });
        itemTouchHelper.attachToRecyclerView(contactsRv);
    }

}
