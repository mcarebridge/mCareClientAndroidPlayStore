package com.phr.ade.service;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.phr.ade.connector.CareXMLReader;
import com.phr.ade.connector.MCareBridgeConnector;
import com.phr.ade.dto.RxLineDTO;
import com.phr.ade.util.CareClientUtil;
import com.phr.ade.xmlbinding.CaredPerson;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientService1 extends IntentService
{

    public ClientService1()
    {
        super("ClientService1");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d("ClientService1", "--calling onHandleIntent --");
        //Uri _uri = intent.getData();
        //Log.d("ClientService", "uri path -- " + _uri.getPath());
    }

    @Override
    public void onCreate()
    {

        // TODO Auto-generated method stub
        super.onCreate();

        //CaredPerson _caredPerson = CareXMLReader.bindXML();
        //Log.d("ClientService", "Service got created");
        //Log.d("--Provider--", _caredPerson.getEmergencyResponse().getProvider().getName());
        //Toast.makeText(this, "ServiceClass.onCreate()", Toast.LENGTH_LONG)
        //        .show();

    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        super.onStart(intent, startId);
        Log.d("ClientService1", "--calling onStart -- " + startId);
        String _responseData = null;
        String _rxConsumed = null;
        String _rxSynch = "FALSE";
        boolean _serviceCall = false;
        String _rxSynchStatus = "NONE";
        String _caredPersonName = "-";
        //Toast.makeText(this, "ServiceClass.onStart()", Toast.LENGTH_LONG)
        //       .show();
        /**
         CaredPerson _caredPerson = CareXMLReader.bindXML();
         ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
         setUpAlarm(_rxLineDTOList);
         **/

        try
        {

            String imeiCode = readIMEICode();
            //_responseData = MCareBridgeConnector.synchMobileUsingIMEI("353322068558368");
            _responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
            _rxConsumed = MCareBridgeConnector.getRxConsumed();
            //Log.d("ClientService1 XML -- ", _responseData + "<<<<<<<");
            Log.d("ClientService1 -- RxConsumed -- ", _rxConsumed + "<<<<<<<");
            _rxSynch = "TRUE";
            _serviceCall = true;
            _rxSynchStatus = "SUCCESS";

        }
        catch (SocketTimeoutException s)
        {
            Log.e("ClientService1", s.getMessage(), s);
            s.printStackTrace();
            _rxSynchStatus = "TIMEOUT";
        }
        catch (UnknownHostException u)
        {
            Log.e("ClientService1", u.getMessage(), u);
            u.printStackTrace();
            //_rxSynchStatus = "TIMEOUT";
            _rxSynchStatus = "HOST_NOT_FOUND";
        }
        catch (Exception e)
        {
            Log.e("ClientService1", e.getMessage(), e);
            e.printStackTrace();
            //_rxSynchStatus = "ERROR";
            _rxSynchStatus = e.getMessage();

        }
        finally
        {

            boolean _isRxReady = false;
            intent.setAction(Intent.ACTION_MAIN);
            //intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //This is the case where Auth is not failed and sync is successful
            if (_responseData != null)
            {
                if (_rxSynchStatus.equals("SUCCESS") && !(_responseData.equalsIgnoreCase("AUTH-FAILED")))
                {
                    intent.putExtra("XML_DATA", _responseData.toCharArray());
                    intent.putExtra("RX_CONSUMED", _rxConsumed.toCharArray());
                    intent.putExtra("RX_ASYNC", _rxSynch.toCharArray());
                    intent.putExtra("AUTH", new String("AUTH-PASSED").toCharArray());
                    CaredPerson _caredPerson = CareXMLReader.bindXML(_responseData);
                    _caredPersonName = _caredPerson.getName();
                    intent.putExtra("CARED_PERSON", _caredPersonName.toCharArray());
                    ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
                    Log.d("ClientService1", "--size of List -- " + _rxLineDTOList.size());
                    _isRxReady = CareClientUtil.checkTimeToTriggerRx(_rxLineDTOList);
                } else
                {
                    intent.putExtra("AUTH", new String("AUTH-FAILED").toCharArray());
                }
            }
            intent.putExtra("RX_SYNCH_STATUS", _rxSynchStatus.toCharArray());
            intent.putExtra("SERVICE_CALL", _serviceCall);
            intent.putExtra("RX_SCHDL", _isRxReady);

            ComponentName cn = new ComponentName(this, com.phr.ade.activity.CareClientActivity2A.class);
            intent.setComponent(cn);

            Log.d("ClientService1", "_rxSynchStatus = " + _rxSynchStatus + " _isRxReady = " + _isRxReady);

            if (_rxSynchStatus.equals("SUCCESS"))
            {
                if (_isRxReady)
                {
                    Log.d("ClientService1", "-- start Activity Triggered Point 1--");
                    Toast.makeText(this, "Rx scheduled -- Loading Schedule", Toast.LENGTH_LONG)
                            .show();
                    //startActivity(intent);
                } else
                {
                    Toast.makeText(this, "Relax. No scheduled medication.", Toast.LENGTH_LONG)
                            .show();
                }
            } else if (_rxSynchStatus.equals("TIMEOUT"))
            {
                Toast.makeText(this, "Error : Connection Timeout", Toast.LENGTH_LONG)
                        .show();
            } else
            {
                Toast.makeText(this, "Error : Unexpected error. Please report admin@mcarebridge.com", Toast.LENGTH_LONG)
                        .show();
            }
        }
        Log.d("ClientService1", "In ClientService1 class ---> triggering intent to call CareClientActivity2A");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    private String readIMEICode()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String _deviceId = telephonyManager.getDeviceId();
        Log.i("ClientService1", _deviceId);
        //Only for testing
        //_deviceId = "867124022666036";
        return _deviceId;
    }

}
