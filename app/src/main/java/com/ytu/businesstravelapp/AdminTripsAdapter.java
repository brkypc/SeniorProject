package com.ytu.businesstravelapp;

import static com.ytu.businesstravelapp.MainActivity.firebaseURL;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdminTripsAdapter extends RecyclerView.Adapter<AdminTripsAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final ArrayList<Trip> trips;
    private final Context context;
    private final FirebaseDatabase database;

    public AdminTripsAdapter(Context context, ArrayList<Trip> trips) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.trips = trips;
        database = FirebaseDatabase.getInstance(firebaseURL);
    }

    @NonNull
    @Override
    public AdminTripsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = mInflater.inflate(R.layout.admin_trip_item, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull AdminTripsAdapter.ViewHolder holder, int position) {
        Trip trip = trips.get(position);

        holder.nameSurname.setText(trip.getNameSurname());
        holder.billPrice.setText(trip.getBillPrice() + "₺");
        holder.calculatedPrice.setText(trip.getAmount() + "₺");
        if (trip.getStatus().equalsIgnoreCase("yes")) {
            holder.status.setText("Onaylandı");
            holder.status.setTextColor(Color.GREEN);
            holder.statusIcon.setImageResource(R.drawable.ic_approved);
        } else {
            holder.status.setText("Onaylanmadı");
            holder.status.setTextColor(Color.RED);
            holder.statusIcon.setImageResource(R.drawable.ic_not_approved);
        }

        holder.itemView.setOnClickListener(view -> showMyDialog(trip));

    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameSurname, billPrice, calculatedPrice, status;
        ImageView statusIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameSurname = itemView.findViewById(R.id.nameSurnameItem);
            billPrice = itemView.findViewById(R.id.billPriceItem);
            calculatedPrice = itemView.findViewById(R.id.calculatedPriceItem);
            status = itemView.findViewById(R.id.billStatusItem);
            statusIcon = itemView.findViewById(R.id.billStatusIcon);
        }
    }


    @SuppressLint("SetTextI18n")
    public void showMyDialog(Trip trip) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.trip_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        AppCompatButton approve = dialog.findViewById(R.id.approve);
        AppCompatButton refuse = dialog.findViewById(R.id.refuse);
        TextView nameSurname = dialog.findViewById(R.id.nameSurnameDialog);
        TextView date = dialog.findViewById(R.id.dateDialog);
        TextView distance = dialog.findViewById(R.id.distanceDialog);
        TextView tripTime = dialog.findViewById(R.id.tripTimeDialog);
        TextView billPrice = dialog.findViewById(R.id.billPriceDialog);
        TextView calculatedPrice = dialog.findViewById(R.id.calculatedPriceDialog);
        ImageView taxiPhoto = dialog.findViewById(R.id.taxiPhotoDialog);

        nameSurname.setText(trip.getNameSurname());
        date.setText(trip.getDate());
        distance.setText("Gidilen Mesafe:             " + trip.getDistance() + " km");
        tripTime.setText("Seyahat Süresi:             " + trip.getTripTime());
        billPrice.setText("Fiş Tutarı:                        " + trip.getBillPrice() + "₺");
        calculatedPrice.setText("Hesaplanan Tutar:        " + trip.getAmount() + "₺");
        if (trip.getTaxiType().equals("1")) {
            taxiPhoto.setImageResource(R.drawable.yellow_taxi);
        } else if (trip.getTaxiType().equals("2")) {
            taxiPhoto.setImageResource(R.drawable.blue_taxi);
        } else {
            taxiPhoto.setImageResource(R.drawable.black_taxi);
        }

        approve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trip.getStatus().equals("no")) {
                    DatabaseReference myRef = database.getReference("trips/" + trip.getId());
                    myRef.child("status").setValue("yes");
                    Toast.makeText(context, "Fatura onaylandı", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Fatura önceden onaylandı", Toast.LENGTH_SHORT).show();

                }
                dialog.dismiss();
            }
        });

        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trip.getStatus().equals("yes")) {
                    DatabaseReference myRef = database.getReference("trips/" + trip.getId());
                    myRef.child("status").setValue("no");
                    Toast.makeText(context, "Fatura reddedildi", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(context, "Fatura önceden reddedildi", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
