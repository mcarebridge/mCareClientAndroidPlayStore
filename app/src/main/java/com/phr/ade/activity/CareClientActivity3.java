package com.phr.ade.activity;


import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.phr.ade.connector.CareXMLReader;
import com.phr.ade.connector.MCareBridgeConnector;
import com.phr.ade.db.util.CareDatabaseAdaptor;
import com.phr.ade.db.vo.AuthSynch;
import com.phr.ade.db.vo.CaredActionSynch;
import com.phr.ade.dto.RxLineDTO;
import com.phr.ade.util.CareClientConstants;
import com.phr.ade.util.CareClientUtil;
import com.phr.ade.xmlbinding.CaredPerson;
import com.phr.ade.xmlbinding.Condition;
import com.phr.ade.xmlbinding.PreExistingCondition;
import com.phr.ade.xmlbinding.RxLines;
import com.phr.ade.xmlbinding.Symptoms;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;


public class CareClientActivity3 extends Activity implements View.OnClickListener, CareClientConstants
{

    private static boolean alarmSet = false;
    private static boolean _sb1Pressed = false;
    private static boolean _sb2Pressed = false;
    private static boolean _sb3Pressed = false;
    private static boolean _sb4Pressed = false;
    private static boolean _sb5Pressed = false;
    private static boolean _sb6Pressed = false;
    private static boolean _sb7Pressed = false;
    private static boolean _sb8Pressed = false;

    private static boolean _rxchk0checked = false;
    private static boolean _rxchk1checked = false;
    private static boolean _rxchk2checked = false;

    private static List<RxLineDTO> rxLineDTOList = null;
    private static boolean rxListHasMore = false;
    private static int rxListCurrPointer = 0;
    private static List<Long> rxListChecked = null;
    private static String rxConsumed = null;
    private static ArrayList<Long> consumedRxList = new ArrayList<Long>();
    private static int rxListReceivedSize = 0;

    private static long emergencyContactNumber = -1;
    private static String emergencyContactProvider = "None";

    private static String responseData = "";
    ProgressDialog progressDialog;
    private CaredPerson caredPerson;

    /**
     * @param rxList
     * @param currPos
     * @return
     */
    public static List<RxLineDTO> readNext(List<RxLineDTO> rxList, int currPos)
    {
        Log.i("CareClientActivity3", "readNext.rxListCurrPointer ========> " + currPos);
        //Log.i("CareClientActivity3", "readNext.rxList.size ========> " + rxList.size());

        int _endofList = -1;
        int _startofList = 0;
        List<RxLineDTO> _subList = null;

        if (currPos > rxList.size())
        {
            currPos = 0;
            rxListCurrPointer = _startofList + 3;
            _startofList = currPos;
        } else
        {
            _startofList = currPos;
            rxListCurrPointer = _startofList + 3;
        }

        _endofList = _startofList + 3;


        if (_endofList > rxList.size())
        {
            //_startofList = _startofList - 1;
            _endofList = rxList.size();
        }

        Log.i("CareClientActivity3", "readNext fetching from  => "
                + (_startofList) + " to = " + (_endofList));
        _subList = rxList.subList(_startofList, _endofList);

        Log.i("CareClientActivity3", "readNext._subList.size ========> " + _subList.size());


        for (Iterator iterator = _subList.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            System.out.println(" --------> _rxLine = " + _rxLineDTO.getRxLine().getRx() + "---" + _rxLineDTO.getRxLine().getScheduleByHours());
        }

        return _subList;
    }

