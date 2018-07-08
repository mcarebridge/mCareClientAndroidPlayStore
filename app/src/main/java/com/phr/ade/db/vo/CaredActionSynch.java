package com.phr.ade.db.vo;


import java.io.Serializable;

public class CaredActionSynch implements Serializable
{
    private Integer id;
    private String IMEI;

    /**
     * Stored the Status of the Cared Rx Action
     * Values :
     * CURRENT - For the record valid for during that scheduled Rx
     * CURRENT -> STAGED : Set it for records which are not valid during that Hr.
     * STAGED -> SYNCHED : After the record has been set to Server
     */
    private String Status;

    //Stores the Server Synch up status
    //OPEN -> SUBMIT
    //OPEN -> TRA_ERR (Transmission error)
    //ERROR (Any other error)
    private String State;
    private String caredResponse;
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

    public String getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate(String createDate)
    {
        this.createDate = createDate;
    }

    public String getCaredResponse()
    {
        return caredResponse;
    }

    public void setCaredResponse(String caredResponse)
    {
        this.caredResponse = caredResponse;
    }
}
