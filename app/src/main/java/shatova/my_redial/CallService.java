package shatova.my_redial;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class CallService extends Service {

    String TAG = "myLOG";


    int attempts;
    String number;
    int tryCall;

    boolean havePermission;
    private android.content.BroadcastReceiver mReceiver;
    private Handler handler;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "SERVICE on Create");



        // Initialize a new BroadcastReceiver instance for local intents from ServiceReceiver.
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // Display a notification that radio has been connected.
                Log.d(TAG, "SERVICE BROADCAST intent received");
                makeCall();
            }
        };

        // Register Local Broadcast receiver - use to receive messages from service
        IntentFilter serviceFilter = new IntentFilter("possibleToMakeNextCall");
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, serviceFilter);

    }

    /**
     * This no-op method is necessary since MusicService is a
     * so-called "Started Service".
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Hook method called every time startService() is called with an
     * Intent associated with this MusicService.
     */
    @Override
    public int onStartCommand(Intent intent,
                              int flags,
                              int startid) {


        havePermission = (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);

        Bundle extras = intent.getExtras();
        if (extras != null){
            number = intent.getStringExtra("number");
            attempts = intent.getIntExtra("attempts", 0);
            Log.d(TAG, "SRVICE intent received NUMBER  = " + number + " ATTEMPTS = " + attempts);
            tryCall = 1;
            makeCall();

        }

        //if (havePermission && tryCall == 1){

        //}



        // Restart Service if it shuts down.
        return START_STICKY;
    }

    private void makeCall(){



//        int lastCallDuration = Integer.parseInt(getCallDetails(getApplicationContext()));
//        Log.d(TAG, "Длительность последнего звонка = " + lastCallDuration);
//        if  (lastCallDuration == 0) {
//            if (tryCall <= attempts) {
        Toast.makeText(getApplicationContext(), "Making call " + tryCall + " after " + attempts+" seconds", Toast.LENGTH_SHORT).show();

        handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "ЗВОНЮ в " + tryCall + " раз из " + attempts);
                try {
                    getApplicationContext().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
//                    Toast.makeText(getApplicationContext(), "Making call " + tryCall + " of " + attempts, Toast.LENGTH_SHORT).show();
                    tryCall++;
                } catch (SecurityException sEx) {
                    Toast.makeText(getApplicationContext(), sEx.getMessage(), Toast.LENGTH_SHORT).show();

                    Log.d(TAG, "Security permission == " + sEx);
                }
            }
        },attempts*1000);

//            }
//            else {
//                Log.d(TAG, "SERVICE ПОПЫТКИ закончились");
//                onDestroy();
//            }
//        }else if (tryCall == 1) {
//            Log.d(TAG, "ЗВОНЮ в " + tryCall + " раз из " + attempts);
//            try {
//                getApplicationContext().startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + number)));
//                Toast.makeText(getApplicationContext(), "Making call " + tryCall + " of " + attempts, Toast.LENGTH_SHORT).show();
//                tryCall++;
//            } catch (SecurityException sEx) {
//                Log.d(TAG, "Security permission == " + sEx);
//            }
//        }
//        else{
//            Log.d(TAG, "SERVICE Длительность последнего звонка больше 0");
//            onDestroy();
//        }

    }

    public static String getCallDetails(Context context) {

        // StringBuilder sb = new StringBuilder();
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {


            Cursor managedCursor = context.getContentResolver().query( CallLog.Calls.CONTENT_URI, null, null, null, null);
            if (managedCursor != null) {
                //int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
                int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
                //int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
                int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
                //sb.append("Call Details :");
                if (managedCursor.moveToFirst()) {
                    //managedCursor.moveToNext();
                    managedCursor.moveToPosition(0);
                    //String phNumber = managedCursor.getString(number);
                    String callType = managedCursor.getString(type);
                    //String callDate = managedCursor.getString(date);
                    //Date callDayTime = new Date(Long.valueOf(callDate));
                    String callDuration = managedCursor.getString(duration);
                    //String dir = null;
                    int dircode = Integer.parseInt(callType);
                    switch (dircode) {
                        case CallLog.Calls.OUTGOING_TYPE:
                            Log.d("myLOG", "OUTGOING" + callDuration);
                            return callDuration;

                    }
                    managedCursor.close();
                }else{
                    Log.d("myLOG", "CURSOR not moveToFirst()");
                }
            }else{
                Log.d("myLOG", "CURSOR empty");
            }
        }

        return "0";
    }

    public void onDestroy(){
        super.onDestroy();
        if (MainActivity.stop){
            Log.d(TAG, "SERVICE onDestroy");
        handler.removeCallbacksAndMessages(null);
        // unregister BroadcastReceivers
        if (mReceiver != null){
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        stopSelf();
        }else {
            Intent intent = new Intent(getApplicationContext(), CallService.class);
            intent.putExtra("number", number);
            intent.putExtra("attempts", attempts);

            startService(intent);
            }


//
    }
}
