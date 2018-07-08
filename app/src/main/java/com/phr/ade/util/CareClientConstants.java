package com.phr.ade.util;


public interface CareClientConstants
{

    //Message for submitting or skipping Rx
    int RXTAKEN_SUCCESS = 0;
    int RXTAKEN_CONN_ERROR = -1;
    int MOBILE_DB_ERR = -2;
    int RXTAKEN_ERROR = -99;

    //Message for Phone error
    int SYNC_SUCCESSFUL = 0;
    int CONNECTION_ERR = 1;
    int TIMEOUT_ERR = 2;


    //ASYNC TRANSMISSION MESSAGES

    int REQUEST_ADDED_NOT_SENT = 100;
    int REQUEST_SENT = 102;
    int REQUEST_SENT_WAITING_FOR_RESP = 200;
    int REQUEST_SENT_RESP_RCVD = 202;
    int REQUEST_SENT_RCVD_ERR = 204;

    int REQUEST_MSG_NOT_READ = 300;
    int REQUEST_MSG_READ = 302;
}