    /**
     * This method spilts the rxconsumed string into a Array
     *
     * @param rxConsumed
     */
    private static void splitRxConsumed(String rxConsumed)
    {

        Log.d("CareClientActivity3", "--calling splitRxConsumed -- " + rxConsumed);

        ArrayList<Long> _consumedRxList = new ArrayList<Long>();
        if (rxConsumed != null && !rxConsumed.equals("-"))
        {
            StringTokenizer _st = new StringTokenizer(rxConsumed, ",");

            while (_st.hasMoreElements())
            {

                Long _rxId = new Long((String) _st.nextElement());

                _consumedRxList.add(_rxId);
            }
        }
        consumedRxList = _consumedRxList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("CareClientActivity3", "--calling onCreate --");

        setContentView(R.layout.activity_care_client3);
        addRxCheckBoxLister();
        onTakenClick();
        onSkipClick();
        rxListChecked = new ArrayList<Long>();

        //findViewById(R.id.rxtaken).setOnClickListener(this);


        //setUpAlarm(_rxLineDTOList);
        /**
         if (!alarmSet) {
         Calendar cur_cal = Calendar.getInstance();
         cur_cal.setTimeInMillis(System.currentTimeMillis());
         cur_cal.set(Calendar.HOUR_OF_DAY, 00);
         cur_cal.set(Calendar.MINUTE, 00);
         cur_cal.set(Calendar.SECOND, 0);

         Log.d("CareClientActivity3", "Calender Set time:" + cur_cal.getTime());
         Intent intent = new Intent(this, com.phr.ade.service.ClientService.class);
         Log.d("CareClientActivity3", "Intent created");

         PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

         AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
         //Wake up after every 60 secs
         alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP,
         cur_cal.getTimeInMillis(), 3600 * 1000, pi);
         Log.d("CareClientActivity3", "alarm manager set");
         alarmSet = true;
         }
         **/

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("CareClientActivity3", "--onStart--");

        //@todo : Synch with server and get the latest XML data

        Intent _intent = getIntent();
        String _responseData = null;
        String _rxConsumed = null;
        String _rxSync = null;
        String _rxSynchStatusString = "INIT";
        String _auth = "AUTH-FAILED";
        boolean _serviceCall = false;

        //temp code 04/01/2017 to reset all the symptom buttons
        resetAllSymptomButtons(false);
        consumedRxList = new ArrayList<Long>();

        Log.d("CareClientActivity3", "--_serviceCall before--" + _serviceCall);

        //new code start
        char[] _xmlData = _intent.getCharArrayExtra("XML_DATA");
        char[] _rxConsumedChars = _intent.getCharArrayExtra("RX_CONSUMED");
        char[] _rxSyncChars = _intent.getCharArrayExtra("RX_ASYNC");
        char[] _rxSynchStatus = _intent.getCharArrayExtra("RX_SYNCH_STATUS");
        char[] _authStatus = _intent.getCharArrayExtra("AUTH");
        _serviceCall = _intent.getBooleanExtra("SERVICE_CALL", false);

        //**
        RxLineDTO _rxDTO = new RxLineDTO();
        ArrayList<RxLineDTO> _rxDTOList = new ArrayList<RxLineDTO>();
        _rxDTOList.add(_rxDTO);
        //Commented on 02/22 for Nullpointer err  starting from here.
        //paintRxLines1(_rxDTOList);
        //*

        //reset the form of previous Data

        //new code end
        Log.d("CareClientActivity3", "--_serviceCall after--" + _serviceCall);

        //Call direct if called explicitly

        if (!_serviceCall)
        {
            try
            {

                String imeiCode = CareClientUtil.readIMEICode(this);

                _responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
                //_responseData = responseData;
                //if the auth is going fail then _responseData = "AUTH-FAILED";
                if (!_responseData.equalsIgnoreCase("AUTH-FAILED"))
                {
                    _auth = "AUTH-PASSED";
                }

                _rxConsumed = MCareBridgeConnector.getRxConsumed();
                _rxSynchStatusString = "SUCCESS";
                Log.d("CareClientActivity3 XML -- ", _responseData);
                Log.d("CareClientActivity3 -- RxConsumed -- ", _rxConsumed + " ########## ");
                splitRxConsumed(_rxConsumed);
            }
            catch (SocketTimeoutException s)
            {
                Log.e("CareClientActivity3", s.getMessage(), s);
                s.printStackTrace();
                _rxSynchStatusString = "TIMEOUT";
            }
            catch (UnknownHostException u)
            {
                Log.e("CareClientActivity3", u.getMessage(), u);
                u.printStackTrace();
                _rxSynchStatusString = "TIMEOUT";
                //Local DB code - start
                //read the last synched record from authSync
                try
                {
                    AuthSynch _authSync = readLastAuthRecord(this);

                    //This reading current record
                    if (_authSync != null)
                    {
                        if (_authSync.getStatus().equals("SUCCESS"))
                        {
                            _auth = "AUTH-PASSED";
                            _responseData = _authSync.getCarePayLoad();
                            _rxConsumed = _authSync.getRxConsumed();
                            _rxSynchStatusString = "SUCCESS";
                            splitRxConsumed(_rxConsumed);
                        }
                    }

                }
                catch (Exception sqle)
                {
                    sqle.printStackTrace();
                    _rxSynchStatusString = "DB ERROR";
                }
                //Local DB code - end
            }
            catch (Exception e)
            {
                Log.e("CareClientActivity3", e.getMessage(), e);
                e.printStackTrace();
                _rxSynchStatusString = "ERROR";
            }
        } else
        {
            if (_rxSynchStatus != null)
            {
                _rxSynchStatusString = new String(_rxSynchStatus);
            }

            if (_xmlData != null)
            {
                _responseData = new String(_xmlData);
            }

            if (_rxConsumedChars != null)
            {
                _rxConsumed = new String(_rxConsumedChars);
                splitRxConsumed(_rxConsumed);
            }

            if (_authStatus != null)
            {
                _auth = new String(_authStatus);
            }
        }

        String _a = "ERR:NO DATA PASSED BY INTENT";
        TextView _rxFor = (TextView) findViewById(R.id.rxfor);
        TextView _rxFor1 = (TextView) findViewById(R.id.rxfor1);
        _rxFor.setTextColor(Color.BLACK);
        _rxFor1.setTextColor(Color.BLACK);

        Log.d("_rxSynchStatusString -----> ", _rxSynchStatusString);

        //if (_rxSynchStatusString.equals("SUCCESS") && _responseData != null) {
        if (_responseData != null)
        {
            if (_rxSynchStatusString.equals("SUCCESS") && _auth.equalsIgnoreCase("AUTH-PASSED"))
            {
                //_a = new String(_xmlData);
                //Log.d("XML Received--", _a);
                _a = _responseData;
                CaredPerson _caredPerson = CareXMLReader.bindXML(_responseData);
                this.caredPerson = _caredPerson;
                ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);

                //get emergencyContactNumber
                String _emergencyNumber = _caredPerson.getEmergencyResponse().getProvider().getCell();
                if (!_emergencyNumber.equalsIgnoreCase("-"))
                {
                    emergencyContactNumber = new Long(_emergencyNumber).longValue();
                }

                emergencyContactProvider = _caredPerson.getEmergencyResponse().getProvider().getContact();

                Log.d("CareClientActivity3 onStart _rxLineDTOList.size()", "------->" + _rxLineDTOList.size());
                _rxLineDTOList = pickRxForHour(_rxLineDTOList);
                rxLineDTOList = _rxLineDTOList;
                paintScreen(this, _caredPerson, _rxLineDTOList);

                boolean _isRxReady = CareClientUtil.checkTimeToTriggerRx(_rxLineDTOList);
                if (_isRxReady)
                {
                    sendUpdateNotification(_caredPerson.getName());
                } else
                {
                    Log.d("CareClientActivity3 interface", "-------> No Scheduled Rx Found <----------");
                    Toast.makeText(this, "No Rx scheduled", Toast.LENGTH_LONG)
                            .show();

                    //Go to Appln landing screen
                    Intent intent = new Intent(this, com.phr.ade.activity.CareClientActivity2A.class);
                    startActivity(intent);
                }
            } else if (_rxSynchStatusString.equals("SUCCESS") && _auth.equalsIgnoreCase("AUTH-FAILED"))
            {
                _rxFor.setText("Authorisation Failed : Please register the Cared Person");
                disableScreen(this);
            }

        } else
        {
            _rxFor.setText("Error in Data Synch. Please try again.");
            Log.d("CareClientActivity3 interface", "------->" + _rxSynchStatusString);
            if (_rxSynchStatusString.equalsIgnoreCase("TIMEOUT"))
            {
                _rxFor.setText("Connection Timeout. Please try again");
            }
            _rxFor.setTextColor(Color.RED);
//            _rxFor1.setText("-");
            _rxFor1.setTextColor(Color.RED);
            disableScreen(this);
        }

        _intent.removeExtra("SERVICE_CALL");
        _intent.removeExtra("XML_DATA");
        _intent.removeExtra("RX_CONSUMED");
        _intent.removeExtra("RX_ASYNC");
        _intent.removeExtra("RX_SYNCH_STATUS");
        _intent.removeExtra("AUTH");


