package com.phr.ade.activity;


import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.phr.ade.connector.CareXMLReader;
import com.phr.ade.connector.MCareBridgeConnector;
import com.phr.ade.dto.RxLineDTO;
import com.phr.ade.xmlbinding.CaredPerson;
import com.phr.ade.xmlbinding.Condition;
import com.phr.ade.xmlbinding.PreExistingCondition;
import com.phr.ade.xmlbinding.RxLines;
import com.phr.ade.xmlbinding.Symptoms;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;


public class CareClientActivity extends Activity implements View.OnClickListener
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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d("CareClientActivity1", "--calling onCreate --");

        setContentView(R.layout.activity_care_client);
        addRxCheckBoxLister();
        onTakenClick();
        onSkipClick();

        //findViewById(R.id.rxtaken).setOnClickListener(this);


        //setUpAlarm(_rxLineDTOList);

        if (!alarmSet)
        {
            Calendar cur_cal = Calendar.getInstance();
            cur_cal.setTimeInMillis(System.currentTimeMillis());
            cur_cal.set(Calendar.HOUR_OF_DAY, 00);
            cur_cal.set(Calendar.MINUTE, 00);
            cur_cal.set(Calendar.SECOND, 0);

            Log.d("CareClientActivity1", "Calender Set time:" + cur_cal.getTime());
            Intent intent = new Intent(this, com.phr.ade.service.ClientService.class);
            Log.d("CareClientActivity1", "Intent created");

            PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);

            AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //Wake up after every 60 secs
            alarm_manager.setRepeating(AlarmManager.RTC_WAKEUP,
                    cur_cal.getTimeInMillis(), 3600 * 1000, pi);
            Log.d("CareClientActivity1", "alarm manager set");
            alarmSet = true;
        }

    }


    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("CareClientActivity1", "--onStart--");

        //@todo : Synch with server and get the latest XML data

        Intent _intent = getIntent();

        String _responseData = null;

        try
        {

            String imeiCode = readIMEICode();
            //_responseData = MCareBridgeConnector.synchMobileUsingIMEI("353197050130472");
            _responseData = MCareBridgeConnector.synchMobileUsingIMEI(imeiCode);
            Log.d("CareClientActivity XML -- ", _responseData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("CareClientActivity1", e.getMessage(), e);
        }

        //char[] _xmlData = _intent.getCharArrayExtra("XML_DATA");

        String _a = "ERR:NO DATA PASSED BY INTENT";

        if (_responseData != null)
        {
            //_a = new String(_xmlData);
            //Log.d("XML Received--", _a);
            _a = _responseData;
            CaredPerson _caredPerson = CareXMLReader.bindXML(_responseData);
            ArrayList<RxLineDTO> _rxLineDTOList = CareXMLReader.extractRxTime(_caredPerson);
            paintScreen(this, _caredPerson, _rxLineDTOList);
        } else
        {
            TextView _rxFor = (TextView) findViewById(R.id.rxfor);
            TextView _rxFor1 = (TextView) findViewById(R.id.rxfor1);
            _rxFor.setText("Cared Person Not Found");
            _rxFor.setTextColor(Color.RED);
            _rxFor1.setText("-");
            _rxFor1.setTextColor(Color.RED);
        }


        Log.d("CareClientActivity onStart--", _a);
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
        Log.d("CareClientActivity1", "-- checkBoxClicked Method --");

        CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
        _rxCheckBox0.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (((CheckBox) v).isChecked())
                {
                    _rxchk0checked = true;
                    //Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    enableTakenSkip(v);
                } else
                {
                    _rxchk0checked = false;
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
                if (((CheckBox) v).isChecked())
                {
                    _rxchk1checked = true;
                    //Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    enableTakenSkip(v);
                } else
                {
                    _rxchk1checked = false;
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
                if (((CheckBox) v).isChecked())
                {
                    _rxchk2checked = true;
                    //Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getText());
                    Log.d("CareClientActivity1", "-- checkBoxChecked --" + ((CheckBox) v).getTag());
                    enableTakenSkip(v);
                } else
                {
                    _rxchk2checked = false;
                    enableTakenSkip(v);
                }
            }
        });
    }


    private void enableTakenSkip(View view)
    {

        int _takenColor = 0xffbbbbbb;
        int _skipColor = 0xffbbbbbb;
        Button _symptom1 = (Button) findViewById(R.id.btsymp1);
        Button _symptom2 = (Button) findViewById(R.id.btsymp2);
        Button _symptom3 = (Button) findViewById(R.id.btsymp3);
        Button _symptom4 = (Button) findViewById(R.id.btsymp4);
        Button _symptom5 = (Button) findViewById(R.id.btsymp5);
        Button _symptom6 = (Button) findViewById(R.id.btsymp6);
        Button _symptom7 = (Button) findViewById(R.id.btsymp7);
        Button _symptom8 = (Button) findViewById(R.id.btsymp8);

        if (_rxchk0checked | _rxchk1checked | _rxchk2checked)
        {
            _takenColor = 0xffacc875;
            _skipColor = 0xfffff3a4;

            _symptom1.setVisibility(View.VISIBLE);
            _symptom2.setVisibility(View.VISIBLE);
            _symptom3.setVisibility(View.VISIBLE);
            _symptom4.setVisibility(View.VISIBLE);
            _symptom5.setVisibility(View.VISIBLE);
            _symptom6.setVisibility(View.VISIBLE);
            _symptom7.setVisibility(View.VISIBLE);
            _symptom8.setVisibility(View.VISIBLE);
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
        }

        //Enable Taken and Skip Button
        Button _taken = (Button) findViewById(R.id.rxtaken);
        _taken.setClickable(true);
        _taken.setBackgroundColor(_takenColor);

        Button _skip = (Button) findViewById(R.id.rxskip);
        _skip.setClickable(true);
        _skip.setBackgroundColor(_skipColor);
    }


    private void onSkipClick()
    {
        Log.d("CareClient", "-- onSkipClick --");

        Button _rxskip = (Button) findViewById(R.id.rxskip);
        _rxskip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitCaredPersonData("SKIP");
                moveTaskToBack(true);
            }
        });
    }


    private void onTakenClick()
    {
        Log.d("CareClient", "-- onTakenClick --");

        Button _rxtaken = (Button) findViewById(R.id.rxtaken);
        _rxtaken.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                submitCaredPersonData("TAKEN");
                moveTaskToBack(true);
            }
        });
    }


    /**
     * Capture user data and submit to server
     */
    private void submitCaredPersonData(String action)
    {
        String _action = "(ACTION=" + action + ")";
        String _rxTaken = "";
        String _symptom = "";
        String _dataSubmitString = "";

        if (_rxchk0checked)
        {
            CheckBox _rxCheckBox0 = (CheckBox) findViewById(R.id.rxcheck0);
            _rxTaken += ((Long) _rxCheckBox0.getTag()).toString() + ",";
        }
        if (_rxchk1checked)
        {
            CheckBox _rxCheckBox1 = (CheckBox) findViewById(R.id.rxcheck1);
            _rxTaken += ((Long) _rxCheckBox1.getTag()).toString() + ",";
        }

        if (_rxchk2checked)
        {
            CheckBox _rxCheckBox2 = (CheckBox) findViewById(R.id.rxcheck2);
            _rxTaken += ((Long) _rxCheckBox2.getTag()).toString() + ",";
        }

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

        Log.d("CareClientActivity ---> Data String ", _dataSubmitString);

        String _responseData = null;

        try
        {

            String imeiCode = readIMEICode();
            //_responseData = MCareBridgeConnector.sendCaredPersonRxData("353197050130472",_dataSubmitString );
            _responseData = MCareBridgeConnector.sendCaredPersonRxData(imeiCode, _dataSubmitString);
            Log.d("CareClientActivity XML -- ", _responseData);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e("CareClientActivity1", e.getMessage(), e);
        }
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
        Log.d("CareClientActivity1", "--calling paintScreen --");
        //Set the RxFor
        TextView _rxFor = (TextView) findViewById(R.id.rxfor);
        _rxFor.setText(caredPerson.getName());

        paintRxLines(context, rxLineDTOs);

        paintCurrentHealthConditions(context, caredPerson.getPreExistingCondition());


    }


    /**
     * @param context
     * @param rxLineDTOs
     */
    private void paintRxLines(Context context, ArrayList<RxLineDTO> rxLineDTOs)
    {
        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);
        Log.d("CareClientActivity1", "Current Hours --" + _hour);
        int _i = 0;

        for (Iterator iterator = rxLineDTOs.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            RxLines _rxLines = _rxLineDTO.getRxLine();
            Log.d("CareClientActivity1", "--Counter--" + _i);
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


    public void onClickSymptoms(View view)
    {
        //Button _symptom = (Button) findViewById(view.getId());
        //_symptom.setBackgroundColor(0xFF84BB6C);
        Button _symptom = null;

        switch (view.getId())
        {

            case R.id.btsymp1:
                _symptom = (Button) findViewById(R.id.btsymp1);
                if (_sb1Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb1Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb1Pressed = true;
                }
                break;

            case R.id.btsymp2:
                _symptom = (Button) findViewById(R.id.btsymp2);
                if (_sb2Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb2Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb2Pressed = true;
                }
                break;

            case R.id.btsymp3:
                _symptom = (Button) findViewById(R.id.btsymp3);
                if (_sb3Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb3Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb3Pressed = true;
                }
                break;

            case R.id.btsymp4:
                _symptom = (Button) findViewById(R.id.btsymp4);
                if (_sb4Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb4Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb4Pressed = true;
                }
                break;

            case R.id.btsymp5:
                _symptom = (Button) findViewById(R.id.btsymp5);
                if (_sb5Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb5Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb5Pressed = true;
                }
                break;

            case R.id.btsymp6:
                _symptom = (Button) findViewById(R.id.btsymp6);
                if (_sb6Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb6Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb6Pressed = true;
                }
                break;

            case R.id.btsymp7:
                _symptom = (Button) findViewById(R.id.btsymp7);
                if (_sb7Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb7Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb7Pressed = true;
                }
                break;

            case R.id.btsymp8:
                _symptom = (Button) findViewById(R.id.btsymp8);
                if (_sb8Pressed)
                {
                    _symptom.setBackgroundColor(0xffbbbbbb);
                    _sb8Pressed = false;
                } else
                {
                    _symptom.setBackgroundColor(0xFFFFB55D);
                    _sb8Pressed = true;
                }
                break;
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
     *
     */
    private void setUpAlarm(ArrayList<RxLineDTO> rxLineDTOs)
    {

        Log.d("CareClientActivity1", "--calling setUpAlarm --");

        Calendar _c = Calendar.getInstance();
        int _hour = _c.get(Calendar.HOUR_OF_DAY);
        Log.d("CareClientActivity1", "Current Hours --" + _hour);

        for (Iterator iterator = rxLineDTOs.iterator(); iterator.hasNext(); )
        {
            RxLineDTO _rxLineDTO = (RxLineDTO) iterator.next();
            if (_rxLineDTO.getRxTime() >= _hour)
            {
                Log.d("CareClientActivity1", "Setting up Alarm" + _rxLineDTO.getRxTime() + " -- "
                        + _rxLineDTO.getRxLineId()
                        + " -- "
                        + _rxLineDTO.getRxLine().getRx());

                Intent intent = new Intent(this, com.phr.ade.service.ClientService.class);
                intent.setData(Uri.parse("myalarms://" + _rxLineDTO.getRxLineId()));
                PendingIntent pi = PendingIntent.getService(this, 0, intent, 0);
                AlarmManager alarm_manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                _c.set(Calendar.HOUR_OF_DAY, _rxLineDTO.getRxTime());
                _c.set(Calendar.MINUTE, 00);
                long when = _c.getTimeInMillis();
                //remind after 5 mins
                alarm_manager.setInexactRepeating(AlarmManager.RTC_WAKEUP, when, (300 * 1000), pi);
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
