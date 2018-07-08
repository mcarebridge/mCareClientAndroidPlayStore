package com.phr.ade.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
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
import java.util.Calendar;
import java.util.Iterator;

public class ClientService extends IntentService
{

    public ClientService()
    {
        super("ClientService");
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.d("ClientService", "--calling onHandleIntent --");
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
        Log.d("ClientService", "--calling onStart -- " + startId);
        String _responseData = null;
        String _rxConsumed = null;
        String _rxSynch = "FALSE";
        boolean _serviceCall = false;
        String _rxSynchStatus = "NONE";
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
            //_responseData = MCareBridgeConnector.synchMobileUsingIMEI("353197050130472");
            _responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
            _rxConsumed = MCareBridgeConnector.getRxConsumed();
            Log.d("CareClientActivity1 XML -- ", _responseData);
            Log.d("CareClientActivity1 -- RxConsumed -- ", _rxConsumed);
            _rxSynch = "TRUE";
            _serviceCall = true;
            _rxSynchStatus = "SUCCESS";

        }
        catch (SocketTimeoutException s)
        {
            Log.e("ClientService", s.getMessage(), s);
            s.printStackTrace();
            _rxSynchStatus = "TIMEOUT";
        }
        catch (UnknownHostException u)
        {
            Log.e("ClientService", u.getMessage(), u);
            u.printStackTrace();
            _rxSynchStatus = "TIMEOUT";
        }
        catch (Exception e)
        {
            Log.e("ClientService", e.getMessage(), e);
            e.printStackTrace();
            _rxSynchStatus = "ERROR";

        }
        finally
        {

            boolean _isRxReady = false;
            intent.setAction(Intent.ACTION_MAIN);
            //intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (_rxSynchStatus.equals("SUCCESS"))
            {
                intent.putExtra("XML_DATA", _responseData.toCharArray());
                intent.putExtra("RX_CONSUMED", _rxConsumed.toCharArray());
                intent.putExtra("RX_ASYNC", _rxSynch.toCharArray());
                CaredPerson _caredPerson = CareXMLReader.bindXML(_responseData);
                ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
                Log.d("ClientService", "--size of List -- " + _rxLineDTOList.size());
                _isRxReady = CareClientUtil.checkTimeToTriggerRx(_rxLineDTOList);
            }
            intent.putExtra("RX_SYNCH_STATUS", _rxSynchStatus.toCharArray());
            intent.putExtra("SERVICE_CALL", _serviceCall);

            ComponentName cn = new ComponentName(this, com.phr.ade.activity.CareClientActivity2.class);
            intent.setComponent(cn);

            Log.d("ClientService", "_rxSynchStatus = " + _rxSynchStatus + "_isRxReady = " + _isRxReady);

            if (_rxSynchStatus.equals("SUCCESS"))
            {
                if (_isRxReady)
                {
                    Log.d("ClientService", "-- start Activity Triggered Point 1--");
                    startActivity(intent);
                }
            } else
            {
                Toast.makeText(this, "No Rx scheduled", Toast.LENGTH_LONG)
                        .show();
            }


            //For error scenario
            if (!_rxSynchStatus.equals("SUCCESS"))
            {
                Log.d("ClientService", "-- start Activity triggered Point 2--");
                startActivity(intent);
            }
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }


    /**
     *
     */
    private void setUpAlarm(ArrayList<RxLineDTO> rxLineDTOs)
    {


        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);
        Log.d("CareClientActivity", "Current Hours --" + _hour);

        for (Iterator iterator = rxLineDTOs.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            if (_rxLineDTO.getRxTime() >= _hour)
            {
                Log.d("CareClientActivity", _rxLineDTO.getRxTime() + " -- "
                        + _rxLineDTO.getRxLineId()
                        + " -- "
                        + _rxLineDTO.getRxLine().getRx());

                Intent intent = new Intent(this, com.phr.ade.service.ClientService.class);
                PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
                AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                long when = _c.getTimeInMillis();
                alarm_manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, when, (60 * 1000), pi);
            }
        }
    }

    /**
     * Read IMEI code
     */
    private String readIMEICode()
    {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String _deviceId = telephonyManager.getDeviceId();
        Log.i("CareClientActivity1", _deviceId);
        return _deviceId;
    }
}
