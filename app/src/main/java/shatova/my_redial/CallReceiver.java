package shatova.my_redial;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.telecom.TelecomManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.logging.LogManager;

public class CallReceiver extends CallStatusReceiver {
       Handler handler=new Handler();
    private Runnable runnable;


    @Override
    protected void onIncomingCallReceived(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, String number, Date start)
    {
        //
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end)
    {
        //
    }
    @SuppressLint("PrivateApi")
    public static boolean endCall(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            final TelecomManager telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
//            if (telecomManager != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                telecomManager.endCall();
                Toast.makeText(context, "Call Ended Automatically", Toast.LENGTH_SHORT).show();
                return true;
//            }
//            Toast.makeText(context, "Call Not Ended", Toast.LENGTH_SHORT).show();

//            return false;
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

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

interface ITelephony {

    boolean endCall();

    void answerRingingCall();

    void silenceRinger();

}