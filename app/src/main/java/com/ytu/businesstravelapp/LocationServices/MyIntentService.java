package com.ytu.businesstravelapp.LocationServices;

import static com.ytu.businesstravelapp.Fragments.MapFragment.MESSENGER_INTENT_KEY;
import static com.ytu.businesstravelapp.Fragments.MapFragment.MESSENGER_INTENT_KEY2;

import android.app.IntentService;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class MyIntentService extends IntentService implements LocationUpdatesComponent.ILocationProvider {
    private static final String TAG = "ytuLog";
    public static final int LOCATION_MESSAGE = 999;
    public static final int LOCATION_MESSAGE2 = 998;

    private LocationUpdatesComponent locationUpdatesComponent;
    private Messenger mActivityMessenger, mActivityMessenger2;

    public MyIntentService() {
        super("MyIntentService");
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate ");

        locationUpdatesComponent = new LocationUpdatesComponent(this);
        locationUpdatesComponent.onCreate(this);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand Service started....");
        if (intent != null) {
            mActivityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY);
            mActivityMessenger2 = intent.getParcelableExtra(MESSENGER_INTENT_KEY2);
        }

        locationUpdatesComponent.onStart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy...");

        sendMessage(LOCATION_MESSAGE, null);
        sendMessage(LOCATION_MESSAGE2, null);

        locationUpdatesComponent.onStop();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i(TAG, "onHandleIntent" + intent);
        if (intent != null) {
            final String action = intent.getAction();
            Log.i(TAG, "action" + action);
        }
    }


    private void sendMessage(int messageID, Location location) {
        if (mActivityMessenger == null) {
            return;
        }

        Message m = Message.obtain();
        m.what = messageID;
        if(location == null ) { m.obj = "finished"; }
        else { m.obj = location; }
        try {
            if (messageID == LOCATION_MESSAGE)
                mActivityMessenger.send(m);
            else
                mActivityMessenger2.send(m);
        } catch (RemoteException e) {
            Log.e(TAG, "Error passing service object back to activity.");
        }
    }

    @Override
    public void onLocationUpdate(Location location) {
        sendMessage(LOCATION_MESSAGE, location);
        sendMessage(LOCATION_MESSAGE2, location);
    }
}