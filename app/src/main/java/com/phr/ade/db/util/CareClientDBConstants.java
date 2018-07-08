package com.phr.ade.db.util;

/**
 *
 */

public interface CareClientDBConstants
{

    static final String DATABASE_NAME = "mcareMobileDb";
    static final int DATABASE_VERSION = 5;
    static final String TABLE_AUTH_SYNC = "authSync";
    static final String CARED_ACTION_SYNC = "careSync";

    // Database fields for authSync
    static final String KEY_ROWID = "_id";
    static final String IMEI = "IMEI";
    static final String CREATEDATE = "CREATEDATE";
    //STATUS values - AUTH-SUCCESS, AUTH-FAILED
    static final String STATUS = "STATUS";
    //STATE values = ACTIVE | INACTIVE
    static final String STATE = "STATE";
    static final String CGNAME = "CGNAME";
    static final String CGID = "CGID";
    static final String CGCELL = "CGCELL";
    static final String CAREDPAYLOAD = "CAREDPAYLOAD";
    static final String RXCONSUMED = "RXCONSUMED";
    static final String CAREDRESPONSE = "CAREDRESPONSE";


    // Database creation sql statement
    static final String TABLE_MOBILE_SYNCH = "create table "
            + TABLE_AUTH_SYNC
            + " (_id INTEGER primary key autoincrement, "
            + " IMEI TEXT not null,"
            + " STATUS TEXT not null,"
            + " STATE TEXT not null,"
            + " CREATEDATE TEXT not null,"
            + " RXCONSUMED TEXT,"
            + " CAREDPAYLOAD TEXT,"
            + " CAREDRESPONSE TEXT"
            + ")";


    static final String TABLE_CARED_ACTION_SYNC = "create table "
            + CARED_ACTION_SYNC
            + " (_id INTEGER primary key autoincrement, "
            + " IMEI TEXT not null,"
            + " STATUS TEXT not null,"
            + " STATE TEXT not null,"
            + " CAREDRESPONSE TEXT,"
            + " CREATEDATE TEXT not null"
            + ")";

}
