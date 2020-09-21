package shatova.my_redial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.TelecomManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.util.Date;

import static android.content.Context.TELECOM_SERVICE;

public class CallReceiver extends CallStatusReceiver {
    Handler handler = new Handler();
    private Runnable runnable;


    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        //
    }

    public static void endCall(Context context) {
        TelecomManager telecomManager = null;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            telecomManager = (TelecomManager) context.getSystemService(TELECOM_SERVICE);

//            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED || telecomManager == null) {
//                return;
//            }


//            if (telecomManager.isInCall()) {
                try {
                    boolean callDisconnected = telecomManager.endCall();
                    if (callDisconnected) {
                        Toast.makeText(context, "Call Ended Automatically", Toast.LENGTH_SHORT).show();
                        return;

                    }
                } catch (Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }


//            }
        }
        try {
            final Class<?> telephonyClass = Class.forName("com.android.internal.telephony.ITelephony");
            final Class<?> telephonyStubClass = telephonyClass.getClasses()[0];
            final Class<?> serviceManagerClass = Class.forName("android.os.ServiceManager");
            final Class<?> serviceManagerNativeClass = Class.forName("android.os.ServiceManagerNative");
            final Method getService = serviceManagerClass.getMethod("getService", String.class);
            final Method tempInterfaceMethod = serviceManagerNativeClass.getMethod("asInterface", IBinder.class);
            final Binder tmpBinder = new Binder();
            tmpBinder.attachInterface(null, "fake");
            final Object serviceManagerObject = tempInterfaceMethod.invoke(null, tmpBinder);
            final IBinder retbinder = (IBinder) getService.invoke(serviceManagerObject, "phone");
            final Method serviceMethod = telephonyStubClass.getMethod("asInterface", IBinder.class);
            final Object telephonyObject = serviceMethod.invoke(null, retbinder);
            final Method telephonyEndCall = telephonyClass.getMethod("endCall");
            telephonyEndCall.invoke(telephonyObject);
            Toast.makeText(context, "Call Ended Automatically", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onOutgoingCallStarted(final Context ctx, String number, Date start)
    {
        
          runnable=new Runnable() {
            @Override
            public void run() {
           endCall(ctx);
            }
        };
          handler.postDelayed(runnable,15000);
        if(MainActivity.speakeron){
            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            audioManager.setMode(AudioManager.MODE_IN_CALL );
            audioManager.setSpeakerphoneOn(true);
        }else {
            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }

        //
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end)
    {
        handler.removeCallbacks(runnable);
                      LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent("possibleToMakeNextCall"));

        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        //
    }

}

