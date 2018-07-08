package com.phr.ade.db.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class CareDBHelper extends SQLiteOpenHelper implements CareClientDBConstants
{
    String TAG = "CareDBHelper";


    public CareDBHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase database)
    {
        Log.i(TAG,
                "------------------------------------------------------------------- onCreate --");
        database.execSQL(TABLE_MOBILE_SYNCH);
        database.execSQL(TABLE_CARED_ACTION_SYNC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        Log.i(TAG, "-- onUpgrade --" + "oldVersion=" + oldVersion
                + " newVersion=" + newVersion);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_SYNCH);
        database.execSQL("DROP TABLE IF EXISTS " + CARED_ACTION_SYNC);
        onCreate(database);
    }
}
