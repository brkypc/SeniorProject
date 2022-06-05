package com.ytu.businesstravelapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ytu.businesstravelapp.Fragments.MapFragment;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class TaxiAdapter extends RecyclerView.Adapter<TaxiAdapter.ViewHolder> {
    private final LayoutInflater mInflater;
    private final ArrayList<Taxi> taxis;
    private final Context context;
    private static ArrayList<Location> locations;
    public static final String MESSENGER_INTENT_KEY = "msg-intent-key";
    private IncomingMessageHandler mHandler;

    public TaxiAdapter(Context context, ArrayList<Taxi> taxis) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.taxis = taxis;
        locations = new ArrayList<>();
        mHandler = new IncomingMessageHandler();
    }

    @NonNull
    @Override
    public TaxiAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        view = mInflater.inflate(R.layout.taxi_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaxiAdapter.ViewHolder holder, int position) {
        Taxi taxi = taxis.get(position);

        if(taxi.getType().equalsIgnoreCase("1")) {
            holder.taxiText.setText("Sarı Taksi");
            holder.taxi.setImageResource(R.drawable.yellow_taxi);
        }
        else if(taxi.getType().equalsIgnoreCase("2")) {
            holder.taxiText.setText("Turkuaz Taksi");
            holder.taxi.setImageResource(R.drawable.blue_taxi);
            holder.taxi.setScaleY((float) 1.15);
        }
        else {
            holder.taxiText.setText("Siyah Taksi");
            holder.taxi.setImageResource(R.drawable.black_taxi);
            holder.taxi.setScaleX((float) 1.15);
            holder.next.setVisibility(View.INVISIBLE);
        }

        holder.startPrice.setText("Açılış Ücreti: " +taxi.getStartPrice() + "₺");
        holder.kmText.setText(taxi.getKmPrice() + "₺/km");

        holder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!holder.startBtn.isChecked()) {
                    context.stopService(new Intent(context, MyIntentService.class));
                    Log.d("test", "stopped service" );
                    for (Location l:locations) {
                        Log.d("test", l.getLatitude() + " long:" + l.getLongitude());
                    }
                    float result = locations.get(0).distanceTo(locations.get(locations.size()-1));
                    Log.d("test", String.valueOf(result/1000));
                    Log.d("test", String.valueOf(result));

                    @SuppressLint("DefaultLocale")
                    String s = String.format("%.1f", result/1000);
                    s = s.replace(',', '.');
                    float calculatedReceipt = (float) (Float.parseFloat(s)*(Float.parseFloat(taxi.getKmPrice())) + Float.parseFloat(taxi.getStartPrice()));
                    if (calculatedReceipt<Float.parseFloat(taxi.getMinPrice())) calculatedReceipt= Float.parseFloat(taxi.getMinPrice());
                    Log.d("test",calculatedReceipt + "");


                    Intent photo = new Intent(context, PhotoActivity.class);
                    photo.putExtra("amount", String.valueOf(calculatedReceipt));
                    photo.putExtra("km", s);
                    context.startActivity(photo);

                }
                else {
                    Intent startServiceIntent = new Intent(context, MyIntentService.class);
                    Messenger messengerIncoming = new Messenger(mHandler);
                    startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming);
                    context.startService(startServiceIntent);
                    Log.d("test", "started service");
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return taxis.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView taxiText, startPrice, kmText;
        ToggleButton startBtn;
        ImageView taxi,next;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taxiText = itemView.findViewById(R.id.taxiText);
            startPrice = itemView.findViewById(R.id.startPrice);
            kmText = itemView.findViewById(R.id.kmText);
            startBtn = itemView.findViewById(R.id.btnStart);
            taxi = itemView.findViewById(R.id.taxi);
            next = itemView.findViewById(R.id.nextBtn);
        }
    }

    public static class IncomingMessageHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d("test", "handleMessage..." + msg.toString());

            super.handleMessage(msg);

            if (msg.what == MyIntentService.LOCATION_MESSAGE) {

                Location obj = (Location) msg.obj;
                locations.add(obj);
                float[] results = new float[10];
                android.location.Location.distanceBetween(locations.get(0).getLatitude(), locations.get(0).getLongitude(),
                        locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude(), results);

                for (float f : results) {
                    Log.d("test", String.valueOf(f));
                }
                String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
                //locationMsg.setText("LAT :  " + obj.getLatitude() + "\nLNG : " + obj.getLongitude() + "\n\n" + obj.toString() + " \n\n\nLast updated- " + currentDateTimeString);
            }
        }
    }
}
