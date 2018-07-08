package com.phr.ade.xmlbinding;

/**
 * Created by deejay on 9/23/2014.
 */
public class Provider
{

    private long id;
    private String name;
    private String contact;
    private String cell;
    private String fixed;
    private String contactPerson;

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

    public String getContact()
    {
        return contact;
    }

    public void setContact(String contact)
    {
        this.contact = contact;
    }

    public String getCell()
    {
        return cell;
    }

    public void setCell(String cell)
    {
        this.cell = cell;
    }

    public String getFixed()
    {
        return fixed;
    }

    public void setFixed(String fixed)
    {
        this.fixed = fixed;
    }

    public String getContactPerson()
    {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson)
    {
        this.contactPerson = contactPerson;
    }
}
