package com.karim.ater.fajralarm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.LogViewHolder> {
    // Adapter of Logs RecyclerView
    private Context context;
    private ArrayList<Contact> contacts;

    LogsAdapter(Context context) {
        this.context = context;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.contacts = databaseHelper.loadContacts();
    }

    @NonNull
    @Override
    public LogViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.log_item, viewGroup, false);
        return new LogViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LogViewHolder logViewHolder, int i) {
        Contact contact = contacts.get(i);
        CallLogDetails callLogDetails = Utils.extractCallDetail(contact.getContactLog());
        logViewHolder.logContactNameTv.setText(contact.getContactName());
        logViewHolder.finalStatusTv.setText(callLogDetails.getFinalStatus());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        logViewHolder.callTimesRv.setLayoutManager(linearLayoutManager);
        logViewHolder.callTimesRv.setHasFixedSize(true);
        logViewHolder.callTimesRv.setAdapter(new CallTimesAdapter(callLogDetails.getCallTimes()));

    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    class LogViewHolder extends RecyclerView.ViewHolder {

        private TextView logContactNameTv;
        private TextView finalStatusTv;
        private RecyclerView callTimesRv;

        LogViewHolder(View view) {
            super(view);
            logContactNameTv = view.findViewById(R.id.logContactNameTv);
            finalStatusTv = view.findViewById(R.id.finalStatusTv);
            callTimesRv = view.findViewById(R.id.callTimesRv);
        }
    }
    // Todo: Dividers
}
