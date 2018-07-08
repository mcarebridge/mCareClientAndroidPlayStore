package com.phr.ade.xmlbinding;

import java.util.List;

/**
 * Created by deejay on 9/23/2014.
 */
public class RxPrescribed
{

    private long id;
    private String rxTag;
    private Physician physician;
    private CareGiver careGiver;
    private List<RxLines> rxLinesList;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getRxTag()
    {
        return rxTag;
    }

    public void setRxTag(String rxTag)
    {
        this.rxTag = rxTag;
    }

    public Physician getPhysician()
    {
        return physician;
    }

    public void setPhysician(Physician physician)
    {
        this.physician = physician;
    }

    public CareGiver getCareGiver()
    {
        return careGiver;
    }

    public void setCareGiver(CareGiver careGiver)
    {
        this.careGiver = careGiver;
    }

    public List<RxLines> getRxLinesList()
    {
        return rxLinesList;
    }

    public void setRxLinesList(List<RxLines> rxLinesList)
    {
        this.rxLinesList = rxLinesList;
    }
}
