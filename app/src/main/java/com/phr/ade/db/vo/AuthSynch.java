package com.phr.ade.db.vo;

import java.io.Serializable;


public class AuthSynch implements Serializable
{
    private Integer id;
    private String IMEI;
    private String Status;
    private String State;
    private String carePayLoad;
    private String rxConsumed;
    private String createDate;


    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getIMEI()
    {
        return IMEI;
    }

    public void setIMEI(String IMEI)
    {
        this.IMEI = IMEI;
    }

    public String getStatus()
    {
        return Status;
    }

    public void setStatus(String status)
    {
        Status = status;
    }

    public String getState()
    {
        return State;
    }

    public void setState(String state)
    {
        State = state;
    }

    public String getCarePayLoad()
    {
        return carePayLoad;
    }

    public void setCarePayLoad(String carePayLoad)
    {
        this.carePayLoad = carePayLoad;
    }

    public String getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    public String getRxConsumed()
    {
        return rxConsumed;
    }

    public void setRxConsumed(String rxConsumed)
    {
        this.rxConsumed = rxConsumed;
    }
}
