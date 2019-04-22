package com.karim.ater.fajralarm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class CallTimesAdapter extends RecyclerView.Adapter<CallTimesAdapter.CallTimesViewHolder> {

    // RecyclerView adapter of call times in call log detail

    private ArrayList<CallTimes> callTimes;

    CallTimesAdapter(ArrayList<CallTimes> callTimes) {
        this.callTimes = callTimes;
    }

    @NonNull
    @Override
    public CallTimesViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.call_times_item, viewGroup, false);
        return new CallTimesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CallTimesViewHolder callTimesViewHolder, int i) {
        callTimesViewHolder.callStartTimeTv.setText(callTimes.get(i).getStartTime());
        callTimesViewHolder.callEndTimeTv.setText(callTimes.get(i).getEndTime());
        callTimesViewHolder.callDurationTv.setText(callTimes.get(i).getRingingDuration());
    }

    @Override
    public int getItemCount() {
        return callTimes.size();
    }

    class CallTimesViewHolder extends RecyclerView.ViewHolder {

        private TextView callStartTimeTv;
        private TextView callEndTimeTv;
        private TextView callDurationTv;

        CallTimesViewHolder(View view) {
            super(view);
            callStartTimeTv = view.findViewById(R.id.callStartTimeTv);
            callEndTimeTv = view.findViewById(R.id.callEndTimeTv);
            callDurationTv = view.findViewById(R.id.callDurationTv);
        }
    }
}
