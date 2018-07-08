package com.phr.ade.connector;

import android.util.Log;
import android.util.Xml;

import com.phr.ade.dto.RxLineDTO;
import com.phr.ade.xmlbinding.CareGiver;
import com.phr.ade.xmlbinding.CaredPerson;
import com.phr.ade.xmlbinding.Condition;
import com.phr.ade.xmlbinding.EmergencyResponse;
import com.phr.ade.xmlbinding.Physician;
import com.phr.ade.xmlbinding.PreExistingCondition;
import com.phr.ade.xmlbinding.Provider;
import com.phr.ade.xmlbinding.RxLines;
import com.phr.ade.xmlbinding.RxPrescribed;
import com.phr.ade.xmlbinding.Symptoms;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by deejay on 9/24/2014.
 */
public class CareXMLReader
{

    private static final String ns = null;
    private static String elementPointer = null;


    public static CaredPerson bindXML(String xmlData)
    {

        //Log.d("CareXMLReader", "printing _xmlData -->" + xmlData);

        InputStream in = null;
        Reader _reader = null;
        CaredPerson _caredPerson = null;

        try
        {

            in = new ByteArrayInputStream(xmlData.getBytes());
            //_reader = new StringReader(XMLData.xmlString);
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser parser = Xml.newPullParser();
            //XmlPullParser parser = factory.newPullParser();
            //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, "utf-8");
            //parser.setInput(_reader);
            _caredPerson = readCaredXML(parser);

            Log.d("CareXMLReader", "printing _caredPerson -->" + _caredPerson);

            PreExistingCondition _preCond = _caredPerson.getPreExistingCondition();
            //Log.d("CareXMLReader", "_preCond -->" + _preCond);


            ArrayList<RxLineDTO> _rxLineDTOList = extractRxTime(_caredPerson);

        }
        catch (Exception e)
        {
            Log.e("CareXMLReader", "Err in reading XML -->", e);
        }
        finally
        {
            try
            {
                in.close();
                //_reader.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return _caredPerson;
    }

    /**
     * @param caredPerson
     */
    public static ArrayList<RxLineDTO> extractRxTime(CaredPerson caredPerson)
    {

        List<RxPrescribed> _rxPresList = caredPerson.getRxPrescribedList();
        ArrayList<RxLines> _rxLines = new ArrayList<RxLines>();
        ArrayList<RxLineDTO> _rxLineDTOList = new ArrayList<RxLineDTO>();

        for (Iterator iterator = _rxPresList.iterator(); iterator.hasNext(); )
        {
            RxPrescribed rxPrescribed = (RxPrescribed) iterator.next();
            //Log.d("CareXMLReader-->RxPrescribed", rxPrescribed.getRxTag());
            List<RxLines> _rxLinesList = rxPrescribed.getRxLinesList();

            //This will happen if there are no RxLines
            if (_rxLinesList != null)
            {
                _rxLines.addAll(_rxLinesList);
            }
        }

        for (Iterator<RxLines> iterator = _rxLines.iterator(); iterator
                .hasNext(); )
        {
            RxLines _rxLines1 = iterator.next();
            //Log.d("CareXMLReader-->Rx-->", _rxLines1.getRx() + "---" + _rxLines1.getScheduleByHours());
            // This data is delimited by comma
            String _scheduleInHrs = _rxLines1.getScheduleByHours();
            Log.d("Schedule Hours : ", _scheduleInHrs);
            StringTokenizer _st = new StringTokenizer(_scheduleInHrs, "-");

            while (_st.hasMoreTokens())
            {
                RxLineDTO _rxDTO = new RxLineDTO();

                String _timeElement = (String) _st.nextElement();
                _rxDTO.setRxTime(Integer.parseInt(_timeElement));
                /**
                 * 07/09/17 : we can get daily and weekly Lines. Weekly lines are
                 * like 1D (Sun), 1D-3D-5D-7D(Twice a week), 1D-3D-5D(Three times a week)
                 */
//                if(_timeElement.contains("D"))
//                {
//                    //find it is the right day to trigger
//                    boolean _dayFound = ifRightDayForRxLine(_timeElement);
//                    if(_dayFound)
//                    {
//                        //Take it as the first dose
//                        _rxDTO.setRxTime(8);
//                    }
//                    else
//                    {
//                        //Not eligble for the day
//                        _rxDTO.setRxTime(-1);
//                    }
//                }
//                else {
//
//                    _rxDTO.setRxTime(Integer.parseInt(_timeElement));
//                }
                _rxDTO.setRxLineId(_rxLines1.getId());
                _rxDTO.setRxLine(_rxLines1);
                // Log.d("CareXMLReader-->Rx-- Adding>", _rxLines1.getRx() + "---" + _rxLines1.getScheduleByHours());
                _rxLineDTOList.add(_rxDTO);

            }
        }

        return _rxLineDTOList;

    }


    /**
     * @param timeElement
     * @return
     */
    private static boolean ifRightDayForRxLine(String timeElement)
    {

        Calendar _c = Calendar.getInstance();
        int _dayOfWeek = _c.get(Calendar.DAY_OF_WEEK);
        String _dayOfWeekStr = Integer.toString(_dayOfWeek);

        if (timeElement.contains(_dayOfWeekStr))
        {
            return true;
        } else
        {
            return false;
        }
    }


    /**
     * @param xmlParser
     * @throws Exception
     */
    private static CaredPerson readCaredXML(XmlPullParser xmlParser) throws Exception
    {

        int eventType = xmlParser.getEventType();
        CaredPerson _caredPerson = null;
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                //System.out.println("Start tag " + xmlParser.getName());
                //Log.d("CareXMLReader", "readCaredXML--> " + xmlParser.getName());
                if (xmlParser.getName().equals("CaredPerson"))
                {

                    _caredPerson = readCaredPerson(xmlParser);
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                //System.out.println("End tag " + xmlParser.getName());
            } else if (eventType == XmlPullParser.TEXT)
            {
                //System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _caredPerson;
    }

    /**
     * @param xmlParser
     * @throws Exception
     */
    private static CaredPerson readCaredPerson(XmlPullParser xmlParser)
    throws Exception
    {

        CaredPerson _caredPerson = new CaredPerson();

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                //System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                //Log.d("CareXMLReader", "--readCaredPerson()=" + xmlParser.getName());
                if (xmlParser.getName().equals("RxPrescribed"))
                {
                    List<RxPrescribed> _rxPrescribedList = readRxPrescribed(xmlParser);
                    _caredPerson.setRxPrescribedList(_rxPrescribedList);
                }
                // dead code - never gets executed
                else if (xmlParser.getName().equals("PreExistingCondition"))
                {
                    PreExistingCondition _preCondition = readPreExistingCondition(xmlParser);
                    _caredPerson.setPreExistingCondition(_preCondition);
                } else if (xmlParser.getName().equals("EmergencyResponse"))
                {
                    EmergencyResponse _emergencyResponse = readEmergencyResponse(xmlParser);
                    _caredPerson.setEmergencyResponse(_emergencyResponse);
                } else if (xmlParser.getName().equals("ID"))
                {
                    eventType = xmlParser.next();
                    if (eventType == XmlPullParser.TEXT)
                    {
                        _caredPerson.setId(Long.parseLong(xmlParser.getText()));
                    }
                } else if (xmlParser.getName().equals("Name"))
                {
                    eventType = xmlParser.next();
                    if (eventType == XmlPullParser.TEXT)
                    {
                        _caredPerson.setName(xmlParser.getText());
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG)
            {
                //System.out.println("End tag " + xmlParser.getName());
            }

            /**
             * else if (eventType == XmlPullParser.TEXT) {
             * System.out.println("Text " + xmlParser.getText()); }
             **/
            //Log.d("CareXMLReader", "-- elementPointer Just before PreExistingCondition  ---  =" + elementPointer);

            if (elementPointer != null
                    && elementPointer.equals("PreExistingCondition"))
            {
                PreExistingCondition _preCondition = readPreExistingCondition(xmlParser);
                _caredPerson.setPreExistingCondition(_preCondition);
            }

            if (elementPointer != null
                    && elementPointer.equals("EmergencyResponse"))
            {
                EmergencyResponse _emergencyResponse = readEmergencyResponse(xmlParser);
                _caredPerson.setEmergencyResponse(_emergencyResponse);
            }

            eventType = xmlParser.next();
        }

        return _caredPerson;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static List<RxPrescribed> readRxPrescribed(XmlPullParser xmlParser)
    throws Exception
    {
        //Log.d("CareXMLReader-->RxPrescribed", "-- Inside readRxPrescribed --");
        RxPrescribed _rxPrescribed = null;
        ArrayList<RxPrescribed> rxPrescribedList = new ArrayList<RxPrescribed>();

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                //Log.d("CareXMLReader", "--readRxPrescribed() Start Tag =" + xmlParser.getName());
                if (xmlParser.getName().equals("RxPrescribed"))
                {
                    _rxPrescribed = new RxPrescribed();
                } else if (xmlParser.getName().equals("Physician"))
                {
                    Physician _phy = readPhysician(xmlParser);
                    _rxPrescribed.setPhysician(_phy);
                } else if (xmlParser.getName().equals("CareGiver"))
                {
                    CareGiver _cg = readCareGiver(xmlParser);
                    _rxPrescribed.setCareGiver(_cg);
                } else if (xmlParser.getName().equals("RxLines"))
                {
                    //Log.d("CareXMLReader", "--readRxPrescribed()-- inside if loop to call RxLine");
                    List<RxLines> _rxLines = readRxLines(xmlParser);
                    _rxPrescribed.setRxLinesList(_rxLines);
                    rxPrescribedList.add(_rxPrescribed);
                } else if (xmlParser.getName().equals("PreExistingCondition"))
                {
                    elementPointer = "PreExistingCondition";
                    break;
                } else if (xmlParser.getName().equals("ID"))
                {
                    eventType = xmlParser.next();
                    if (eventType == XmlPullParser.TEXT)
                    {
                        _rxPrescribed
                                .setId(Long.parseLong(xmlParser.getText()));
                    }
                } else if (xmlParser.getName().equals("RxTag"))
                {
                    eventType = xmlParser.next();
                    if (eventType == XmlPullParser.TEXT)
                    {
                        _rxPrescribed.setRxTag(xmlParser.getText());
                    }
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                //System.out.println("-----> Found End Tag <------"
                //        + xmlParser.getName());
                //Log.d("CareXMLReader", "--readRxPrescribed() End Tag =" + xmlParser.getName());
                if (xmlParser.getName().equals("RxPrescribed"))
                {
                    //System.out
                    //        .println("-----> Found RxPrescribed End Tag <------");
                    rxPrescribedList.add(_rxPrescribed);
                }
                // else if (xmlParser.getName().equals("PreExistingCondition"))
                // {
                // break;
                // }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }

            // End point detected
            // if (elementPointer != null &&
            // elementPointer.equals("RxPrescribed")) {
            // System.out.println("-----> Adding to rxPrescribedList<------");
            // rxPrescribedList.add(_rxPrescribed);
            // break;
            // }

            eventType = xmlParser.next();
        }

        return rxPrescribedList;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static Physician readPhysician(XmlPullParser xmlParser)
    throws Exception
    {

        Physician _physician = new Physician();

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                //Log.d("CareXMLReader", "--readPhysician()=" + xmlParser.getName());
                if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _physician.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Name"))
                {
                    xmlParser.next();
                    _physician.setName(xmlParser.getText());
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                if (xmlParser.getName().equals("Physician"))
                {
                    break;
                }
            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _physician;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static CareGiver readCareGiver(XmlPullParser xmlParser)
    throws Exception
    {

        CareGiver _careGiver = new CareGiver();

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");


            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                //Log.d("CareXMLReader", "--readCareGiver()=" + xmlParser.getName());

                if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _careGiver.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Name"))
                {
                    xmlParser.next();
                    _careGiver.setName(xmlParser.getText());
                }
            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());
                if (xmlParser.getName().equals("CareGiver"))
                {
                    break;
                }
            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _careGiver;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static List<RxLines> readRxLines(XmlPullParser xmlParser)
    throws Exception
    {
        //Log.d("CareXMLReader", "--RxLines-- Calling first line of method --");
        ArrayList<RxLines> _rxLinesList = new ArrayList<RxLines>();
        RxLines _rxLines = null;

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                //Log.d("CareXMLReader", "--readRxLines()=" + xmlParser.getName());
                if (xmlParser.getName().equals("RxLines"))
                {
                    _rxLines = new RxLines();
                } else if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _rxLines.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Rx"))
                {
                    xmlParser.next();
                    _rxLines.setRx(xmlParser.getText());
                } else if (xmlParser.getName().equals("Dosage"))
                {
                    xmlParser.next();
                    _rxLines.setDosage(xmlParser.getText());
                } else if (xmlParser.getName().equals("Route"))
                {
                    xmlParser.next();
                    _rxLines.setRoute(xmlParser.getText());
                } else if (xmlParser.getName().equals("ScheduleByHours"))
                {
                    xmlParser.next();
                    _rxLines.setScheduleByHours(xmlParser.getText());
                }
                // System.out.println("Start tag " + xmlParser.getName());
            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());
                if (xmlParser.getName().equals("RxLines"))
                {
                    _rxLinesList.add(_rxLines);
                } else if (xmlParser.getName().equals("RxPrescribed"))
                {
                    elementPointer = xmlParser.getName();
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _rxLinesList;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */

    private static PreExistingCondition readPreExistingCondition(
            XmlPullParser xmlParser) throws Exception
    {
        //Log.d("CareXMLReader", "---- Inside PreExistingCondition ------");
        PreExistingCondition _preExistingCondition = new PreExistingCondition();

        int eventType = xmlParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                //Log.d("CareXMLReader", "--PreExistingCondition =" + xmlParser.getName());
                if (xmlParser.getName().equals("Condition"))
                {
                    List<Condition> _conditionList = readCondition(xmlParser);
                    _preExistingCondition.setConditionList(_conditionList);
                } else if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _preExistingCondition.setId(Long.parseLong(xmlParser
                            .getText()));
                } else if (xmlParser.getName().equals("EmergencyResponse"))
                {
                    elementPointer = "EmergencyResponse";
                    break;
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());

                if (xmlParser.getName().equals("PreExistingCondition"))
                {
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }

            // if (elementPointer.equals("PreExistingCondition")) {
            // break;
            // }

            eventType = xmlParser.next();
        }

        return _preExistingCondition;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static List<Condition> readCondition(XmlPullParser xmlParser)
    throws Exception
    {

        ArrayList<Condition> _conditionList = new ArrayList<Condition>();
        Condition _condition = null;

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());

                if (xmlParser.getName().equals("Condition"))
                {
                    _condition = new Condition();
                } else if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _condition.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Name"))
                {
                    xmlParser.next();
                    _condition.setName(xmlParser.getText());
                } else if (xmlParser.getName().equals("Symptoms"))
                {
                    List<Symptoms> _symptomList = readSymptoms(xmlParser);
                    _condition.setSymptoms(_symptomList);
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());

                if (xmlParser.getName().equals("Condition"))
                {

                } else if (xmlParser.getName().equals("PreExistingCondition"))
                {
                    _conditionList.add(_condition);
                    elementPointer = "PreExistingCondition";
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _conditionList;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static List<Symptoms> readSymptoms(XmlPullParser xmlParser)
    throws Exception
    {

        Symptoms _symptom = null;
        ArrayList<Symptoms> _symptomList = new ArrayList<Symptoms>();

        int eventType = xmlParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                if (xmlParser.getName().equals("Symptoms"))
                {
                    _symptom = new Symptoms();
                } else if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _symptom.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Tag"))
                {
                    xmlParser.next();
                    _symptom.setTag(xmlParser.getText());
                }
            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());
                if (xmlParser.getName().equals("Symptoms"))
                {
                    _symptomList.add(_symptom);
                } else if (xmlParser.getName().equals("Condition"))
                {
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _symptomList;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static EmergencyResponse readEmergencyResponse(
            XmlPullParser xmlParser) throws Exception
    {

        EmergencyResponse _emergencyResponse = new EmergencyResponse();

        int eventType = xmlParser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());
                if (xmlParser.getName().equals("Provider"))
                {
                    Provider _provider = readProvider(xmlParser);
                    _emergencyResponse.setProvider(_provider);
                }
            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());

                if (xmlParser.getName().equals("EmergencyResponse"))
                {
                    elementPointer = null;
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _emergencyResponse;
    }

    /**
     * @param xmlParser
     * @return
     * @throws Exception
     */
    private static Provider readProvider(XmlPullParser xmlParser)
    throws Exception
    {

        Provider _provider = new Provider();

        int eventType = xmlParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT)
        {
            if (eventType == XmlPullParser.START_DOCUMENT)
            {
                // System.out.println("Start document");
            } else if (eventType == XmlPullParser.START_TAG)
            {
                // System.out.println("Start tag " + xmlParser.getName());

                if (xmlParser.getName().equals("ID"))
                {
                    xmlParser.next();
                    _provider.setId(Long.parseLong(xmlParser.getText()));
                } else if (xmlParser.getName().equals("Name"))
                {
                    xmlParser.next();
                    _provider.setName(xmlParser.getText());
                } else if (xmlParser.getName().equals("Contact"))
                {
                    xmlParser.next();
                    _provider.setContact(xmlParser.getText());
                } else if (xmlParser.getName().equals("Cell"))
                {
                    xmlParser.next();
                    _provider.setCell(xmlParser.getText());
                } else if (xmlParser.getName().equals("Fixed"))
                {
                    xmlParser.next();
                    _provider.setFixed(xmlParser.getText());
                } else if (xmlParser.getName().equals("ContactPerson"))
                {
                    xmlParser.next();
                    _provider.setContactPerson(xmlParser.getText());
                }

            } else if (eventType == XmlPullParser.END_TAG)
            {
                // System.out.println("End tag " + xmlParser.getName());
                if (xmlParser.getName().equals("Provider"))
                {
                    break;
                }

            } else if (eventType == XmlPullParser.TEXT)
            {
                // System.out.println("Text " + xmlParser.getText());
            }
            eventType = xmlParser.next();
        }

        return _provider;
    }
}
