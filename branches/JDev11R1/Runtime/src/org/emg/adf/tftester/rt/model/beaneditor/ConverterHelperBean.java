/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model.beaneditor;

import java.beans.PropertyDescriptor;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;

import java.util.Date;

import oracle.jbo.Key;

import oracle.jbo.domain.Timestamp;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.propertyeditors.CustomDateEditor;

/**
 * This class converts String values to the correct type.
 * It is used when populating a Map type input parameter.
 * Spring BeanWrapper does not support a Map. Apache commons PropertyUtils does 
 * support a map but does not have the built-in property editors Spring has, and
 * they don't work with maps anyway as they are registered against a class, 
 * not a specific property
 */
public class ConverterHelperBean
{
  private byte byteValue;
  private float floatValue;
  private int intValue;
  private char charValue;
  private short shortValue;
  private double doubleValue;
  private boolean booleanValue;
  private long longValue;

  private Byte byteValueC;
  private Float floatValueC;
  private Integer intValueC;
  private Character charValueC;
  private Short shortValueC;
  private Double doubleValueC;
  private Boolean booleanValueC;
  private Long longValueC;
  
  private BigDecimal bigDecimalValue;
  private java.util.Date utilDateValue;
  private oracle.jbo.domain.Date jboDateValue;
  private oracle.jbo.domain.Timestamp jboTimestampValue;
  private Key key;
  
  public ConverterHelperBean()
  {
    super();
  }
  

  public static void registerPropertyEditors(BeanWrapper bw)
  {
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
    bw.registerCustomEditor(java.util.Date.class, new CustomDateEditor(sdf,true));    
    bw.registerCustomEditor(oracle.jbo.domain.Date.class, new JboDateEditor(sdf,true));    
    bw.registerCustomEditor(oracle.jbo.domain.Timestamp.class, new JboTimeStampEditor(sdf,true));    
    bw.registerCustomEditor(oracle.jbo.Key.class, new JboKeyEditor());    
  }

//  public static

  public void setByteValue(byte byteValue)
  {
    this.byteValue = byteValue;
  }

  public byte getByteValue()
  {
    return byteValue;
  }

  public void setFloatValue(float floatValue)
  {
    this.floatValue = floatValue;
  }

  public float getFloatValue()
  {
    return floatValue;
  }

  public void setIntValue(int intValue)
  {
    this.intValue = intValue;
  }

  public int getIntValue()
  {
    return intValue;
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

  public void setBooleanValue(boolean booleanValue)
  {
    this.booleanValue = booleanValue;
  }

  public boolean isBooleanValue()
  {
    return booleanValue;
  }

  public void setLongValue(long longValue)
  {
    this.longValue = longValue;
  }

  public long getLongValue()
  {
    return longValue;
  }

  public void setByteValueC(Byte byteValueC)
  {
    this.byteValueC = byteValueC;
  }

  public Byte getByteValueC()
  {
    return byteValueC;
  }

  public void setFloatValueC(Float floatValueC)
  {
    this.floatValueC = floatValueC;
  }

  public Float getFloatValueC()
  {
    return floatValueC;
  }

  public void setIntValueC(Integer intValueC)
  {
    this.intValueC = intValueC;
  }

  public Integer getIntValueC()
  {
    return intValueC;
  }

  public void setCharValueC(Character charValueC)
  {
    this.charValueC = charValueC;
  }

  public Character getCharValueC()
  {
    return charValueC;
  }

  public void setShortValueC(Short shortValueC)
  {
    this.shortValueC = shortValueC;
  }

  public Short getShortValueC()
  {
    return shortValueC;
  }

  public void setDoubleValueC(Double doubleValueC)
  {
    this.doubleValueC = doubleValueC;
  }

  public Double getDoubleValueC()
  {
    return doubleValueC;
  }

  public void setBooleanValueC(Boolean booleanValueC)
  {
    this.booleanValueC = booleanValueC;
  }

  public Boolean getBooleanValueC()
  {
    return booleanValueC;
  }

  public void setLongValueC(Long longValueC)
  {
    this.longValueC = longValueC;
  }

  public Long getLongValueC()
  {
    return longValueC;
  }

  public void setBigDecimalValue(BigDecimal bigDecimalValue)
  {
    this.bigDecimalValue = bigDecimalValue;
  }

  public BigDecimal getBigDecimalValue()
  {
    return bigDecimalValue;
  }

  public void setUtilDateValue(Date utilDateValue)
  {
    this.utilDateValue = utilDateValue;
  }

  public Date getUtilDateValue()
  {
    return utilDateValue;
  }

  public void setJboDateValue(oracle.jbo.domain.Date jboDateValue)
  {
    this.jboDateValue = jboDateValue;
  }

  public oracle.jbo.domain.Date getJboDateValue()
  {
    return jboDateValue;
  }

  public void setJboTimestampValue(Timestamp jboTimestampValue)
  {
    this.jboTimestampValue = jboTimestampValue;
  }

  public Timestamp getJboTimestampValue()
  {
    return jboTimestampValue;
  }

  public void setKey(Key key)
  {
    this.key = key;
  }

  public Key getKey()
  {
    return key;
  }
  
  public static Object convertValue(String type, String stringValue)
  {
    ConverterHelperBean instance = new ConverterHelperBean();
    BeanWrapper bw = new BeanWrapperImpl(instance);
    registerPropertyEditors(bw);
    PropertyDescriptor matchingDescriptor = null;
    for (PropertyDescriptor descriptor :   bw.getPropertyDescriptors())
    {
      if (descriptor.getPropertyType().getName().equals(type) )
      {
        matchingDescriptor = descriptor;
        break;
      }
    }
    if (matchingDescriptor!=null)
    {
      bw.setPropertyValue(matchingDescriptor.getName(), stringValue);    
      return bw.getPropertyValue(matchingDescriptor.getName());
    }
    return stringValue;
  }
  
  public static void main(String[] args)
    throws ClassNotFoundException
  {
    ConverterHelperBean instance = new ConverterHelperBean();
    Class c = Class.forName("char");
    System.err.println("IV: "+instance.intValue);
    instance.intValue = 0;
    System.err.println("IV 2: "+instance.intValue);
    System.err.println("CV: "+instance.charValue);
    instance.charValue = 0;
    System.err.println("CV 2: "+instance.charValue);
    System.err.println(convertValue("char", "A"));
  }
}
