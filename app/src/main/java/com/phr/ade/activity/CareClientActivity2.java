package com.phr.ade.activity;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
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
import com.phr.ade.service.MCareHttpAsyncTask;
import com.phr.ade.service.McareOnTaskDoneListenerImpl;
import com.phr.ade.service.OnTaskDoneListener;
import com.phr.ade.util.CareClientConstants;
import com.phr.ade.util.CareClientUtil;
import com.phr.ade.xmlbinding.CaredPerson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class CareClientActivity2 extends Activity implements View.OnClickListener, CareClientConstants
{

    private static boolean alarmSet = false;
    private static boolean isActiveInBkgrnd = false;
    ProgressDialog progressDialog;
    private CaredPerson caredPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("CareClientActivity2", "--calling onCreate --");
        setContentView(R.layout.activity_care_client2);

        if (!alarmSet)
        {
            Calendar cur_cal = Calendar.getInstance();
            cur_cal.setTimeInMillis(System.currentTimeMillis());
            cur_cal.set(Calendar.HOUR_OF_DAY, 00);
            cur_cal.set(Calendar.MINUTE, 00);
            cur_cal.set(Calendar.SECOND, 0);

            Log.d("CareClientActivity2", "Calender Set time:" + cur_cal.getTime());
            Intent intent = new Intent(this, com.phr.ade.service.ClientService1.class);
            Log.d("CareClientActivity2", "Intent created");
            PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

            AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //Wake up after every 60 mins
            alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP,
                    cur_cal.getTimeInMillis(), 3600 * 1000, pi);
            //alarm_manager.setInexactRepeating(AlarmManager.RTC_WAKEUP,cur_cal.getTimeInMillis(), 3600 * 1000, pi);
            Log.d("CareClientActivity2", "alarm manager set");
            alarmSet = true;
        }
    }

    @Override
    public void onStart()
    {
        super.onStart();
        String _responseData = null;

        if (!isActiveInBkgrnd)
        {
            Log.d("CareClientActivity2", "--calling onStart --");
            Intent _intent = getIntent();
            char[] _rxSynchStatus = _intent.getCharArrayExtra("RX_SYNCH_STATUS");
            char[] _caredPersonName = _intent.getCharArrayExtra("CARED_PERSON");
            char[] _auth = _intent.getCharArrayExtra("AUTH");
            char[] _xmlData = _intent.getCharArrayExtra("XML_DATA");
            boolean _rxSchdl = _intent.getBooleanExtra("RX_SCHDL", false);

            String rxSynchStatus = _rxSynchStatus != null ? new String(_rxSynchStatus) : "";
            String caredPersonName = _caredPersonName != null ? new String(_caredPersonName) : "-";
            String auth = _auth != null ? new String(_auth) : "";

            Log.d("CareClientActivity2", "-- rxSynchStatus --" + rxSynchStatus);
            Log.d("CareClientActivity2", "-- AUTH --" + auth);
            Log.d("CareClientActivity2", "-- isActiveInBkgrnd --" + isActiveInBkgrnd);

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
            new LoginAsyncTask(this).execute();
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
     * @param v
     */
    public void onSynch(View v)
    {
        Log.d("CareClientActivity2", "--calling onSynch --");
        char[] _auth = getIntent().getCharArrayExtra("AUTH");
        boolean _rxSchdl = getIntent().getBooleanExtra("RX_SCHDL", false);
        String auth = _auth != null ? new String(_auth) : "";
        Log.d("CareClientActivity2", "-- onSynch.AUTH --" + auth + " _rxSchdl -- " + _rxSchdl);

        //temp <code>
        if (auth.equals("AUTH-PASSED") && _rxSchdl)
        {
            Intent intent = new Intent(this, com.phr.ade.activity.CareClientActivity3.class);
            intent.setAction(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("SERVICE_CALL", false);
            startActivity(intent);
        } else
        {
//            Intent _intent = new Intent(this, com.phr.ade.service.ClientService1.class);
//            PendingIntent pi = PendingIntent.getService(this, 0, _intent, 0);
//            startService(_intent);
            new LoginAsyncTask(this).execute();
        }
    }

    /**
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

        //code to clean closure - start
        //Intent intent = new Intent(CareClientActivity2.this, CareClientActivity2.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //startActivity(intent);
        //code to clean closure - end

        isActiveInBkgrnd = true;
        moveTaskToBack(true);
        /**
         _rxClose.setOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
        moveTaskToBack(true);
        }
        });
         **/
    }

    private String selectDisplayMessage(String rxSynchStatus, String caredPersonName, boolean rxSchdl, String auth, String xmlData)
    {

        String _messageToDisplay = "Welcome to mCareBridge. Data Sync-up in process. Please wait.";
        CaredPerson _caredPerson = null;

        Log.d("selectDisplayMessage ", rxSynchStatus + "-" + caredPersonName);
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
                    _messageToDisplay = "You have scheduled medication. Click on 'Load Rx' Proceed ";
                    CareClientUtil.triggerRxAlert(_context);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
                    _synchButton.setText("Load Rx");
                    _synchButton.setBackground(_d);
                } else
                {
                    _messageToDisplay = "Relax. No scheduled medication.";
                    _synchButton.setText("No Rx");
                    Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtndisabled);
                    _synchButton.setBackground(_d);
                    _synchButton.setClickable(false);
                }
                _closeButton.setClickable(true);
            }
            // auth.equals("AUTH-FAILED")
            else
            {
                Log.d("selectDisplayMessage : AuthStatus - ", auth + "-");
                _messageToDisplay = "Welcome to mCareBridge.";
                _synchButton.setClickable(true);
                _closeButton.setClickable(true);
                _caredPersonText.setText("New User");
                //Context _context = getApplicationContext();
                Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
                _synchButton.setBackground(_d);
                _synchButton.setText("Retry");
                _messageToDisplay += '\t' + "You are not registered with mCareBridge. Please contact your Care Provider.";
            }
        } else if (rxSynchStatus.equals("TIMEOUT"))
        {
            _messageToDisplay = "Connection Timeout. Please check internet connection.";
            setWifiIcon(CONNECTION_ERR);
            _synchButton.setText("Retry");
            _synchButton.setClickable(true);
            _closeButton.setClickable(true);
        } else if (rxSynchStatus.equals("HOST_NOT_FOUND"))
        {
            _messageToDisplay = "Unable to resolve server name. Please check internet connection.";
            setWifiIcon(CONNECTION_ERR);
            Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
            _synchButton.setBackground(_d);
            _synchButton.setText("Retry");
            _synchButton.setClickable(true);
            _closeButton.setClickable(true);
        } else if (rxSynchStatus.equals("ERROR"))
        {
            _messageToDisplay = "Error in Data Synch. Please report to admin@mcarebridge.com.";
            setWifiIcon(TIMEOUT_ERR);
            _synchButton.setClickable(false);
            _synchButton.setText("Synch Err");
            _closeButton.setClickable(true);
        } else
        {
            Log.d("Displaying rxSynchStatus values for last condition :  ----> ", rxSynchStatus);
            _messageToDisplay = "Unexpected err: " + rxSynchStatus + " Please report to admin@mcarebridge.com.";
            setWifiIcon(CONNECTION_ERR);
            Drawable _d = _context.getResources().getDrawable(R.drawable.synchbtn);
            _synchButton.setBackground(_d);
            _synchButton.setClickable(true);
            _synchButton.setText("Retry");
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

        String _emergencyNumber = getCaredPerson().getEmergencyResponse().getProvider().getCell();

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
            Toast.makeText(this, "No Emergency Contact Found", Toast.LENGTH_LONG)
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
        Uri uri = Uri.parse("smsto:" + "+19175018339");
        Intent sendIntent = new Intent(Intent.ACTION_SENDTO, uri);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is my text to send.");
        sendIntent.setAction(Intent.ACTION_SEND);

        sendIntent.setType("text/plain");
        // Put this line here
        sendIntent.setPackage("com.whatsapp");
        startActivity(sendIntent);
    }

    public void asynchAuthenticate(View v)
    {
        String BASE_URL = "https://mcarebridge.appspot.com/health/";
        String MCB_AUTH_SERVER_LINK = "caredPersonMobileSecurity";
        String MCB_DATA_EXCHANGE = "caredPersonMobileRxExchange";

        String _synchServerData = MCB_AUTH_SERVER_LINK;
        String _webAppURL = BASE_URL + MCB_AUTH_SERVER_LINK;

        Context _context = getApplicationContext();
        McareOnTaskDoneListenerImpl _mCareOnTask = new McareOnTaskDoneListenerImpl(this);
        new MCareHttpAsyncTask(_context, _webAppURL, _mCareOnTask).execute();
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Void>
    {

        public CareClientActivity2 ccActivity2;
        private Context mContext;
        private OnTaskDoneListener onTaskDoneListener;


        public LoginAsyncTask(CareClientActivity2 ccActivity2)
        {
            this.ccActivity2 = ccActivity2;
        }


        public LoginAsyncTask(Context context, OnTaskDoneListener onTaskDoneListener)
        {
            this.mContext = context;
            this.onTaskDoneListener = onTaskDoneListener;
        }


        @Override
        protected void onPreExecute()
        {

            Log.d("LoginAsyncTask.onPreExecute", "----");

            progressDialog = new ProgressDialog(CareClientActivity2.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args)
        {
            // Parse response data
            Log.d("LoginAsyncTask.doInBackground", "----");
            Intent _intent = new Intent(this.ccActivity2, com.phr.ade.service.ClientService1.class);
            startService(_intent);
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

