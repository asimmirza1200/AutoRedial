package shatova.my_redial;

import android.Manifest;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.wifi.aware.PublishConfig;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.ButtonBarLayout;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.util.Log;
import android.content.Context;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import java.util.ArrayList;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.LogRecord;
import android.os.Handler;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

public class MainActivity extends AppCompatActivity {

    public final static int MY_PERMISSIONS_REQUEST = 11;
    public static boolean stop=false;

    EditText phoneNumber;
    Button callButton;
    Button exitButton;

    String TAG = "myLOG";

    String number;
    int attempts = 0;
    EditText attemptsEditText;
    TextView counter;

    ComponentName component;




    /**
     * Use BroadcastReceiver instances for receiving intents
     */
    private BroadcastReceiver mReceiver = null;



    private boolean called;

    boolean requestAsked;
    String[] PERMISSIONS = new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE,
             Manifest.permission.READ_CALL_LOG};
    public static boolean speakeron=true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Log.d(TAG, "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
         Toolbar toolbar=findViewById(R.id.my_toolbar);
         setSupportActionBar(toolbar);
        /*if (savedInstanceState != null){
            attempts = savedInstanceState.getInt("attempts");
            call();
        }*/

        attemptsEditText = findViewById(R.id.atteptsNum);


        requestAsked = false;
        //callEnded = true;

        phoneNumber = findViewById(R.id.phone_number);
        counter = findViewById(R.id.counter);

        String lastNumber = CallLog.Calls.getLastOutgoingCall(this);
        if (!lastNumber.isEmpty()){
            phoneNumber.setText(lastNumber);
        }



        if (!hasPermission(getApplicationContext(), PERMISSIONS)) {
            Log.d(TAG, "Request Permissions");
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSIONS_REQUEST);

        }//else{
            //Log.d(TAG, "PERMISSIONS GRANTED");
        //}


        callButton = findViewById(R.id.call_button);
        callButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "CALL button pressed");


                  if (phoneNumber.getText().toString().isEmpty()){
                      phoneNumber.setError("Enter Phone Number");

                  }else if (attemptsEditText.getText().toString().isEmpty()){
                      attemptsEditText.setError("Enter Delay Time");
                  }
                  else
                   {
                       attempts = Integer.parseInt(attemptsEditText.getText().toString());
                       number = phoneNumber.getText().toString();
                      Intent intent = new Intent(getApplicationContext(), CallService.class);
                      intent.putExtra("number", phoneNumber.getText().toString());
                      intent.putExtra("attempts", attempts);

                      startService(intent);
                  }


            }
        });

        exitButton = findViewById(R.id.exit_button);
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Service Stoped", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "EXIT button pressed");
                stop=true;
//                //stop CallService
                stopService(new Intent(MainActivity.this, CallService.class));
//
//
//                //Disable
                getApplicationContext().getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
            }
        });
        ImageView pick_contact = findViewById(R.id.pick_contact);
        pick_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               requestPermission();
            }
        });

        //disable CallStatus Broadcast receiver declared in manifest
        component = new ComponentName(getApplication(), CallReceiver.class);
        int status = getApplicationContext().getPackageManager().getComponentEnabledSetting(component);
        if(status == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            Log.d(TAG, "receiver is enabled");
        } else if(status == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            Log.d(TAG, "receiver is disabled");
        }

        //Enable
        getApplicationContext().getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.menu, menu);


        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.item2:
                if (item.isChecked()) {
                    item.setChecked(false);
                    speakeron=true;
                } else {
                    speakeron=false;

                    item.setChecked(true);
//                    audioManager.setSpeakerphoneOn(false);
                }
                return true;
            case R.id.subitem1:
                attemptsEditText.setText("30");
                return true;
            case R.id.subitem2:
                attemptsEditText.setText("60");

                return true;
            case R.id.subitem3:
                attemptsEditText.setText("120");

                return true;
            case R.id.subitem4:
                attemptsEditText.setText("300");

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void requestPermission() {
//
        TedPermission.with(getApplicationContext())
                .setPermissionListener(permissionlistener)
//                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
                .check();


    }
    private final int PICK_CONTACT=1;
    PermissionListener permissionlistener = new PermissionListener() {


        @Override
        public void onPermissionGranted() {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {


        }


    };
    @Override public void onActivityResult(int reqCode, int resultCode, Intent data){ super.onActivityResult(reqCode, resultCode, data);

        if (reqCode == PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                Cursor c = managedQuery(contactData, null, null, null, null);
                if (c.moveToFirst()) {
                    String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

                    String hasPhone =
                            c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                        phones.moveToFirst();
                        String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY));

                        String cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        // Toast.makeText(getApplicationContext(), cNumber, Toast.LENGTH_SHORT).show();
//                            showSelectedNumber(1,cNumber);
                        phoneNumber.setText(cNumber);

                    }
                }
            }
        }
    }
    public boolean hasPermission(Context context, String... permissions) {
        //Log.d(TAG, "hasPermission");
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


/*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        Log.d(TAG, "SavedInstance === state ===");
        savedInstanceState.putInt("attempts", attempts);
    }*/

    @Override
    public void onResume() {
        Log.d(TAG, "=== onResume === ");
        super.onResume();

//        String lastNumber = CallLog.Calls.getLastOutgoingCall(this);
//        if (!lastNumber.isEmpty()){
//            phoneNumber.setText(lastNumber);}

        //Log.d(TAG, "LAST OUTGOING!!!!!   " + lastNumber);


    }

    @Override
    protected void onDestroy(){
        super.onDestroy();

//        //stop CallService
//        stopService(new Intent(this, CallService.class));
//
//
//        //Disable
//        getApplicationContext().getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_DISABLED , PackageManager.DONT_KILL_APP);
        //Enable
        //getApplicationContext().getPackageManager().setComponentEnabledSetting(component, PackageManager.COMPONENT_ENABLED_STATE_ENABLED , PackageManager.DONT_KILL_APP);






    }





}


