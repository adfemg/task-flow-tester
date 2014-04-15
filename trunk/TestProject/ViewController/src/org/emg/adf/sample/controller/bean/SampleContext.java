package org.emg.adf.sample.controller.bean;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;

import oracle.jbo.Key;

import org.emg.adf.tftester.rt.model.beaneditor.ConverterHelperBean;
import org.emg.adf.tftester.rt.model.beaneditor.JboKeyEditor;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.propertyeditors.CustomDateEditor;


public class SampleContext implements SampleContextInterface {
  private String aap;
  private String noot;
  private String mies;
  private int intje;
  private Integer nummertje;
  private Integer nummerPrim;
  private BigDecimal bignummer;
  private char charValue;
  private short shortValue;
  private double doubleValue;
  private oracle.jbo.domain.Date dateValue;
  private Key key;
  private HashMap mapProperty;

  public SampleContext()
  {
    super();
  }

  public void setNoot(String noot)
  {
    this.noot = noot;
  }

  public String getNoot()
  {
    return noot;
  }

  public void setAap(String aap)
  {
    this.aap = aap;
  }

  public String getAap()
  {
    return aap;
  }

  public String getMies()
  {
    return mies;
  }

  public void setNummertje(Integer nummertje)
  {
    this.nummertje = nummertje;
  }

  public Integer getNummertje()
  {
    return nummertje;
  }

  public void setNummerPrim(Integer nummerPrim)
  {
    this.nummerPrim = nummerPrim;
  }

  public Integer getNummerPrim()
  {
    return nummerPrim;
  }

  public void setBignummer(BigDecimal bignummer)
  {
    this.bignummer = bignummer;
  }

  public BigDecimal getBignummer()
  {
    return bignummer;
  }

  public static void main(String[] args)
  {
    // see http://static.springsource.org/spring/docs/current/spring-framework-reference/html/validation.html
    SampleContext sc = new SampleContext();
    sc.test2();
  }

  public void test()
  {
    // see http://static.springsource.org/spring/docs/current/spring-framework-reference/html/validation.html
    BeanWrapper bw = new BeanWrapperImpl(this);
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    JboKeyEditor ed = new JboKeyEditor();
    bw.registerCustomEditor(Key.class, ed);
    try
    {
      bw.setPropertyValue("key", "23, AA, 44");
      System.err.println("key: "+getKey());
    }
    catch (BeansException e)
    {
      e.printStackTrace();
      System.err.println(e.getLocalizedMessage());
    }
  }

  public void test2()
  {
      Object value = ConverterHelperBean.convertValue("oracle.jbo.Key",  "23, AA, 44");
      System.err.println("key: "+value);
  }

  public void setKey(Key key)
  {
    this.key = key;
  }

  public Key getKey()
  {
    return key;
  }

  public void setMapProperty(HashMap mapProperty)
  {
    this.mapProperty = mapProperty;
  }

  public HashMap getMapProperty()
  {
    return mapProperty;
  }

  public class JboDateEditor extends CustomDateEditor
  {
    public JboDateEditor(SimpleDateFormat sdf, Boolean value)
    {
      super(sdf, value);
    }

    public Object getValue()
    {
      java.util.Date value = (Date) super.getValue();
      if (value==null)
      {
        return null;
      }
      java.sql.Timestamp sqlDate = new java.sql.Timestamp (value.getTime());
      return  new oracle.jbo.domain.Date(sqlDate);
    }
  }

  public void setIntje(int intje)
  {
    this.intje = intje;
  }

  public int getIntje()
  {
    return intje;
  }

  public void setCharValue(char charValue)
  {
    this.charValue = charValue;
  }

  public char getCharValue()
  {
    return charValue;
  }

  public void setShortValue(short shortValue)
  {
    this.shortValue = shortValue;
  }

  public short getShortValue()
  {
    return shortValue;
  }

  public void setDoubleValue(double doubleValue)
  {
    this.doubleValue = doubleValue;
  }

  public double getDoubleValue()
  {
    return doubleValue;
  }

  public void setDateValue(oracle.jbo.domain.Date dateValue)
  {
    this.dateValue = dateValue;
  }

  public oracle.jbo.domain.Date getDateValue()
  {
    return dateValue;
  }
}
