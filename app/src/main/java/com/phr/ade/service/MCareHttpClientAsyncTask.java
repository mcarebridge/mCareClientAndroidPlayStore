package com.phr.ade.service;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.phr.ade.activity.R;

import java.util.HashMap;

/**
 * This call connects with mCare server Async gets the data
 */

public class MCareHttpClientAsyncTask extends AsyncTask<String, Void, HashMap>
{

    private Context mContext;
    private OnTaskDoneListener onTaskDoneListener;
    private HashMap mCareKeyValue;
    private ProgressDialog progressDialog;
    private boolean invokedinBackground;


    public MCareHttpClientAsyncTask(Context context, OnTaskDoneListener onTaskDoneListener, boolean invokedinBackground)
    {
        Log.d("MCareHttpClientAsyncTask", "-- Constructor --");

        this.mContext = context;
        this.onTaskDoneListener = onTaskDoneListener;
        this.progressDialog = new ProgressDialog(context);
        this.invokedinBackground = invokedinBackground;
    }

    @Override
    protected void onPreExecute()
    {

        Log.d("MCareHttpClientAsyncTask.onPreExecute", "----");
        if (!invokedinBackground)
        {
            String _msg = mContext.getResources().getString(R.string.msgRxSynchUp);
            progressDialog.setMessage(_msg);
            progressDialog.show();
            super.onPreExecute();
        }
    }


    /**
     * Initiate Server Connection and return server response to OnTaskListener
     *
     * @param params
     * @return
     */
    @Override
    protected HashMap doInBackground(String... params)
    {

        Log.d("MCareHttpClientAsyncTask", "-- doInBackground --");

        String _responseData = "--- RESPONSE STRING FROM MCareHttpClientAsyncTask.doInBackground --";

        MCareHTTPService _mCareHttpService = new MCareHTTPService();
        HashMap<String, String> mCareKeyValue = _mCareHttpService.onStart(this.mContext);


        return mCareKeyValue;
    }


    /**
     * Clean up of resources.
     *
     * @param s
     */
    @Override
    protected void onPostExecute(HashMap s)
    {
        Log.d("CareClientActivity2A", "-- onPostExecute --");
        super.onPostExecute(s);

        if (onTaskDoneListener != null && s != null)
        {
            onTaskDoneListener.onTaskDone(s);
        } else
        {
            onTaskDoneListener.onError();
        }

        this.progressDialog.dismiss();
    }

}
