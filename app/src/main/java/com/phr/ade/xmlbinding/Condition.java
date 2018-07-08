package com.phr.ade.xmlbinding;

import java.util.List;

/**
 * Created by deejay on 9/23/2014.
 */
public class Condition
{

    private long id;
    private String name;
    private List<Symptoms> symptoms;

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

    public List<Symptoms> getSymptoms()
    {
        return symptoms;
    }

    public void setSymptoms(List<Symptoms> symptoms)
    {
        this.symptoms = symptoms;
    }
}
