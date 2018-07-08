package com.phr.ade.service;

import android.content.Context;
import android.util.Log;

import com.phr.ade.connector.CareXMLReader;
import com.phr.ade.connector.MCareBridgeConnector;
import com.phr.ade.db.util.CareDatabaseAdaptor;
import com.phr.ade.db.vo.AuthSynch;
import com.phr.ade.db.vo.CaredActionSynch;
import com.phr.ade.dto.RxLineDTO;
import com.phr.ade.util.CareClientUtil;
import com.phr.ade.xmlbinding.CaredPerson;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class MCareHTTPService
{

    private String rxSynchStatus;

    public MCareHTTPService()
    {
    }

    public HashMap onStart(Context context)
    {
        Log.d("MCareHTTPService", "--calling onStart -- ");
        String _responseData = null;
        String _rxConsumed = null;
        String _rxSynch = "FALSE";
        boolean _serviceCall = false;
        String rxSynchStatus = "NONE";
        String _caredPersonName = "-";
        String imeiCode = "-";

        HashMap<String, String> headerKeyValueList = new HashMap<String, String>();

        try
        {
            imeiCode = readIMEICode(context);
            //_responseData = MCareBridgeConnector.synchMobileUsingIMEI("353322068558368");

            /**
             * Synch up the local db with Server
             *
             * 1. From careSync read all the records with state = OPEN
             * 2. Send record one by one
             * 3. Update all careRecord = STATUS='ARCHIVED', STATE='TRANSMITTED'
             *
             */
            readAndSendLocalRecords(context, imeiCode);

            _responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
            _rxConsumed = MCareBridgeConnector.getRxConsumed();
            //Log.d("MCareHTTPService XML -- ", _responseData + "<<<<<<<");
            Log.d("MCareHTTPService -- RxConsumed -- ", _rxConsumed + "<<<<<<<");
            _rxSynch = "TRUE";
            _serviceCall = true;
            rxSynchStatus = "SUCCESS";

        }
        catch (SocketTimeoutException s)
        {
            Log.e("MCareHTTPService", s.getMessage(), s);
            s.printStackTrace();
            rxSynchStatus = "TIMEOUT";
        }
        catch (UnknownHostException u)
        {
            Log.e("MCareHTTPService", u.getMessage(), u);
            u.printStackTrace();
            //_rxSynchStatus = "TIMEOUT";
            rxSynchStatus = "HOST_NOT_FOUND";
        }
        catch (Exception e)
        {
            Log.e("MCareHTTPService", e.getMessage(), e);
            e.printStackTrace();
            //_rxSynchStatus = "ERROR";
            rxSynchStatus = e.getMessage();

        }
        finally
        {

            boolean _isRxReady = false;
            //Default failed message.
            headerKeyValueList.put("AUTH", "AUTH-FAILED");
            headerKeyValueList.put("RX_SYNCH_STATUS", rxSynchStatus);
            //This is the case where Auth is not failed and sync is successful
            if (_responseData != null)
            {
                if (rxSynchStatus.equals("SUCCESS") && !(_responseData.equalsIgnoreCase("AUTH-FAILED")))
                {
                    //intent.putExtra("XML_DATA", _responseData.toCharArray());
                    //intent.putExtra("RX_CONSUMED", _rxConsumed.toCharArray());
                    //intent.putExtra("RX_ASYNC", _rxSynch.toCharArray());
                    //intent.putExtra("AUTH", new String("AUTH-PASSED").toCharArray());
                    headerKeyValueList.put("XML_DATA", _responseData);
                    headerKeyValueList.put("RX_CONSUMED", _rxConsumed);
                    headerKeyValueList.put("RX_ASYNC", _rxSynch);
                    headerKeyValueList.put("AUTH", "AUTH-PASSED");

                    CaredPerson _caredPerson = CareXMLReader.bindXML(_responseData);
                    _caredPersonName = _caredPerson.getName();
                    //intent.putExtra("CARED_PERSON", _caredPersonName.toCharArray());
                    headerKeyValueList.put("CARED_PERSON", _caredPersonName);
                    ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
                    Log.d("MCareHTTPService", "--size of List -- " + _rxLineDTOList.size());
                    _isRxReady = CareClientUtil.checkTimeToTriggerRx(_rxLineDTOList);
                } else
                {
                    //intent.putExtra("AUTH", new String("AUTH-FAILED").toCharArray());

                    headerKeyValueList.put("AUTH", "AUTH-FAILED");
                }
                // Add db code : start
                //Add the data in db if the connection is successful;
                try
                {
                    AuthSynch _authSynch = new AuthSynch();
                    _authSynch.setStatus(rxSynchStatus);
                    _authSynch.setState("CURRENT");
                    _authSynch.setRxConsumed(_rxConsumed);
                    _authSynch.setIMEI(imeiCode);
                    _authSynch.setCarePayLoad(_responseData);
                    addSynchDataToDB(_authSynch, context);
                }
                catch (Exception e)
                {
                    Log.e("MCareHTTPService", e.getMessage(), e);
                    e.printStackTrace();
                    //_rxSynchStatus = "ERROR";
                    rxSynchStatus = e.getMessage();
                }
                //Add db code : end
            }
            //intent.putExtra("RX_SYNCH_STATUS", _rxSynchStatus.toCharArray());
            //intent.putExtra("SERVICE_CALL", _serviceCall);
            //intent.putExtra("RX_SCHDL", _isRxReady);

            /**
             * Read the db if the server connection fails and for cached Authorization within last 4 hrs.
             * If exist then only Authorise.
             */
            if (rxSynchStatus.equals("HOST_NOT_FOUND") | rxSynchStatus.equals("TIMEOUT"))
            {
                try
                {
                    AuthSynch _authSynch = readLastAuthRecord(context);
                    if (_authSynch != null)
                    {
                        rxSynchStatus = _authSynch.getStatus();
                        headerKeyValueList.put("XML_DATA", _authSynch.getCarePayLoad());
                        headerKeyValueList.put("RX_CONSUMED", _authSynch.getRxConsumed());
                        headerKeyValueList.put("RX_ASYNC", _rxSynch);
                        headerKeyValueList.put("RX_SYNCH_STATUS", rxSynchStatus);
                        if (_authSynch.getStatus().equals("SUCCESS"))
                        {
                            headerKeyValueList.put("AUTH", "AUTH-PASSED");
                            CaredPerson _caredPerson = CareXMLReader.bindXML(_authSynch.getCarePayLoad());
                            _caredPersonName = _caredPerson.getName();
                            //intent.putExtra("CARED_PERSON", _caredPersonName.toCharArray());
                            headerKeyValueList.put("CARED_PERSON", _caredPersonName);
                            ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
                            Log.d("MCareHTTPService", "--size of List -- " + _rxLineDTOList.size());
                            _isRxReady = CareClientUtil.checkTimeToTriggerRx(_rxLineDTOList);

                        } else if (_authSynch.getStatus().equals("AUTH-FAILED"))
                        {
                            headerKeyValueList.put("AUTH", "AUTH-FAILED");
                        }
                    }
                }
                catch (Exception re)
                {
                    Log.e("MCareHTTPService", re.getMessage(), re);
                    re.printStackTrace();
                }
            }


            headerKeyValueList.put("SERVICE_CALL", new Boolean(_serviceCall).toString());
            headerKeyValueList.put("RX_SCHDL", new Boolean(_isRxReady).toString());


            Log.d("MCareHTTPService", "_rxSynchStatus = " + rxSynchStatus + " _isRxReady = " + _isRxReady);

            return headerKeyValueList;
        }
    }


    /**
     * Send local stored caredresponse data to server and update
     * record in local system
     *
     * @param context
     * @throws Exception
     */
    private void readAndSendLocalRecords(Context context, String imei) throws Exception
    {
        //Read all the careSynch records with state = 'OPEN'

        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        ArrayList<CaredActionSynch> _careList = _careDbAdaptor.readCareSyncRecordInOpenState();
        _careDbAdaptor.close();


        int i = 0;
        while (i < _careList.size())
        {
            CaredActionSynch _careSync = _careList.get(i);

            //transmit the data to server
            MCareBridgeConnector.sendCaredPersonRxData(imei, _careSync.getCaredResponse());

            //The record will not be updated if the connection fails
            //@todo : what is the auth fail?
            //Update caresync
            _careDbAdaptor.open();
            _careSync.setState("TRANSMITTED");
            _careSync.setStatus("ARCHIVED");
            long updatedRecCount = _careDbAdaptor.updateCareSynchedRow(_careSync);
            _careDbAdaptor.close();
            i++;
        }

    }


    private void    addSynchDataToDB(AuthSynch authSynch, Context context) throws Exception
    {
        /**
         * Add the data in mobile table if only the Synch is sucessful
         */
        // Added the code for local db Synch
        // db Code start
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);

        //find the record with state = CURRENT.
        AuthSynch _authSynchRead = readLastAuthRecord(context);

        //if found update state to ARCHIVE
        if (_authSynchRead != null)
        {
            _authSynchRead.setState("ARCHIVED");
            updateAuthRecord(_authSynchRead, context);
        }

        _careDbAdaptor.open();
        _careDbAdaptor.insertMobileSynchedRow(authSynch);
        _careDbAdaptor.close();
    }

    /**
     * @param context
     * @return
     * @throws Exception
     */
    private AuthSynch readLastAuthRecord(Context context) throws Exception
    {
        /**
         * Add the data in mobile table if only the Synch is sucessful
         */
        // Added the code for local db Synch
        // db Code start
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        AuthSynch _authSync = _careDbAdaptor.readMobileAuthRecord();
        _careDbAdaptor.close();

        return _authSync;
    }


    /**
     * @param context
     * @return
     * @throws Exception
     */
    private long updateAuthRecord(AuthSynch authSynch, Context context) throws Exception
    {
        /**
         * Add the data in mobile table if only the Synch is sucessful
         */
        // Added the code for local db Synch
        // db Code start
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        long updatedRecCount = _careDbAdaptor.updateMobileSynchedRow(authSynch);
        _careDbAdaptor.close();
        return updatedRecCount;
    }


    private String readIMEICode(Context context)
    {
//        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
//        String _deviceId = telephonyManager.getDeviceId();
//        //Only for testing
//        _deviceId = "354124073452765";
//
//        Log.i("MCareHTTPService", _deviceId);

        return CareClientUtil.readIMEICode(context);
    }
}