        //Log.d("CareClientActivity onStart--", _a);
    }

    /**
     * Read the last synched recors
     *
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
     * @param _rxLineDTOList
     * @return
     */
    private ArrayList<RxLineDTO> pickRxForHour(ArrayList<RxLineDTO> _rxLineDTOList)
    {
        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);

        for (Iterator iterator = _rxLineDTOList.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            if (_rxLineDTO.getRxTime() != _hour)
            {
                iterator.remove();
            }
        }
        return _rxLineDTOList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.care_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings)
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view)
    {


    }

    public void addRxCheckBoxLister()
    {
        Log.d("CareClientActivity3", "-- checkBoxClicked Method --");

        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
        _rxCheckBox0.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Long _a = (Long) ((CheckBox) v).getTag();

                if (((CheckBox) v).isChecked())
                {
                    _rxchk0checked = true;
                    //Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    rxListChecked.add(_a);
                    enableTakenSkip(v);
                } else
                {
                    _rxchk0checked = false;
                    rxListChecked.remove(_a);
                    enableTakenSkip(v);
                }

            }
        });

        CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
        _rxCheckBox1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Long _a = (Long) ((CheckBox) v).getTag();
                if (((CheckBox) v).isChecked())
                {
                    _rxchk1checked = true;
                    //Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    rxListChecked.add(_a);
                    enableTakenSkip(v);
                } else
                {
                    _rxchk1checked = false;
                    rxListChecked.remove(_a);
                    enableTakenSkip(v);
                }
            }
        });


        CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
        _rxCheckBox2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Long _a = (Long) ((CheckBox) v).getTag();

                if (((CheckBox) v).isChecked())
                {
                    _rxchk2checked = true;
                    //Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity3", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    rxListChecked.add(_a);
                    enableTakenSkip(v);
                } else
                {
                    _rxchk2checked = false;
                    rxListChecked.remove(_a);
                    enableTakenSkip(v);
                }
            }
        });
    }

    private void enableTakenSkip(View view)
    {

        Button _symptom1 = (Button) findViewById(R.id.btsymp1);
        Button _symptom2 = (Button) findViewById(R.id.btsymp2);
        Button _symptom3 = (Button) findViewById(R.id.btsymp3);
        Button _symptom4 = (Button) findViewById(R.id.btsymp4);
        Button _symptom5 = (Button) findViewById(R.id.btsymp5);
        Button _symptom6 = (Button) findViewById(R.id.btsymp6);
        Button _symptom7 = (Button) findViewById(R.id.btsymp7);
        Button _symptom8 = (Button) findViewById(R.id.btsymp8);

        Context _context = getApplicationContext();
        Button _taken = (Button) findViewById(R.id.rxtaken);


        //if (_rxchk0checked | _rxchk1checked | _rxchk2checked) {
        if (!rxListChecked.isEmpty())
        {

            _symptom1.setVisibility(View.VISIBLE);
            _symptom2.setVisibility(View.VISIBLE);
            _symptom3.setVisibility(View.VISIBLE);
            _symptom4.setVisibility(View.VISIBLE);
            _symptom5.setVisibility(View.VISIBLE);
            _symptom6.setVisibility(View.VISIBLE);
            _symptom7.setVisibility(View.VISIBLE);
            _symptom8.setVisibility(View.VISIBLE);

            //Enable Taken Button
            Drawable _d1 = _context.getResources().getDrawable(R.drawable.takenactionbtn);
            _taken.setBackground(_d1);
            _taken.setClickable(true);

            Log.d("CareClientActivity3", "-- taken button  is enabled --");

        } else
        {
            _symptom1.setVisibility(View.INVISIBLE);
            _symptom2.setVisibility(View.INVISIBLE);
            _symptom3.setVisibility(View.INVISIBLE);
            _symptom4.setVisibility(View.INVISIBLE);
            _symptom5.setVisibility(View.INVISIBLE);
            _symptom6.setVisibility(View.INVISIBLE);
            _symptom7.setVisibility(View.INVISIBLE);
            _symptom8.setVisibility(View.INVISIBLE);

            //Enable Taken Button
            Drawable _d1 = _context.getResources().getDrawable(R.drawable.takenactionbtndisabled);
            _taken.setBackground(_d1);
            _taken.setClickable(false);
            Log.d("CareClientActivity3", "-- taken button  is disabled --");
        }


        /**
         Button _skip = (Button) findViewById(R.id.rxskip);
         _skip.setClickable(true);
         Drawable _d2 = _context.getResources().getDrawable(R.drawable.skipactionbtn);
         _skip.setBackground(_d2);
         **/
    }

    private void onSkipClick()
    {
        Log.d("CareClient", "-- onSkipClick --");

        Button _rxskip = (Button) findViewById(R.id.rxskip);
//        _rxskip.setEnabled(false);
        _rxskip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int _rxTakenStatus = submitCaredPersonData("SKIP");
                Button _rxskip1 = (Button) findViewById(R.id.rxskip);
                Context _context = getApplicationContext();

                if (_rxTakenStatus == RXTAKEN_SUCCESS)
                {
                    //moveTaskToBack(true);
                    //Go to Appln landing screen
                    Intent intent = new Intent(_context, com.phr.ade.activity.CareClientActivity2A.class);
                    intent.putExtra("RX_SYNCH_STATUS", new String("SUCCESS").toCharArray());
                    intent.putExtra("AUTH", new String("AUTH-PASSED").toCharArray());
                    intent.putExtra("RX_SCHDL", true);
                    intent.putExtra("CARED_PERSON", caredPerson.getName().toCharArray());
                    startActivity(intent);
                } else
                {
                    _rxskip1.setText("Close");
                    Intent intent = new Intent(_context, com.phr.ade.activity.CareClientActivity2A.class);
                    intent.putExtra("CARED_PERSON", caredPerson.getName().toCharArray());
                    startActivity(intent);
                }
            }
        });
    }

    private void onTakenClick()
    {
        Log.d("CareClient", "-- onTakenClick --");

        Button _rxtaken = (Button) findViewById(R.id.rxtaken);
//        _rxtaken.setEnabled(false);
        _rxtaken.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int _rxTakenStatus = submitCaredPersonData("TAKEN");

                Log.d(" _rxTakenStatus =  ", _rxTakenStatus + "");
                Context _context = getApplicationContext();
                //Use this only for simulating taken submission
//                int _rxTakenStatus = 0;
                Button _rxtaken1 = (Button) findViewById(R.id.rxtaken);


                if (_rxTakenStatus == RXTAKEN_SUCCESS)
                {
                    resetAllSymptomButtons(false);
                    Intent intent = new Intent(_context, com.phr.ade.activity.CareClientActivity2A.class);
                    intent.putExtra("RX_SYNCH_STATUS", new String("SUCCESS").toCharArray());
                    intent.putExtra("AUTH", new String("AUTH-PASSED").toCharArray());
                    intent.putExtra("RX_SCHDL", true);
                    intent.putExtra("CARED_PERSON", caredPerson.getName().toCharArray());
                    startActivity(intent);
                } else
                {
                    _rxtaken1.setText("Retry");
                }


                //01/27/18 : The above logic is changed as if the connection is not available
                // store in local db

//                resetAllSymptomButtons(false);
//                Intent intent = new Intent(_context, com.phr.ade.activity.CareClientActivity2A.class);
//                intent.putExtra("RX_SYNCH_STATUS", new String("SUCCESS").toCharArray());
//                intent.putExtra("AUTH", new String("AUTH-PASSED").toCharArray());
//                intent.putExtra("RX_SCHDL", true);
//                intent.putExtra("CARED_PERSON", caredPerson.getName().toCharArray());
//                startActivity(intent);
            }
        });
    }

    /**
     * Capture user data and submit to server
     */
    private int submitCaredPersonData(String action)
    {

        String _action = "(ACTION=" + action + ")";
        String _rxTaken = "";
        String _symptom = "";
        String _dataSubmitString = "";
        int _rxtaken_status = RXTAKEN_ERROR;
        String imeiCode = "-";

        /**
         if (_rxchk0checked) {
         CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
         _rxTaken += ((Long) _rxCheckBox0.getTag()).toString() + ",";
         }
         if (_rxchk1checked) {
         CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
         _rxTaken += ((Long) _rxCheckBox1.getTag()).toString() + ",";
         }

         if (_rxchk2checked) {
         CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
         _rxTaken += ((Long) _rxCheckBox2.getTag()).toString() + ",";
         }
         */

        if (!rxListChecked.isEmpty())
        {
            for (Iterator iterator = rxListChecked.iterator(); iterator.hasNext(); )
            {
                _rxTaken += (Long) iterator.next() + ",";
            }
        }


        /**
         * Debug info
         */

        Log.d("submitCaredPersonData : Symptom Button status : ",
                " _sb1Pressed - " + _sb1Pressed +
                        " _sb2Pressed - " + _sb2Pressed +
                        " _sb3Pressed - " + _sb3Pressed +
                        " _sb4Pressed - " + _sb4Pressed +
                        " _sb5Pressed - " + _sb5Pressed +
                        " _sb6Pressed - " + _sb6Pressed +
                        " _sb7Pressed - " + _sb7Pressed +
                        " _sb8Pressed - " + _sb8Pressed
        );


        if (_sb1Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp1);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }

        if (_sb2Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp2);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb3Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp3);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb4Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp4);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb5Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp5);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb6Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp6);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb7Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp7);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }
        if (_sb8Pressed)
        {
            Button _symptomButton = (Button) findViewById(R.id.btsymp8);
            _symptom += ((Long) _symptomButton.getTag()).toString() + ",";
        }

        if (_symptom.equals(""))
        {
            _symptom = "-";
        }

        _dataSubmitString = _action;
        _dataSubmitString += "(RXTAKEN=" + _rxTaken + ")";
        _dataSubmitString += "(SYMPTOMS=" + _symptom + ")";
        //Pass the current date from the mobile
        Calendar _c = Calendar.getInstance();

        // 05/08/2017 : Added the pattern to send the mobile timezone too
        SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm Z");
        String _currDateString = _dateFormat.format(new Date(_c.getTimeInMillis()));
        Log.d("CareClientActivity ---> Current Date ", _currDateString);
        _dataSubmitString += "(CURRDATE=" + _currDateString + ")";


        Log.d("CareClientActivity ---> Data String ", _dataSubmitString);

        String _responseData = null;
        TextView _rxFor = (TextView) findViewById(R.id.rxfor);

        try
        {

            imeiCode = CareClientUtil.readIMEICode(this);
            //_responseData = MCareBridgeConnector.sendCaredPersonRxData("353197050130472",_dataSubmitString );
            _responseData = MCareBridgeConnector.sendCaredPersonRxData(imeiCode, _dataSubmitString);
            //Log.d("CareClientActivity XML -- ", _responseData);
            _rxtaken_status = RXTAKEN_SUCCESS;
        }
        catch (UnknownHostException e)
        {
            Log.e("CareClientActivity3", e.getMessage(), e);
            e.printStackTrace();
            _rxtaken_status = RXTAKEN_CONN_ERROR;
            //01/27/18 - Change this message to "Using local store : Connection error"
            _rxFor.setText("Connection Timeout. Please try again");

            //Invoke local store function

            try
            {
                CaredActionSynch _caredActionSynch = new CaredActionSynch();
                _caredActionSynch.setStatus("CURRENT");
                _caredActionSynch.setState("OPEN");
                _caredActionSynch.setIMEI(imeiCode);
                _caredActionSynch.setCaredResponse(_dataSubmitString);
                addCaredSynchDataToDB(_caredActionSynch, this, _action, _rxTaken, _symptom, _currDateString);
                _rxFor.setText("Using local cache. Please check connection.");
                _rxtaken_status = RXTAKEN_SUCCESS;
            }
            catch (Exception dbe)
            {
                _rxtaken_status = MOBILE_DB_ERR;
                Log.e("CareClientActivity3", dbe.getMessage(), dbe);
                dbe.printStackTrace();
                //_rxSynchStatus = "ERROR";
                _rxFor.setText("Local Database error. Please inform admin - admin@sevha.com");
            }
            //On error rollback
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("CareClientActivity3", e.getMessage(), e);
            _rxFor.setText("Unexpected error. Please inform - admin admin@sevha.com");
        }
        finally
        {
            return _rxtaken_status;
        }
    }


    /**
     * @param caredActionSynch
     * @param context
     * @throws Exception
     */
    private void addCaredSynchDataToDB(CaredActionSynch caredActionSynch, Context context, String action,
                                       String rxTaken, String symptom,
                                       String currDateString) throws Exception
    {
        /**
         * Add the data in mobile table if only the Synch is sucessfull
         */
        // Added the code for local db Synch
        // db Code start
        //1. see if a record with CURRENT status already exist

        CaredActionSynch _careSync = readCaredSynchRecord(context);


        if (_careSync != null)
        {
            /**
             * If the last read record isn't of the same hour. Then change status
             * to 'TOBESYNCD'
             */
            Calendar _c = Calendar.getInstance();
            int _sysHour = _c.get(Calendar.HOUR);

            SimpleDateFormat _sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date _currRecCreateDate = _sdf.parse(_careSync.getCreateDate());
            Calendar _currDate = Calendar.getInstance();
            _currDate.setTime(_currRecCreateDate);
            int _currRecSysHr = _currDate.get(Calendar.HOUR);

            if (_sysHour == _currRecSysHr)
            {
                caredActionSynch.setId(_careSync.getId());
                updateCaredActionRecord(caredActionSynch, context, action, rxTaken, symptom, currDateString);
            } else
            {
                //Make the current record status to "TOBESYNCD"
                _careSync.setStatus("TOBESYNCD");
                updateCaredActionRecordStatus(_careSync, context);
                addCaredActionRecord(caredActionSynch, context, rxTaken);
            }
        } else
        {
            addCaredActionRecord(caredActionSynch, context, rxTaken);
        }
    }


    /**
     * The method updates careSync's status from CURREN TO TOBESYNCD, and adds a new CURRENT status row
     *
     * @param caredActionSynch
     * @param context
     * @param rxTaken
     * @throws Exception
     */
    private void addCaredActionRecord(CaredActionSynch caredActionSynch, Context context, String rxTaken) throws Exception
    {

        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        _careDbAdaptor.insertCaredActionSynchedRow(caredActionSynch);
        //read the CURRENT record from MobileAuthSync;
        AuthSynch _authSynch = _careDbAdaptor.readMobileAuthRecord();
        String _overallRxTaken = _authSynch.getRxConsumed() + rxTaken;
        _authSynch.setRxConsumed(_overallRxTaken);
        _careDbAdaptor.updateMobileSynchedRow(_authSynch);
        _careDbAdaptor.close();
    }


    /**
     * @param action
     * @param rxTaken
     * @param symptom
     * @param currDateString
     * @return
     */
    String buildTakenStr(String action, String rxTaken, String symptom, String currDateString)
    {
        String _dataSubmitString = action;
        _dataSubmitString += "(RXTAKEN=" + rxTaken + ")";
        _dataSubmitString += "(SYMPTOMS=" + symptom + ")";
        _dataSubmitString += "(CURRDATE=" + currDateString + ")";

        return _dataSubmitString;
    }


    /**
     * @param context
     * @return
     * @throws Exception
     */
    private CaredActionSynch readCaredSynchRecord(Context context) throws Exception
    {
        /**
         * Add the data in mobile table if only the Synch is sucessful
         */
        // Added the code for local db Synch
        // db Code start
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        CaredActionSynch _careSync = _careDbAdaptor.readCareSyncRecord();
        _careDbAdaptor.close();

        return _careSync;
    }


    /**
     * @param context
     * @return
     * @throws Exception
     */
    private long updateCaredActionRecord(CaredActionSynch caredActionSynch, Context context, String action,
                                         String rxTaken, String symptom,
                                         String currDateString) throws Exception
    {
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        //read the CURRENT record from MobileAuthSync;
        AuthSynch _authSynch = _careDbAdaptor.readMobileAuthRecord();
        String _overallRxTaken = _authSynch.getRxConsumed() + rxTaken;
        _authSynch.setRxConsumed(_overallRxTaken);

        String _overRxTakenString = buildTakenStr(action, _overallRxTaken, symptom, currDateString);

        _careDbAdaptor.updateMobileSynchedRow(_authSynch);
        caredActionSynch.setCaredResponse(_overRxTakenString);
        long updatedRecCount = _careDbAdaptor.updateCareSynchedRow(caredActionSynch);
        _careDbAdaptor.close();
        return updatedRecCount;
    }


    /**
     * Update the STATUS id careSync
     *
     * @param caredActionSynch
     * @param context
     * @return
     * @throws Exception
     */
    private long updateCaredActionRecordStatus(CaredActionSynch caredActionSynch, Context context) throws Exception
    {
        CareDatabaseAdaptor _careDbAdaptor = new CareDatabaseAdaptor(context);
        _careDbAdaptor.open();
        long updatedRecCount = _careDbAdaptor.updateCareSynchedRow(caredActionSynch);
        _careDbAdaptor.close();
        return updatedRecCount;
    }


    @Override
    protected void onPause()
    {
        super.onPause();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    private void paintScreen(Context context, CaredPerson caredPerson, ArrayList<RxLineDTO> rxLineDTOs)
    {
        Log.d("CareClientActivity3", "--calling paintScreen --");
        //Set the RxFor

//        Button _rxtaken = (Button) findViewById(R.id.rxtaken);
//        _rxtaken.setEnabled(true);

//        Button _rxskip = (Button) findViewById(R.id.rxskip);
//        _rxskip.setEnabled(true);

        if (caredPerson != null)
        {
            TextView _rxFor = (TextView) findViewById(R.id.rxfor);
            _rxFor.setText(caredPerson.getName());
            rxListReceivedSize = rxLineDTOs.size();
            paintRxLines1(rxLineDTOs);
            paintCurrentHealthConditions(context, caredPerson.getPreExistingCondition());
        }

    }

    /**
     * @param context
     */
    private void disableScreen(Context context)
    {
        initRxChart();
        resetAllSymptomButtons(true);
        invisibleRxCheckBoxs();
    }

    /**
     * @param context
     */
    private void paintRxLines(Context context, ArrayList<RxLineDTO> rxLineDTOs)
    {
        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);
        Log.d("CareClientActivity3", "Current Hours --" + _hour);
        int _i = 0;

        if (rxLineDTOs != null)
        {

            for (Iterator iterator = rxLineDTOList.iterator(); iterator.hasNext(); )
            {
                RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
                RxLines _rxLines = _rxLineDTO.getRxLine();
                Log.d("CareClientActivity3", "--Counter--" + _i);
                Log.d("CareClientActivity-->paintRxLines->", _rxLines.getRx() + "----" + _rxLineDTO.getRxTime());

                if (_rxLineDTO.getRxTime() == _hour)
                {
                    if (_i == 0)
                    {
                        TextView _rxDetails_0 = (TextView) findViewById(R.id.rx1);
                        TextView _rxDosage_0 = (TextView) findViewById(R.id.dose1);
                        TextView _rxDosageTime_0 = (TextView) findViewById(R.id.time1);
                        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
                        //_rxCheckBox0.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                        _rxCheckBox0.setTag(new Long(_rxLineDTO.getRxLineId()));
                        _rxCheckBox0.setVisibility(View.VISIBLE);


                        //Set RxDetails
                        _rxDetails_0.setText(_rxLines.getRx());
                        _rxDosage_0.setText(_rxLines.getDosage());
                        _rxDosageTime_0.setText(_rxLineDTO.getRxTime() + " Hrs");

                    }

                    if (_i == 1)
                    {
                        TextView _rxDetails_1 = (TextView) findViewById(R.id.rx2);
                        TextView _rxDosage_1 = (TextView) findViewById(R.id.dose2);
                        TextView _rxDosageTime_1 = (TextView) findViewById(R.id.time2);

                        //Set RxDetails
                        _rxDetails_1.setText(_rxLines.getRx());
                        _rxDosage_1.setText(_rxLines.getDosage());
                        _rxDosageTime_1.setText(_rxLineDTO.getRxTime() + " Hrs");

                        CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
                        //_rxCheckBox1.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                        _rxCheckBox1.setTag(new Long(_rxLineDTO.getRxLineId()));
                        _rxCheckBox1.setVisibility(View.VISIBLE);
                    }

                    if (_i == 2)
                    {
                        TextView _rxDetails_2 = (TextView) findViewById(R.id.rx3);
                        TextView _rxDosage_2 = (TextView) findViewById(R.id.dose3);
                        TextView _rxDosageTime_2 = (TextView) findViewById(R.id.time3);

                        //Set RxDetails
                        _rxDetails_2.setText(_rxLines.getRx());
                        _rxDosage_2.setText(_rxLines.getDosage());
                        _rxDosageTime_2.setText(_rxLineDTO.getRxTime() + " Hrs");

                        CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
                        //_rxCheckBox2.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                        _rxCheckBox2.setTag(new Long(_rxLineDTO.getRxLineId()));
                        _rxCheckBox2.setVisibility(View.VISIBLE);
                    }

                    _i++;
                }
            }
        }
    }

    private void paintRxLines1(List<RxLineDTO> rxLineDTOs)
    {
        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);
        Log.d("CareClientActivity3", "paintRxLines1 - size of rxLineDTOs" + rxLineDTOs.size());
        initRxChart();
        int _i = 0;
        Context _context = getApplicationContext();
        Button _taken = (Button) findViewById(R.id.rxtaken);
        Button _skip = (Button) findViewById(R.id.rxskip);


        //02/08/2017 - adding logic for disable Taken if all the Rx in the scr are taken

        int _allrXTaken = 0;


        //Check to enable or disable Next Button
        Button _next = (Button) findViewById(R.id.nextRx);
        if (rxListReceivedSize <= 3)
        {
            //_next.setVisibility(View.D);
            _next.setClickable(false);
        } else
        {
            //_next.setVisibility(View.VISIBLE);
            _next.setClickable(true);
            Drawable _d1 = _context.getResources().getDrawable(R.drawable.nextactionbtn);
            _next.setBackground(_d1);
            //_next.setBackgroundColor(Color.parseColor("#309060"));
        }


        for (Iterator iterator = rxLineDTOs.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            RxLines _rxLines = _rxLineDTO.getRxLine();

            if (_rxLines == null)
            {
                Log.d("CareClientActivity3------------->", "_rxLines is null");
            }

            Log.d("CareClientActivity3------------->", _rxLines.getRx());

            if (_rxLineDTO.getRxTime() == _hour)
            {
                if (_i == 0)
                {
                    Log.d("CareClientActivity3", "Writing Row # :" + _i);
                    TextView _rxDetails_0 = (TextView) findViewById(R.id.rx1);
                    TextView _rxDosage_0 = (TextView) findViewById(R.id.dose1);
                    TextView _rxDosageTime_0 = (TextView) findViewById(R.id.time1);

                    _rxDetails_0.setTextColor(Color.BLACK);
                    _rxDosage_0.setTextColor(Color.BLACK);
                    _rxDosageTime_0.setTextColor(Color.BLACK);

                    CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
                    //_rxCheckBox0.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                    _rxCheckBox0.setTag(new Long(_rxLineDTO.getRxLineId()));

                    //Log.d("CareClientActivity3", "--checking for checkbox value --" + rxListChecked.contains(_rxLineDTO.getRxLineId()));

                    if (rxListChecked.contains(_rxLineDTO.getRxLineId()))
                    {
                        if (!_rxCheckBox0.isChecked())
                        {
                            _rxCheckBox0.toggle();
                        }
                    }

                    _rxCheckBox0.setVisibility(View.VISIBLE);
                    _rxCheckBox0.setClickable(true);
                    //In this block check if the Rx has already been taken if Yes, do not allow it to clicked again.
                    if (consumedRxList != null & consumedRxList.contains(_rxLineDTO.getRxLineId()))
                    {

                        _rxCheckBox0.setClickable(false);
                        _rxCheckBox0.setVisibility(View.INVISIBLE);

                        _rxDetails_0.setTextColor(Color.LTGRAY);
                        _rxDosage_0.setTextColor(Color.LTGRAY);
                        _rxDosageTime_0.setTextColor(Color.LTGRAY);

                        _allrXTaken++;
                    }

                    //Set RxDetails
                    _rxDetails_0.setText(_rxLines.getRx());
                    _rxDosage_0.setText(_rxLines.getDosage());
                    _rxDosageTime_0.setText(_rxLineDTO.getRxTime() + ":00 HRS");

                }

                if (_i == 1)
                {
                    Log.d("CareClientActivity3", "Writing Row # :" + _i);
                    TextView _rxDetails_1 = (TextView) findViewById(R.id.rx2);
                    TextView _rxDosage_1 = (TextView) findViewById(R.id.dose2);
                    TextView _rxDosageTime_1 = (TextView) findViewById(R.id.time2);

                    _rxDetails_1.setTextColor(Color.BLACK);
                    _rxDosage_1.setTextColor(Color.BLACK);
                    _rxDosageTime_1.setTextColor(Color.BLACK);

                    //Set RxDetails
                    _rxDetails_1.setText(_rxLines.getRx());
                    _rxDosage_1.setText(_rxLines.getDosage());
                    _rxDosageTime_1.setText(_rxLineDTO.getRxTime() + ":00 HRS");

                    CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
                    //_rxCheckBox1.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                    _rxCheckBox1.setTag(new Long(_rxLineDTO.getRxLineId()));


                    if (rxListChecked.contains(_rxLineDTO.getRxLineId()))
                    {
                        if (!_rxCheckBox1.isChecked())
                        {
                            _rxCheckBox1.toggle();
                        }
                    }

                    _rxCheckBox1.setVisibility(View.VISIBLE);
                    _rxCheckBox1.setClickable(true);
                    //In this block check if the Rx has already been taken if Yes, do not allow it to clicked again.
                    if (consumedRxList != null & consumedRxList.contains(_rxLineDTO.getRxLineId()))
                    {
                        _rxCheckBox1.setClickable(false);
                        _rxCheckBox1.setVisibility(View.INVISIBLE);
                        _rxDetails_1.setTextColor(Color.LTGRAY);
                        _rxDosage_1.setTextColor(Color.LTGRAY);
                        _rxDosageTime_1.setTextColor(Color.LTGRAY);

                        _allrXTaken++;
                    }
                }

                if (_i == 2)
                {
                    Log.d("CareClientActivity3", "Writing Row # :" + _i);
                    TextView _rxDetails_2 = (TextView) findViewById(R.id.rx3);
                    TextView _rxDosage_2 = (TextView) findViewById(R.id.dose3);
                    TextView _rxDosageTime_2 = (TextView) findViewById(R.id.time3);

                    _rxDetails_2.setTextColor(Color.BLACK);
                    _rxDosage_2.setTextColor(Color.BLACK);
                    _rxDosageTime_2.setTextColor(Color.BLACK);

                    //Set RxDetails
                    _rxDetails_2.setText(_rxLines.getRx());
                    _rxDosage_2.setText(_rxLines.getDosage());
                    _rxDosageTime_2.setText(_rxLineDTO.getRxTime() + ":00 HRS");

                    CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
                    //_rxCheckBox2.setText(new Long(_rxLineDTO.getRxLineId()).toString());
                    _rxCheckBox2.setTag(new Long(_rxLineDTO.getRxLineId()));


                    if (rxListChecked.contains(_rxLineDTO.getRxLineId()))
                    {
                        if (!_rxCheckBox2.isChecked())
                        {
                            _rxCheckBox2.toggle();
                        }
                    }

                    _rxCheckBox2.setVisibility(View.VISIBLE);
                    _rxCheckBox2.setClickable(true);
                    //In this block check if the Rx has already been taken if Yes, do not allow it to clicked again.
                    if (consumedRxList != null & consumedRxList.contains(_rxLineDTO.getRxLineId()))
                    {
                        _rxCheckBox2.setClickable(false);
                        _rxCheckBox2.setVisibility(View.INVISIBLE);
                        _rxDetails_2.setTextColor(Color.LTGRAY);
                        _rxDosage_2.setTextColor(Color.LTGRAY);
                        _rxDosageTime_2.setTextColor(Color.LTGRAY);

                        _allrXTaken++;
                    }
                }

                _i++;
            }

            if (rxListCurrPointer == 0)
            {
                rxListCurrPointer += 3;
            }

            //02/07/2017 - Check if all the Rx on the scr is taken

            if (_allrXTaken++ == 3)
            {
                _taken.setClickable(false);
                _skip.setText("Close");
                _skip.setClickable(true);
            }
        }
    }

    private void initRxChart()
    {
        Log.d("CareClientActivity3", "Calling initRxChart --");

        TextView _rxDetails_0 = (TextView) findViewById(R.id.rx1);
        TextView _rxDosage_0 = (TextView) findViewById(R.id.dose1);
        TextView _rxDosageTime_0 = (TextView) findViewById(R.id.time1);

        _rxDetails_0.setText("-");
        _rxDosage_0.setText("-");
        _rxDosageTime_0.setText("-" + " Hrs");

        TextView _rxDetails_1 = (TextView) findViewById(R.id.rx2);
        TextView _rxDosage_1 = (TextView) findViewById(R.id.dose2);
        TextView _rxDosageTime_1 = (TextView) findViewById(R.id.time2);

        _rxDetails_1.setText("-");
        _rxDosage_1.setText("-");
        _rxDosageTime_1.setText("-" + " Hrs");


        TextView _rxDetails_2 = (TextView) findViewById(R.id.rx3);
        TextView _rxDosage_2 = (TextView) findViewById(R.id.dose3);
        TextView _rxDosageTime_2 = (TextView) findViewById(R.id.time3);

        _rxDetails_2.setText("-");
        _rxDosage_2.setText("-");
        _rxDosageTime_2.setText("-" + " Hrs");

        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
        CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
        CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);

        _rxCheckBox0.setClickable(false);
        _rxCheckBox1.setClickable(false);
        _rxCheckBox2.setClickable(false);

    }

    /**
     * Reset all the Symptom buttons after initial reset
     *
     * @param disableClick
     */
    private void resetAllSymptomButtons(boolean disableClick)
    {
        /*** Initialize all the Symptom Buttons ***/

        Log.d("CareClientActivity3", "-- calling resetAllSymptomButtons --");

        resetSymptomButton(R.id.btsymp1, _sb1Pressed, disableClick);
        resetSymptomButton(R.id.btsymp2, _sb2Pressed, disableClick);
        resetSymptomButton(R.id.btsymp3, _sb3Pressed, disableClick);
        resetSymptomButton(R.id.btsymp4, _sb4Pressed, disableClick);
        resetSymptomButton(R.id.btsymp5, _sb5Pressed, disableClick);
        resetSymptomButton(R.id.btsymp6, _sb6Pressed, disableClick);
        resetSymptomButton(R.id.btsymp7, _sb7Pressed, disableClick);
        resetSymptomButton(R.id.btsymp8, _sb8Pressed, disableClick);
    }

    /**
     * Reset all the Symptom buttons
     *
     * @param viewId
     * @param disableButton
     */
    private void resetSymptomButton(int viewId, boolean symBtnFlag, boolean disableButton)
    {
        Context _context = getApplicationContext();

        Button _symptom = (Button) findViewById(viewId);
        Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
        _symptom.setBackground(_d);
        symBtnFlag = false;

        if (disableButton)
        {
            _symptom.setClickable(false);
        }
    }

    private void invisibleRxCheckBoxs()
    {
        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
        _rxCheckBox0.setVisibility(View.INVISIBLE);

        CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
        _rxCheckBox1.setVisibility(View.INVISIBLE);

        CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
        _rxCheckBox2.setVisibility(View.INVISIBLE);
    }

    private void resetNavigation()

    {

    }

    public void onClickSymptoms(View view)
    {
        //Button _symptom = (Button) findViewById(view.getId());
        //_symptom.setBackgroundColor(0xFF84BB6C);
        Button _symptom = null;
        Context _context = getApplicationContext();
        Log.d("CareClientActivity3", "onClickSymptoms # :" + view.getId());

        Button _b = (Button) findViewById(view.getId());

        Log.d("CareClientActivity3", " button Clicked # :" + _b.getText());


        Log.d("submitCaredPersonData before : Symptom Button status : ",
                " _sb1Pressed - " + _sb1Pressed + "\n" +
                        " _sb2Pressed - " + _sb2Pressed + "\n" +
                        " _sb3Pressed - " + _sb3Pressed + "\n" +
                        " _sb4Pressed - " + _sb4Pressed + "\n" +
                        " _sb5Pressed - " + _sb5Pressed + "\n" +
                        " _sb6Pressed - " + _sb6Pressed + "\n" +
                        " _sb7Pressed - " + _sb7Pressed + "\n" +
                        " _sb8Pressed - " + _sb8Pressed
        );

        switch (view.getId())
        {

            case R.id.btsymp1:
                _symptom = (Button) findViewById(R.id.btsymp1);
                if (_sb1Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb1Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);
                    _sb1Pressed = true;
                }
                break;

            case R.id.btsymp2:
                _symptom = (Button) findViewById(R.id.btsymp2);
                if (_sb2Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb2Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);
                    _sb2Pressed = true;
                }
                break;

            case R.id.btsymp3:
                _symptom = (Button) findViewById(R.id.btsymp3);
                if (_sb3Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb3Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);
                    _sb3Pressed = true;
                }
                break;

            case R.id.btsymp4:
                _symptom = (Button) findViewById(R.id.btsymp4);
                if (_sb4Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb4Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);
                    _sb4Pressed = true;
                }
                break;

            case R.id.btsymp5:
                _symptom = (Button) findViewById(R.id.btsymp5);
                if (_sb5Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb5Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);

                    _sb5Pressed = true;
                }
                break;

            case R.id.btsymp6:
                _symptom = (Button) findViewById(R.id.btsymp6);
                if (_sb6Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);

                    _sb6Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);

                    _sb6Pressed = true;
                }
                break;

            case R.id.btsymp7:
                _symptom = (Button) findViewById(R.id.btsymp7);
                if (_sb7Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb7Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);

                    _sb7Pressed = true;
                }
                break;

            case R.id.btsymp8:
                _symptom = (Button) findViewById(R.id.btsymp8);
                if (_sb8Pressed)
                {
                    //_symptom.setBackgroundColor(0xffbbbbbb);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtn);
                    _symptom.setBackground(_d);
                    _sb8Pressed = false;
                } else
                {
                    //_symptom.setBackgroundColor(0xFFFFB55D);
                    Drawable _d = _context.getResources().getDrawable(R.drawable.sympactionbtnselect);
                    _symptom.setBackground(_d);

                    _sb8Pressed = true;
                }
                break;
        }

        /**
         * Debug info
         */

        Log.d("submitCaredPersonData after : Symptom Button status : ",
                " _sb1Pressed - " + _sb1Pressed + "\n" +
                        " _sb2Pressed - " + _sb2Pressed + "\n" +
                        " _sb3Pressed - " + _sb3Pressed + "\n" +
                        " _sb4Pressed - " + _sb4Pressed + "\n" +
                        " _sb5Pressed - " + _sb5Pressed + "\n" +
                        " _sb6Pressed - " + _sb6Pressed + "\n" +
                        " _sb7Pressed - " + _sb7Pressed + "\n" +
                        " _sb8Pressed - " + _sb8Pressed
        );

    }

    /**
     * On Click of Emergency Button dial emergency Number
     *
     * @param view
     */
    public void dialEmergency(View view)
    {
        if (emergencyContactNumber != -1)
        {

            String _emergencyNumber = "tel:" + emergencyContactNumber;
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

    private void paintCurrentHealthConditions(Context context, PreExistingCondition preExistingCondition)
    {
        Condition _condition = preExistingCondition.getConditionList().get(0);
        List<Symptoms> _sympList = _condition.getSymptoms();

        Symptoms _symp = _sympList.get(0);
        Button _symptom1 = (Button) findViewById(R.id.btsymp1);
        _symptom1.setText(_symp.getTag());
        _symptom1.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(1);
        Button _symptom2 = (Button) findViewById(R.id.btsymp2);
        _symptom2.setText(_symp.getTag());
        _symptom2.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(2);
        Button _symptom3 = (Button) findViewById(R.id.btsymp3);
        _symptom3.setText(_symp.getTag());
        _symptom3.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(3);
        Button _symptom4 = (Button) findViewById(R.id.btsymp4);
        _symptom4.setText(_symp.getTag());
        _symptom4.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(4);
        Button _symptom5 = (Button) findViewById(R.id.btsymp5);
        _symptom5.setText(_symp.getTag());
        _symptom5.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(5);
        Button _symptom6 = (Button) findViewById(R.id.btsymp6);
        _symptom6.setText(_symp.getTag());
        _symptom6.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(6);
        Button _symptom7 = (Button) findViewById(R.id.btsymp7);
        _symptom7.setText(_symp.getTag());
        _symptom7.setTag(new Long(_symp.getId()));

        _symp = _sympList.get(7);
        Button _symptom8 = (Button) findViewById(R.id.btsymp8);
        _symptom8.setText(_symp.getTag());
        _symptom8.setTag(new Long(_symp.getId()));

    }

    /**
     * Read IMEI code
     */
    private String readIMEICode()
    {
//        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//        String _deviceId = telephonyManager.getDeviceId();
//
//        //Only for testing
////        String _deviceId = "867124022666036";
//        Log.i("CareClientActivity3", _deviceId);
        return CareClientUtil.readIMEICode(this);
    }

    /**
     * @param view
     */
    public void readNext(View view)
    {
        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
        CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
        CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);

        if (_rxCheckBox0.isChecked())
        {
            _rxCheckBox0.toggle();
        }

        if (_rxCheckBox1.isChecked())
        {
            _rxCheckBox1.toggle();
        }

        if (_rxCheckBox2.isChecked())
        {
            _rxCheckBox2.toggle();
        }

        List<RxLineDTO> rxList = readNext(rxLineDTOList, rxListCurrPointer);
        paintRxLines1(rxList);
    }

    private void sendUpdateNotification(String caredPersonName)
    {
        // dj start
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.notify;
        CharSequence tickerText = "Your Rx";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.defaults = Notification.DEFAULT_SOUND;

        Context kontext = getApplicationContext();
        CharSequence contentTitle = "My notification";
        CharSequence contentText = "Rx reminder for " + caredPersonName;

        Intent notificationIntent = new Intent(this, com.phr.ade.service.ClientService1.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        notification.setLatestEventInfo(kontext, contentTitle, contentText,
                contentIntent);


        int notificationId = (int) Math.random();

        mNotificationManager.notify(notificationId, notification);
        // dj end
    }

    private void sendRxMissedNotification(String caredPersonName)
    {
        // dj start
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.notify;
        CharSequence tickerText = "Your Rx";
        long when = System.currentTimeMillis();

        Notification notification = new Notification(icon, tickerText, when);
        notification.defaults = Notification.DEFAULT_SOUND;

        Context kontext = getApplicationContext();
        CharSequence contentTitle = "My notification";
        CharSequence contentText = "Rx reminder for " + caredPersonName;

        //Intent notificationIntent = new Intent(this, com.phr.ade.service.ClientService.class);
        //PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        //        notificationIntent, 0);

//        notification.setLatestEventInfo(kontext, contentTitle, contentText,
//                contentIntent);


        int notificationId = (int) Math.random();

        mNotificationManager.notify(notificationId, notification);
        // dj end
    }

    private class LoginAsyncTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {

            Log.d("LoginAsyncTask.onPreExecute", "----");

            progressDialog = new ProgressDialog(CareClientActivity3.this);
            progressDialog.setMessage("Please wait...");
            progressDialog.show();
            super.onPreExecute();
        }

        protected Void doInBackground(Void... args)
        {
            // Parsse response data
            Log.d("LoginAsyncTask.doInBackground", "----");
            String imeiCode = readIMEICode();

            try
            {
                responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
                Log.d("LoginAsyncTask.responseData", "----> " + responseData);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

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
                        Thread.sleep(2000);
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