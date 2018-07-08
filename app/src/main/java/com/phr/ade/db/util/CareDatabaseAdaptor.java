package com.phr.ade.db.util;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.phr.ade.db.vo.AuthSynch;
import com.phr.ade.db.vo.CaredActionSynch;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CareDatabaseAdaptor implements CareClientDBConstants
{

    private static final String TAG = "CareDatabaseAdaptor";
    private Context context;
    private SQLiteDatabase database;
    private CareDBHelper dbHelper;

    public CareDatabaseAdaptor(Context context)
    {
        this.context = context;
    }

    public CareDatabaseAdaptor open() throws SQLException
    {
        dbHelper = new CareDBHelper(context);
        database = dbHelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbHelper.close();
    }


    /**
     * Add a row in Table AuthSync
     *
     * @param authSynch
     * @return
     * @throws Exception
     */
    public long insertMobileSynchedRow(AuthSynch authSynch) throws Exception
    {
        long i = 9999;

        ContentValues initalValues = setMobileSynchContentValues(authSynch);
        i = database.insert(TABLE_AUTH_SYNC, null, initalValues);

        Log.d(TAG, "-- insertMobileSynchedRow rows added = " + i);
        return i;
    }


    /**
     * Update a row in Table AuthSync
     *
     * @param authSynch
     * @return
     * @throws Exception
     */
    public long updateMobileSynchedRow(AuthSynch authSynch) throws Exception
    {
        long i = 9999;

        ContentValues updateValues = setMobileSynchContentValues(authSynch);
        database.update(TABLE_AUTH_SYNC, updateValues, KEY_ROWID + " = ?",
                new String[]{authSynch.getId().toString()});
        Log.d(TAG, "-- updateMobileSynchedRow rows added = " + i);
        return i;
    }


    /**
     * @param careSync
     * @return
     * @throws Exception
     */
    public long updateCareSynchedRow(CaredActionSynch careSync) throws Exception
    {
        long i = 9999;

        ContentValues updateValues = setCaredActionSynchContentValues(careSync);
        database.update(CARED_ACTION_SYNC, updateValues, KEY_ROWID + " = ?",
                new String[]{careSync.getId().toString()});
        Log.d(TAG, "-- updateCareSynchedRow rows added = " + i);
        return i;
    }


    /**
     * @return
     * @throws Exception
     */
    public AuthSynch readMobileAuthRecord() throws Exception
    {
        Cursor _cursor = null;
        AuthSynch authSynch = null;

        _cursor = database.query(TABLE_AUTH_SYNC, new String[]{
                        KEY_ROWID,
                        IMEI, STATUS, STATE, RXCONSUMED, CAREDPAYLOAD, CREATEDATE
                }, STATE + "=?",
                new String[]{"CURRENT"}, null, null, null, null);

        if (_cursor != null)
        {
            _cursor.moveToFirst();
            while (_cursor.isAfterLast() == false)
            {
                authSynch = new AuthSynch();
                authSynch.setId(_cursor.getInt(0));
                authSynch.setIMEI(_cursor.getString(1));
                authSynch.setStatus(_cursor.getString(2));
                authSynch.setState(_cursor.getString(3));
                authSynch.setRxConsumed(_cursor.getString(4));
                authSynch.setCarePayLoad(_cursor.getString(5));
                authSynch.setCreateDate(_cursor.getString(6));
                _cursor.moveToNext();
            }

            _cursor.close();
        }
        return authSynch;
    }


    /**
     * @return
     * @throws Exception
     */
    public CaredActionSynch readCareSyncRecord() throws Exception
    {
        Cursor _cursor = null;
        CaredActionSynch _careSync = null;

        _cursor = database.query(CARED_ACTION_SYNC, new String[]{
                        KEY_ROWID,
                        IMEI, STATUS, STATE, CAREDRESPONSE, CREATEDATE
                }, STATUS + "=?",
                new String[]{"CURRENT"}, null, null, null, null);

        if (_cursor != null)
        {
            _cursor.moveToFirst();
            while (_cursor.isAfterLast() == false)
            {
                _careSync = new CaredActionSynch();
                _careSync.setId(_cursor.getInt(0));
                _careSync.setIMEI(_cursor.getString(1));
                _careSync.setStatus(_cursor.getString(2));
                _careSync.setState(_cursor.getString(3));
                _careSync.setCaredResponse(_cursor.getString(4));
                _careSync.setCreateDate(_cursor.getString(5));
                _cursor.moveToNext();
            }
            _cursor.close();
        }
        return _careSync;
    }


    /**
     * @return
     * @throws Exception
     */
    public ArrayList<CaredActionSynch> readCareSyncRecordInOpenState() throws Exception
    {
        Cursor _cursor = null;
        CaredActionSynch _careSync = null;
        ArrayList<CaredActionSynch> _careList = new ArrayList<CaredActionSynch>();

        _cursor = database.query(CARED_ACTION_SYNC, new String[]{
                        KEY_ROWID,
                        IMEI, STATUS, STATE, CAREDRESPONSE, CREATEDATE
                }, STATE + "=?",
                new String[]{"OPEN"}, null, null, null, null);

        if (_cursor != null)
        {
            _cursor.moveToFirst();
            while (_cursor.isAfterLast() == false)
            {
                _careSync = new CaredActionSynch();
                _careSync.setId(_cursor.getInt(0));
                _careSync.setIMEI(_cursor.getString(1));
                _careSync.setStatus(_cursor.getString(2));
                _careSync.setState(_cursor.getString(3));
                _careSync.setCaredResponse(_cursor.getString(4));
                _careSync.setCreateDate(_cursor.getString(5));

                _careList.add(_careSync);
                _cursor.moveToNext();
            }
            _cursor.close();
        }
        return _careList;
    }


    /**
     * Add a new row in CaredAction Table
     *
     * @param caredActionSynch
     * @return
     * @throws Exception
     */
    public long insertCaredActionSynchedRow(CaredActionSynch caredActionSynch) throws Exception
    {
        long i = 9999;

        ContentValues initialValues = setCaredActionSynchContentValues(caredActionSynch);
        i = database.insert(CARED_ACTION_SYNC, null, initialValues);
        Log.d(TAG, "-- insert caredActionSync Row rows added = " + i);
        return i;
    }


    /**
     * Set content in to AuthSynch
     *
     * @param authSynch
     * @return
     */
    private ContentValues setMobileSynchContentValues(AuthSynch authSynch)
    {
        ContentValues values = new ContentValues();
        values.put("IMEI", authSynch.getIMEI());
        values.put("STATUS", authSynch.getStatus());
        values.put("STATE", authSynch.getState());

        //This is needed - as when no Rx is consumed server send "-"
        //This caused issues in Synching back with sever

        if (authSynch.getRxConsumed().equals("-")){
            authSynch.setRxConsumed("");
        }
        values.put("RXCONSUMED", authSynch.getRxConsumed());
        values.put("CAREDPAYLOAD", authSynch.getCarePayLoad());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String _createDate = simpleDateFormat.format(new Date());
        values.put("CREATEDATE", _createDate);
        return values;
    }


    /**
     * Set content in to CareActionSynch
     *
     * @param caredActionSynch
     * @return
     */
    private ContentValues setCaredActionSynchContentValues(CaredActionSynch caredActionSynch)
    {
        ContentValues values = new ContentValues();
        values.put("IMEI", caredActionSynch.getIMEI());
        values.put("STATUS", caredActionSynch.getStatus());
        values.put("STATE", caredActionSynch.getState());
        values.put("CAREDRESPONSE", caredActionSynch.getCaredResponse());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String _createDate = simpleDateFormat.format(new Date());
        values.put("CREATEDATE", _createDate);
        return values;
    }
}
