package com.phr.ade.connector;

import android.os.StrictMode;
import android.util.Log;

import com.phr.ade.util.CareClientUtil;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

/**
 * Created by deejay on 10/27/2014.
 */
public class MCareBridgeConnector
{

    private static final String TAG = "MCareBridgeConnector";
    //private static final String BASE_URL = "http://caregiver.mcarebridge.com/health/";
//    private static final String BASE_URL = "https://mcarebridge.appspot.com/health/";
    private static final String BASE_URL = "https://sevha.com/health/";
    private static final String MCB_AUTH_SERVER_LINK = "caredPersonMobileSecurity";
    private static final String MCB_DATA_EXCHANGE = "caredPersonMobileRxExchange";
    private static String rxConsumed;
    private static boolean smsSent = false;


    /**
     * Use this method to Synch using IMEI code
     *
     * @param imeiCode
     * @return
     * @throws Exception
     */
    public static String synchMobileUsingIMEI(String imeiCode)
    throws Exception
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpResponse response = null;
        String _data = null;

        // Get Timesheet snapshot from SqlLite
        String synchServerDataString = null;

        HttpClient client = new DefaultHttpClient();

        int timeout = 5; // seconds
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout * 2000); // http.connection.timeout
        HttpConnectionParams.setSoTimeout(httpParams, timeout * 2000); // http.socket.timeout

        String _synchServerData = MCB_AUTH_SERVER_LINK;
        String _webAppURL = BASE_URL + MCB_AUTH_SERVER_LINK;
        HttpPost httppost = new HttpPost(_webAppURL);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
        nameValuePairs.add(new BasicNameValuePair("imeiCode", imeiCode.trim()));

        Calendar _c = Calendar.getInstance();
        SimpleDateFormat _dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String _currDateString = _dateFormat.format(new Date(_c.getTimeInMillis()));
        Log.d("MCareBridgeConnector ---> Current Date ", _currDateString);
        nameValuePairs.add(new BasicNameValuePair("currdate", _currDateString));

        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));


        response = client.execute(httppost);
        Header[] headers = response.getHeaders("AUTH_MSG");

        Log.i(TAG,
                "synchMobileUsingIMEI ---------------------->"
                        + headers[0].getValue());

//        if (headers[0].getValue().equalsIgnoreCase("MOBILE_SERVER_SYSTEM_ERR"))
//        {
//            throw new Exception("SYNCH_ERR");
//        }
        String _authStr = extractAuthMsg(headers[0].getValue());
        Log.i(TAG,
                "_authStr ---------------------->"
                        + _authStr);

        if (_authStr.equalsIgnoreCase("AUTH-SUCCESS"))
        {
            synchServerDataString = readHTTPResponse(client, httppost, response);
            smsSent = false;
            _data = extractRxData(synchServerDataString);
            Log.i(TAG,
                    "_mobileResponse ---------------------->" + _data);
        } else if (_authStr.equalsIgnoreCase("AUTH-FAILED"))
        {
            _data = "AUTH-FAILED";
        }

        return _data;
    }


    /**
     * @param imeiCode
     * @return
     * @throws Exception
     */
    public static String sendCaredPersonRxData(String imeiCode, String rxData)
    throws Exception
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpResponse response = null;
        String _data = null;

        // Get Timesheet snapshot from SqlLite
        String synchServerDataString = null;

        HttpClient client = new DefaultHttpClient();

        int timeout = 5; // seconds
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout * 1000); // http.connection.timeout
        HttpConnectionParams.setSoTimeout(httpParams, timeout * 1000); // http.socket.timeout

        String _synchServerData = MCB_AUTH_SERVER_LINK;
        String _webAppURL = BASE_URL + MCB_DATA_EXCHANGE;
        HttpPost httppost = new HttpPost(_webAppURL);
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        nameValuePairs.add(new BasicNameValuePair("imeiCode", imeiCode.trim()));
        nameValuePairs.add(new BasicNameValuePair("rxData", rxData));


        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        response = client.execute(httppost);
        Header[] headers = response.getHeaders("AUTH_MSG");

        Log.i(TAG,
                "synchMobileUsingIMEI ---------------------->"
                        + headers[0].getValue());

