package shatova.my_redial;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.v4.content.LocalBroadcastManager;

import java.util.Date;

public class CallReceiver extends CallStatusReceiver {

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

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start)
    {
        if(MainActivity.speakeron){
            final AudioManager audioManager = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);

            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION );
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
                      LocalBroadcastManager.getInstance(ctx).sendBroadcast(new Intent("possibleToMakeNextCall"));

        //
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start)
    {
        //
    }

}
