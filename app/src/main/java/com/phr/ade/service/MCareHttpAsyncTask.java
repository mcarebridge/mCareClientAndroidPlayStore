package com.phr.ade.service;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by dheerajs on 4/9/2017.
 */

public class MCareHttpAsyncTask extends AsyncTask<String, Void, HashMap>
{

    private Context mContext;
    private OnTaskDoneListener onTaskDoneListener;
    private String urlStr = "";


    public MCareHttpAsyncTask(Context context, String url, OnTaskDoneListener onTaskDoneListener)
    {
        this.mContext = context;
        this.urlStr = url;
        this.onTaskDoneListener = onTaskDoneListener;
    }


    @Override
    protected HashMap doInBackground(String... params)
    {

        String _responseString = null;
        String imeiCode = "867124022666036";
        HttpURLConnection httpConnection = null;

        try
        {

            URL mUrl = new URL(urlStr);
            httpConnection = (HttpURLConnection) mUrl.openConnection();
            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Content-length", "0");
            httpConnection.setUseCaches(false);
            httpConnection.setAllowUserInteraction(false);
            httpConnection.setConnectTimeout(100000);
            httpConnection.setReadTimeout(100000);

            StringBuilder postData = new StringBuilder();
            postData.append("imeiCode=" + imeiCode);
            byte[] postDataBytes = postData.toString().getBytes("UTF-8");

            httpConnection.getOutputStream().write(postDataBytes);

            httpConnection.connect();

            int responseCode = httpConnection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK)
            {
                BufferedReader br = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null)
                {
                    sb.append(line + "\n");
                }
                br.close();
                _responseString = sb.toString();
            }

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            httpConnection.disconnect();
            return null;
        }
    }

    @Override
    protected void onPostExecute(HashMap s)
    {
        super.onPostExecute(s);

        if (onTaskDoneListener != null && s != null)
        {
            onTaskDoneListener.onTaskDone(s);
        } else
            onTaskDoneListener.onError();
    }

}
