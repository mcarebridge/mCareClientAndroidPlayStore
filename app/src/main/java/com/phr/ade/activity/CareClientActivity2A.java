package com.phr.ade.activity;

/**
 * This class open the UI to accept the Rx and Symptoms
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.phr.ade.connector.CareXMLReader;
import com.phr.ade.service.MCareHTTPService;
import com.phr.ade.service.MCareHttpClientAsyncTask;
import com.phr.ade.service.McareOnTaskDoneListenerImpl;
import com.phr.ade.service.OnTaskDoneListener;
import com.phr.ade.util.CareClientConstants;
import com.phr.ade.util.CareClientUtil;
import com.phr.ade.xmlbinding.CaredPerson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/******************************************************************************
 * Copyright mCareBridge 2017-18
 * This class paints the main GUI. Main functions
 * - Authentication
 * - Check for Rx Readiness
 *
 *****************************************************************************/

public class CareClientActivity2A extends Activity implements View.OnClickListener, CareClientConstants
{

    private static boolean alarmSet = false;
    private static boolean isActiveInBkgrnd = false;
    ProgressDialog progressDialog;
    private CaredPerson caredPerson;

    /**
     * Loaded at create instance
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        Log.d("CareClientActivity2A", "--calling onCreate --");
        setContentView(R.layout.activity_care_client2a);
        Intent _intent = getIntent();
        boolean _serviceCall = _intent.getBooleanExtra("SERVICE_CALL", false);
        Log.d("CareClientActivity2A", "--- onCreate._serviceCall --" + _serviceCall);
        Log.d("CareClientActivity2A", "--- onCreate.alertSet --" + alarmSet);

        /**
         if (!alarmSet) {
         Calendar cur_cal = Calendar.getInstance();
         cur_cal.setTimeInMillis(System.currentTimeMillis());
         cur_cal.set(Calendar.HOUR_OF_DAY, 00);
         cur_cal.set(Calendar.MINUTE, 00);
         cur_cal.set(Calendar.SECOND, 0);

         Intent intent = new Intent(this, AlarmReceiver.class);
         PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
         AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         //Wake up after every 60 mins
         alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP,
         System.currentTimeMillis(), 60 * 1000, pi);
         //alarm_manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,cur_cal.getTimeInMillis(), 3600 * 1000, pi);
         Log.d("CareClientActivity2A", "------------------------> Alarm Manager Set");
         alarmSet = true;
         }
         **/
        if (!alarmSet)
        {
            AlarmReceiver alarm = new AlarmReceiver();
            alarm.setAlarm(this);
            alarmSet = true;
        }

