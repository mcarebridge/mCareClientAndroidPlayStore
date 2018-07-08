package com.phr.ade.activity;


/******************************************************************************
 * Copyright mCareBridge 2017-18
 * This class sets the Alarm for Application to rise and connect with
 * mCareBridge server for Synch up
 *****************************************************************************/

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phr.ade.service.MCareHttpClientAsyncTask;
import com.phr.ade.service.McareOnTaskDoneListenerImpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class AlarmReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {

        Log.d("AlarmReceiver", "-- starting onReceive Method --");

        McareOnTaskDoneListenerImpl _mCareOnTask = new McareOnTaskDoneListenerImpl(context);
        MCareHttpClientAsyncTask _httpClientTask = new MCareHttpClientAsyncTask(context, _mCareOnTask, true);
        _httpClientTask.execute();
    }

    public void setAlarm(Context context)
    {
        Log.d("AlarmReceiver", "-- starting setAlarm Method --");
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 15, pi); // Millisec * Second * Minutes
        //To start at the begining of the Hours find the time and nearst time to 30 mins. This way alarm can
        // start after every start of hr or nearest 15 mins

        Calendar _c = Calendar.getInstance();
        _c.add(Calendar.HOUR, 1);
        _c.set(Calendar.MINUTE, 0);
        _c.set(Calendar.SECOND, 0);

        long _startTime = _c.getTimeInMillis();

        SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-mm-dd hh:mm:00 a");
        Log.d("AlarmReceiver ----> the alarm will run at  ", _sdf.format(new Date(_startTime)));

        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, _startTime, 1000 * 60 * 60, pi); // set for 60 mins interval.
    }

    public void cancelAlarm(Context context)
    {
        Log.d("AlarmReceiver", "-- starting cancelAlarm Method --");

        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
