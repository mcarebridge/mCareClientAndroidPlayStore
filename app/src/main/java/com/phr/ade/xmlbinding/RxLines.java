package com.phr.ade.xmlbinding;

/**
 * Created by deejay on 9/23/2014.
 */
public class RxLines
{
    private long id;
    private String rx;
    private String dosage;
    private String route;
    private String scheduleByHours;
    ;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getRx()
    {
        return rx;
    }

    public void setRx(String rx)
    {
        this.rx = rx;
    }

    public String getDosage()
    {
        return dosage;
    }

    public void setDosage(String dosage)
    {
        this.dosage = dosage;
    }

    public String getRoute()
    {
        return route;
    }

    public void setRoute(String route)
    {
        this.route = route;
    }

    public String getScheduleByHours()
    {
        return scheduleByHours;
    }

    public void setScheduleByHours(String scheduleByHours)
    {
        this.scheduleByHours = scheduleByHours;
    }
}
