package com.phr.ade.xmlbinding;

import java.util.List;

/**
 * Created by deejay on 9/23/2014.
 */
public class PreExistingCondition
{
    private long id;
    private List<Condition> conditionList;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public List<Condition> getConditionList()
    {
        return conditionList;
    }

    public void setConditionList(List<Condition> conditionList)
    {
        this.conditionList = conditionList;
    }
}
