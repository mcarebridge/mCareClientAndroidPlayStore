package com.phr.ade.dto;

import com.phr.ade.xmlbinding.RxLines;

public class RxLineDTO
{

    private int rxTime;
    private long rxLineId;
    private RxLines rxLine;

    public int getRxTime()
    {
        return rxTime;
    }

    public void setRxTime(int rxTime)
    {
        this.rxTime = rxTime;
    }

    public long getRxLineId()
    {
        return rxLineId;
    }

    public void setRxLineId(long rxLineId)
    {
        this.rxLineId = rxLineId;
    }

    public RxLines getRxLine()
    {
        return rxLine;
    }

    public void setRxLine(RxLines rxLine)
    {
        this.rxLine = rxLine;
    }
}
