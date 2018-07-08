package com.phr.ade.xmlbinding;

import java.util.List;

/**
 * Created by deejay on 9/23/2014.
 */
public class CaredPerson
{

    private long id;
    private String name;
    private List<RxPrescribed> rxPrescribedList;
    private PreExistingCondition preExistingCondition;
    private EmergencyResponse emergencyResponse;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<RxPrescribed> getRxPrescribedList()
    {
        return rxPrescribedList;
    }

    public void setRxPrescribedList(List<RxPrescribed> rxPrescribedList)
    {
        this.rxPrescribedList = rxPrescribedList;
    }

    public PreExistingCondition getPreExistingCondition()
    {
        return preExistingCondition;
    }

    public void setPreExistingCondition(PreExistingCondition preExistingCondition)
    {
        this.preExistingCondition = preExistingCondition;
    }

    public EmergencyResponse getEmergencyResponse()
    {
        return emergencyResponse;
    }

    public void setEmergencyResponse(EmergencyResponse emergencyResponse)
    {
        this.emergencyResponse = emergencyResponse;
    }
}
