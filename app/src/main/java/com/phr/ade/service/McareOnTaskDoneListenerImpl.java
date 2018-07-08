package com.phr.ade.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.phr.ade.activity.CareClientActivity2A;

import java.util.HashMap;

/**
 * Created by dheerajs on 4/9/2017.
 */

public class McareOnTaskDoneListenerImpl implements OnTaskDoneListener
{

    private Context context;

    public McareOnTaskDoneListenerImpl(Context context)
    {

        this.context = context;
    }


    @Override
    public void onTaskDone(HashMap mCareKeyValue)
    {

        Log.d("McareOnTaskDoneListenerImpl", "-- onTaskDone --");

        String _careedPerson = (String) mCareKeyValue.get("CARED_PERSON");
        String _rxSynchStatus = (String) mCareKeyValue.get("RX_SYNCH_STATUS");
        String _auth = (String) mCareKeyValue.get("AUTH");
        Boolean _rxSchdl = new Boolean((String) mCareKeyValue.get("RX_SCHDL"));
        String _xmlData = (String) mCareKeyValue.get("XML_DATA");
        Boolean _serviceCall = new Boolean((String) mCareKeyValue.get("SERVICE_CALL"));

//        Log.d("McareOnTaskDoneListenerImpl -- > " , _xmlData);

        ComponentName cn = new ComponentName(this.context, com.phr.ade.activity.CareClientActivity2A.class);
        Intent intent = new Intent(context, CareClientActivity2A.class);
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (_auth.equalsIgnoreCase("AUTH-PASSED"))
        {
            intent.putExtra("CARED_PERSON", _careedPerson.toCharArray());
            intent.putExtra("XML_DATA", _xmlData.toCharArray());
        }

        intent.putExtra("RX_SYNCH_STATUS", _rxSynchStatus.toCharArray());
        intent.putExtra("AUTH", _auth.toCharArray());
        intent.putExtra("RX_SCHDL", _rxSchdl);

        intent.putExtra("SERVICE_CALL", _serviceCall);

        context.startActivity(intent);
    }

    @Override
    public void onError()
    {
        Log.e("McareOnTaskDoneListenerImpl", "-- onTaskDone --");
    }
}