        //TEST CODE FOR ONE TIME TRIGGER
        /**
         Context _context = getApplicationContext();
         if (!_serviceCall) {
         McareOnTaskDoneListenerImpl _mCareOnTask = new McareOnTaskDoneListenerImpl();
         MCareHttpClientAsyncTask _httpClientTask = new MCareHttpClientAsyncTask(_context, _mCareOnTask);
         _httpClientTask.execute();
         }
         **/
    }

    /**
     * onStart life cycle
     */
    @Override
    public void onStart()
    {
        super.onStart();
        String _responseData = null;

        if (!isActiveInBkgrnd)
        {
            Log.d("CareClientActivity2A", "--calling onStart --");
            String iso3Language = getApplicationContext().getResources().getConfiguration().locale.getISO3Language();
            Log.d("Application Locale ", iso3Language);
            Intent _intent = getIntent();
            char[] _rxSynchStatus = _intent.getCharArrayExtra("RX_SYNCH_STATUS");
            char[] _caredPersonName = _intent.getCharArrayExtra("CARED_PERSON");
            char[] _auth = _intent.getCharArrayExtra("AUTH");
            char[] _xmlData = _intent.getCharArrayExtra("XML_DATA");
            boolean _rxSchdl = _intent.getBooleanExtra("RX_SCHDL", false);

            String rxSynchStatus = _rxSynchStatus != null ? new String(_rxSynchStatus) : "";
            String caredPersonName = _caredPersonName != null ? new String(_caredPersonName) : "-";
            String auth = _auth != null ? new String(_auth) : "";

            Log.d("CareClientActivity2A", "-- rxSynchStatus --" + rxSynchStatus);
            Log.d("CareClientActivity2A", "-- AUTH --" + auth);
            Log.d("CareClientActivity2A", "-- isActiveInBkgrnd --" + isActiveInBkgrnd);

            if (_xmlData != null)
            {
                _responseData = new String(_xmlData);
            }

            TextView _rxMsgWindow = (TextView) findViewById(R.id.msgwindow);
            _rxMsgWindow.setTextColor(Color.BLACK);

            TextView _lastUpdTimeStamp = (TextView) findViewById(R.id.lastUpdTimeStamp);
            _lastUpdTimeStamp.setTextColor(Color.BLACK);

            String _messageToDisplay = selectDisplayMessage(rxSynchStatus, caredPersonName, _rxSchdl, auth, _responseData);

            _rxMsgWindow.setText(_messageToDisplay);
        } else
        {
//            new LoginAsyncTask(this).execute();
            McareOnTaskDoneListenerImpl _mCareOnTask = new McareOnTaskDoneListenerImpl(this);
            MCareHttpClientAsyncTask _httpClientTask = new MCareHttpClientAsyncTask(this, _mCareOnTask, true);
            _httpClientTask.execute();
            isActiveInBkgrnd = false;
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    public void onClick(View v)
    {
    }

    /**
     * Called on click of onSynch method
     *
     * @param v
     */
    public void onSynch(View v)
    {
        Log.d("CareClientActivity2A", "--calling onSynch --");
        char[] _auth = getIntent().getCharArrayExtra("AUTH");
        boolean _rxSchdl = getIntent().getBooleanExtra("RX_SCHDL", false);
        String auth = _auth != null ? new String(_auth) : "";
        Log.d("CareClientActivity2A", "-- onSynch.AUTH --" + auth + " _rxSchdl -- " + _rxSchdl);

        //temp <code>
        if (auth.equals("AUTH-PASSED") && _rxSchdl)
        {
            Intent intent = new Intent(this, CareClientActivity3.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("SERVICE_CALL", false);
            startActivity(intent);
        } else
        {
//            Intent _intent = new Intent(this, com.phr.ade.service.ClientService1.class);
//            PendingIntent pi = PendingIntent.getService(this, 0, _intent, 0);
//            startService(_intent);
            //new LoginAsyncTask(this).execute();
            McareOnTaskDoneListenerImpl _mCareOnTask = new McareOnTaskDoneListenerImpl(this);
            MCareHttpClientAsyncTask _httpClientTask = new MCareHttpClientAsyncTask(this, _mCareOnTask, false);
            _httpClientTask.execute();
        }
    }

    /**
     * Called on click of Quit method
     *
     * @param v
     */
    public void onQuit(View v)
    {
        Log.d("CareClient", "-- onQuit --");

        Button _rxClose = (Button) findViewById(R.id.closeBtn);
        //_rxClose.setClickable(false);

        Button _synchButton = (Button) findViewById(R.id.synchbtn);
        _synchButton.setClickable(false);

        Context _context = getApplicationContext();
        Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtndisabled);
        _synchButton.setBackground(_d);


        isActiveInBkgrnd = true;
        moveTaskToBack(true);
    }

    private String selectDisplayMessage(String rxSynchStatus, String caredPersonName, boolean rxSchdl, String auth, String xmlData)
    {

        String _messageToDisplay = "Welcome to mCareBridge. Data Sync-up in process. Please wait.";
        CaredPerson _caredPerson = null;

        if (rxSynchStatus.equals(""))
        {
            rxSynchStatus = "WELCOME";
        }

        Log.d("rxSynchStatus : ---**", rxSynchStatus + "** - caredPersonName : --**" + caredPersonName + "***");
        Button _synchButton = (Button) findViewById(R.id.synchbtn);
        Button _closeButton = (Button) findViewById(R.id.closeBtn);
        TextView _caredPersonText = (TextView) findViewById(R.id.cpName);

        TextView _rxMsgWindow = (TextView) findViewById(R.id.msgwindow);

        TextView _lastUpdTimeStamp = (TextView) findViewById(R.id.lastUpdTimeStamp);

        Date _currDate = Calendar.getInstance().getTime();
        SimpleDateFormat _sdf = new SimpleDateFormat("MMM d,y hh:mm a z");
        String _formattedDate = _sdf.format(_currDate);
        _lastUpdTimeStamp.setText(_formattedDate);

        Context _context = getApplicationContext();

        if (rxSynchStatus.equals("SUCCESS"))
        {
            //_messageToDisplay = "Schedule updated at : " + _formattedDate + '\n' + "Welcome " + caredPersonName;
            //_messageToDisplay = "Schedule updated at : " + _formattedDate;
            _synchButton.setClickable(true);
            _caredPersonText.setText(caredPersonName);

            if (auth.equals("AUTH-PASSED"))
            {

                if (xmlData != null)
                {
                    _caredPerson = CareXMLReader.bindXML(xmlData);
                    Log.d("-- Displaying Emergency Contact Details -- ", _caredPerson.getEmergencyResponse().getProvider().getContactPerson());
                    Log.d("-- Displaying Emergency Contact Details -- ", _caredPerson.getEmergencyResponse().getProvider().getCell());
                    setCaredPerson(_caredPerson);

                }

                if (rxSchdl)
                {
                    //_messageToDisplay = "You have scheduled medication. Click on 'Load Rx' to Proceed ";
                    _messageToDisplay = _context.getResources().getString(R.string.rxscdledMsg);
                    CareClientUtil.triggerRxAlert(_context);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
                    String _loadRx = _context.getResources().getString(R.string.btnLoadRx);
                    _synchButton.setText(_loadRx);
                    _synchButton.setBackground(_d);
                } else
                {
                    _messageToDisplay = _context.getResources().getString(R.string.norxscdledMsg);
                    String _noRx = _context.getResources().getString(R.string.btnNoRx);
                    _synchButton.setText(_noRx);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtndisabled);
                    _synchButton.setBackground(_d);
                    _synchButton.setClickable(false);
                }
                _closeButton.setClickable(true);
            }
            // auth.equals("AUTH-FAILED")
            else
            {
                Log.d("selDisplayMsg:authStatus:", auth + "-");
                _messageToDisplay = _context.getResources().getString(R.string.msgMainWelcome);
                _synchButton.setClickable(true);
                _closeButton.setClickable(true);
                _caredPersonText.setText("New User");
                //Context _context = getApplicationContext();
                Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
                _synchButton.setBackground(_d);
                String _retry = _context.getResources().getString(R.string.btnRetry);
                _synchButton.setText(_retry);
                _messageToDisplay += '\t' + _context.getResources().getString(R.string.msgNotReg);
            }
        } else if (rxSynchStatus.equals("WELCOME"))
        {
            _messageToDisplay = _context.getResources().getString(R.string.msgWelcome);
            setWifiIcon(SYNC_SUCCESSFUL);
            Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
            _synchButton.setBackground(_d);
            String _synch = _context.getResources().getString(R.string.btnSynch);
            _synchButton.setText(_synch);
            _synchButton.setClickable(true);
            _closeButton.setClickable(true);
        } else if (rxSynchStatus.equals("TIMEOUT"))
        {
            _messageToDisplay = _context.getResources().getString(R.string.msgTimeOutNoInternet);
            setWifiIcon(CONNECTION_ERR);
            String _retry = _context.getResources().getString(R.string.btnRetry);
            _synchButton.setText(_retry);
            _synchButton.setClickable(true);
            _closeButton.setClickable(true);
        } else if (rxSynchStatus.equals("HOST_NOT_FOUND"))
        {
            _messageToDisplay = _context.getResources().getString(R.string.msgSrvNotFndNoInternet);
            setWifiIcon(CONNECTION_ERR);
            Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
            _synchButton.setBackground(_d);
            String _retry = _context.getResources().getString(R.string.btnRetry);
            _synchButton.setText(_retry);
            _synchButton.setClickable(true);
            _closeButton.setClickable(true);
        } else if (rxSynchStatus.equals("ERROR"))
        {
            _messageToDisplay = _context.getResources().getString(R.string.msgDataSynchErr);
            setWifiIcon(TIMEOUT_ERR);
            _synchButton.setClickable(false);
            String _synchErr = _context.getResources().getString(R.string.btnSynchErr);
            _synchButton.setText(_synchErr);
            _closeButton.setClickable(true);
        } else
        {
            Log.d("Displaying rxSynchStatus values for last condition :  ----> ", rxSynchStatus);
            _messageToDisplay = "Unexpected err: " + rxSynchStatus + " Please report to care@sevha.com.";
            setWifiIcon(CONNECTION_ERR);
            Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
            _synchButton.setBackground(_d);
            _synchButton.setClickable(true);
            String _retry = _context.getResources().getString(R.string.btnRetry);
            _synchButton.setText(_retry);
            _closeButton.setClickable(true);
        }

        return _messageToDisplay;
    }

    /**
     * Set Background ICON thru code
     */

    private void setWifiIcon(int phoneError)
    {
        Context _context = getApplicationContext();
        // Left, top, right, bottom drawables.
        Button _synchButton = (Button) findViewById(R.id.synchbtn);
        Drawable[] drawables = _synchButton.getCompoundDrawables();
        Drawable _img = null;
        // get left drawable.
        Drawable _topCompoundDrawable = drawables[1];
        // get new drawable.

        switch (phoneError)
        {
            case SYNC_SUCCESSFUL:
                _img = _context.getResources().getDrawable(R.drawable.ic_local_pharmacy_white_24dp);
                break;

            case CONNECTION_ERR:
                _img = _context.getResources().getDrawable(R.drawable.ic_signal_wifi_off_white_24dp);
                break;

            case TIMEOUT_ERR:
                _img = _context.getResources().getDrawable(R.drawable.ic_sync_problem_white_24dp);
                break;

            default:
                break;
        }

        // set image size (don't change the size values)
        _img.setBounds(_topCompoundDrawable.getBounds());
        // set new drawable
        _synchButton.setCompoundDrawables(null, _img, null, null);
    }

    public void dialEmergency(View v)
    {
        long emergencyContactNumber = -1;
        String emergencyContactProvider = "None";
        Context _context = getApplicationContext();
        String _emergencyNumber = null;

        try
        {
            _emergencyNumber = getCaredPerson().getEmergencyResponse().getProvider().getCell();

            if (!_emergencyNumber.equalsIgnoreCase("-"))
            {
                emergencyContactNumber = new Long(_emergencyNumber).longValue();
            }

            emergencyContactProvider = getCaredPerson().getEmergencyResponse().getProvider().getContact();

            if (emergencyContactNumber != -1)
            {

                _emergencyNumber = "tel:" + emergencyContactNumber;
                Uri number = Uri.parse(_emergencyNumber);
                Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                Toast.makeText(this, "Calling Emergency Contact :" + emergencyContactProvider, Toast.LENGTH_LONG)
                        .show();
                startActivity(callIntent);
            } else
            {
                Toast.makeText(this, _context.getResources().getString(R.string.msgNoEmgContact), Toast.LENGTH_LONG)
                        .show();
            }
        }
        catch(NullPointerException npe)
        {
            Toast.makeText(this, _context.getResources().getString(R.string.msgNoEmgContact), Toast.LENGTH_LONG)
                    .show();
        }

    }

    public CaredPerson getCaredPerson()
    {
        return caredPerson;
    }

    private void setCaredPerson(CaredPerson caredPerson)
    {
        this.caredPerson = caredPerson;
    }

    public void sendWhatsAppMessage(View v)
    {
        Uri uri = Uri.parse("smsto:" + "+xxxxxxxxxxx");
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("text/plain");
        // Put this line here
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Void>
    {

        public CareClientActivity2A ccActivity2;
        private Context mContext;
        private McareOnTaskDoneListenerImpl onTaskDoneListener;


        public LoginAsyncTask(CareClientActivity2A ccActivity2)
        {
            this.ccActivity2 = ccActivity2;
        }


        public LoginAsyncTask(Context context, OnTaskDoneListener onTaskDoneListener)
        {
            this.mContext = context;
            this.onTaskDoneListener = new McareOnTaskDoneListenerImpl(context);
        }


        @Override
        protected void onPreExecute()
        {

            Log.d("LoginAsyncTask.onPreExecute", "----");

            progressDialog = new ProgressDialog(CareClientActivity2A.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args)
        {
            // Parse response data
            Log.d("LoginAsyncTask.doInBackground", "----");
            String _responseData = "--- RESPONSE STRING FROM LoginAsyncTask.doInBackground --";

//            try {
//                Thread.sleep(1000 * 6);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            MCareHTTPService _mCareHttpService = new MCareHTTPService();
            HashMap<String, String> mCareKeyValue = _mCareHttpService.onStart(this.mContext);

//            Intent _intent = new Intent(this.ccActivity2, com.phr.ade.service.ClientService1.class);
//            startService(_intent);
            return null;
        }

        protected void onPostExecute(Void result)
        {

            Log.d("LoginAsyncTask.onPostExecute", "----");

            new Thread()
            {
                @Override
                public void run()
                {
                    super.run();
                    try
                    {
                        Thread.sleep(1000);
                        if (progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }.start();

//            if (progressDialog.isShowing())
//                progressDialog.dismiss();
            //move activity
            super.onPostExecute(result);
        }
    }
}