//        if (headers[0].getValue().equalsIgnoreCase("MOBILE_SERVER_SYSTEM_ERR"))
//        {
//            throw new Exception("SYNCH_ERR");
//        }
        String _authStr = extractAuthMsg(headers[0].getValue());
        Log.i(TAG,
                "_authStr ---------------------->"
                        + _authStr);

        if (_authStr.equalsIgnoreCase("AUTH-SUCCESS"))
        {
            synchServerDataString = readHTTPResponse(client, httppost, response);
            Log.i(TAG,
                    "_mobileResponse ---------------------->"
                            + _data);
        }

        return _data;
    }

    /**
     * @param client
     * @param httppost
     * @param response
     * @throws IOException
     */
    private static String readHTTPResponse(HttpClient client,
                                           HttpPost httppost, HttpResponse response) throws IOException
    {
        // Get hold of the response entity
        HttpEntity entity = response.getEntity();
        String responseString = null;

        // If the response does not enclose an entity, there is no need
        // to worry about connection release
        if (entity != null)
        {

            InputStream instream = null;

            try
            {
                instream = entity.getContent();

                GZIPInputStream zis = new GZIPInputStream(
                        new BufferedInputStream(instream));

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(zis));
                // do something useful with the response
                responseString = reader.readLine();
            }
            catch (IOException ex)
            {

                // In case of an IOException the connection will be released
                // back to the connection manager automatically
                throw ex;

            }
            catch (RuntimeException ex)
            {

                // In case of an unexpected exception you may want to abort
                // the HTTP request in order to shut down the underlying
                // connection and release it back to the connection manager.
                httppost.abort();
                throw ex;

            }
            finally
            {

                // Closing the input stream will trigger connection release
                instream.close();
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                client.getConnectionManager().shutdown();
            }
        }

        return responseString;
    }


    /**
     * @param serverData
     * @return
     */
    private static String extractRxData(String serverData)
    {

        Log.d("MCareBridgeConnector", "--data received --" + serverData);

        StringTokenizer _st = new StringTokenizer(serverData, "()");
        String _envelope = (String) _st.nextElement();
        String _authMsg = (String) _st.nextElement();
        String _data = (String) _st.nextElement();
        String _rxconsumed = (String) _st.nextElement();
        String _rxSkipped = (String) _st.nextElement();
        String _caredPersonName = (String) _st.nextElement();

        Log.d("MCareBridgeConnector", "-- _rxconsumed --" + _rxconsumed);
        Log.d("MCareBridgeConnector", "-- _rxskipped --" + _rxSkipped);
        Log.d("MCareBridgeConnector", "-- _caredPersonName --" + _caredPersonName);

        String _rxData = _data.substring(_data.indexOf(":") + 1,
                _data.length());

        String _rxconsumedExtract = _rxconsumed.substring(_rxconsumed.indexOf(":") + 1,
                _rxconsumed.length());
        setRxConsumed(_rxconsumedExtract);

        _rxSkipped = _rxSkipped.substring(_rxSkipped.indexOf(":") + 1, _rxSkipped.length());

        _caredPersonName = _caredPersonName.substring(_caredPersonName.indexOf(":") + 1, _caredPersonName.length());

        Log.d("MCareBridgeConnector", "-- _rxskipped after substring --" + _rxSkipped);

        if (!_rxSkipped.equals("-"))
        {
            Hashtable _cgRxMap = mapRxSkipped(_rxSkipped);
            sendSMSForRxSkipped(_cgRxMap, _caredPersonName);
        }
        return _rxData;
    }


    /**
     * @param cgRxMap
     */
    private static void sendSMSForRxSkipped(Hashtable cgRxMap, String caredPersonName)
    {

        Set _keys = cgRxMap.keySet();
        Calendar _c = Calendar.getInstance();
        int _currentHour = _c.get(Calendar.HOUR_OF_DAY);

        if (_currentHour == 0)
        {
            _currentHour = 23;
        }
        {
            --_currentHour;
        }

        for (Iterator iterator = _keys.iterator(); iterator.hasNext(); )
        {
            String _message = "Dear ";
            String _cgNameCell = (String) iterator.next();
            String _cgName = _cgNameCell.substring(0, _cgNameCell.indexOf(":"));
            String _cgCell = _cgNameCell.substring(_cgNameCell.indexOf(":") + 1, _cgNameCell.length());

            _message += _cgName + ",";
            _message += caredPersonName + " has missed the following Rx - ";

            List _rxList = (List) cgRxMap.get(_cgNameCell);

            for (Iterator iterator1 = _rxList.iterator(); iterator1.hasNext(); )
            {
                String _rxIdName = (String) iterator1.next();
                _message += _rxIdName.substring(_rxIdName.indexOf(":") + 1, _rxIdName.length()) + " ";
            }

            _message += "scheduled at " + _currentHour + " Hrs";

            //@todo : Fix  SMS module to use Whatapp / email too
            if (!smsSent)
            {
                // 04/09 - Stopping SMS module for sometime.
                CareClientUtil.sendSMS(_cgCell, _message);
                smsSent = true;
            }
        }
    }


    /**
     * Extract Location and Shift data from the server data
     *
     * @param serverData
     * @return
     */
    private static String extractAuthMsg(String serverData)
    {
        StringTokenizer _st = new StringTokenizer(serverData, "()");
        String _authMsg = (String) _st.nextElement();
        String _authStr = _authMsg.substring(_authMsg.indexOf(":") + 1,
                _authMsg.length());
        return _authStr;
    }

    public static String getRxConsumed()
    {
        return rxConsumed;
    }

    private static void setRxConsumed(String rxConsumed)
    {

        String _rxconsumed = rxConsumed.substring(rxConsumed.indexOf(":") + 1,
                rxConsumed.length());
        MCareBridgeConnector.rxConsumed = _rxconsumed;
    }


    /**
     * Extract the string received in the format
     * ([CGNAME1:CGCELL1][RXID1:RXNAME1][RXID2:RXNAME2])([CGNAME2:CGCELL2][RXID1:RXNAME1][RXID2:RXNAME2])
     *
     * @param rxskippedExtract
     */
    private static Hashtable mapRxSkipped(String rxskippedExtract)
    {

        Log.d("MCareBridgeConnector", "-- reading message  --" + rxskippedExtract);

        StringTokenizer _st = new StringTokenizer(rxskippedExtract, "{}");
        Hashtable<String, List> _h = new Hashtable<String, List>();

        while (_st.hasMoreTokens())
        {
            ArrayList<String> _rxList = new ArrayList<String>();
            String _cgAndRxString = (String) _st.nextElement();

            Log.d("MCareBridgeConnector", "-- _cgAndRxString --" + _cgAndRxString);

            StringTokenizer _st1 = new StringTokenizer(_cgAndRxString, "[]");
            String _id = (String) _st1.nextElement();
            Log.d("MCareBridgeConnector", "-- _id --" + _id);

            while (_st1.hasMoreTokens())
            {

                String _rxIdName = (String) _st1.nextElement();

                Log.d("MCareBridgeConnector", "-- _value  --" + _rxIdName);

                _rxList.add(_rxIdName);
            }

            _h.put(_id, _rxList);
        }

        return _h;
    }
}
