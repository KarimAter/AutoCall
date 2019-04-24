package com.karim.ater.fajralarm;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LogsFragment extends Fragment {
    // Logs Fragment class
    private View view;
    private Context context;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getContext();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            DatabaseHelper databaseHelper = new DatabaseHelper(context);
            boolean emptyLogs = databaseHelper.isLogExisting();
            if (emptyLogs)
                view = inflater.inflate(R.layout.empty_log_fragment, container, false);
            else {
                view = inflater.inflate(R.layout.fragment_logs, container, false);
                RecyclerView logsRv = view.findViewById(R.id.logsRv);
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                logsRv.setLayoutManager(linearLayoutManager);
                logsRv.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
                logsRv.setAdapter(new LogsAdapter(context));
            }
        }
        return view;
    }
}
