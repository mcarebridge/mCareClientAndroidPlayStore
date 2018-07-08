package com.phr.ade.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by deejay on 9/21/2014.
 */
public class StartMyServiceAtBootReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            Intent serviceIntent = new Intent(context, com.phr.ade.service.ClientService.class);
            context.startService(serviceIntent);
        }
    }
}
