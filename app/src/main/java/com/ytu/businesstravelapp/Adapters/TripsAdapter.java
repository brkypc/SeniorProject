package com.ytu.businesstravelapp.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ytu.businesstravelapp.R;
import com.ytu.businesstravelapp.Classes.Trip;

import java.util.ArrayList;

public class TripsAdapter extends RecyclerView.Adapter<TripsAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final ArrayList<Trip> trips;
    private final Context context;

    public TripsAdapter(Context context, ArrayList<Trip> trips) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.trips = trips;
    }

    @NonNull
    @Override
    public TripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

            view = mInflater.inflate(R.layout.trip_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TripsAdapter.ViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.date.setText(trip.getDate());
        holder.tripTime.setText(trip.getTripTime());
        holder.distance.setText(trip.getDistance() + " km");
        holder.amount.setText(trip.getAmount() + " ₺");
        if (trip.getTaxiType().equalsIgnoreCase("1"))
            holder.taxiPhoto.setImageResource(R.drawable.yellow_taxi);
        else if(trip.getTaxiType().equalsIgnoreCase("2")){
            holder.taxiPhoto.setImageResource(R.drawable.blue_taxi);
            holder.taxiPhoto.setScaleY((float) 1.15);
        }
        else {
            holder.taxiPhoto.setImageResource(R.drawable.black_taxi);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView date, distance, tripTime, amount;
        ImageView taxiPhoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            distance = itemView.findViewById(R.id.itemDistance);
            tripTime = itemView.findViewById(R.id.tripTime);
            amount = itemView.findViewById(R.id.itemAmount);
            taxiPhoto = itemView.findViewById(R.id.taxiPhoto);

        }
    }

}
