/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model.beaneditor;

import java.text.DateFormat;

import java.util.Date;

import org.apache.commons.beanutils.Converter;

import org.springframework.beans.propertyeditors.CustomDateEditor;

/**
 * Custom Java-bean property editor to convert a String value into an oracle.jbo.domain.Timestamp object
 */
public class JboTimeStampEditor
  extends CustomDateEditor implements Converter
{
  public JboTimeStampEditor(DateFormat dateFormat, boolean b, int i)
  {
    super(dateFormat, b, i);
  }

  public JboTimeStampEditor(DateFormat dateFormat, boolean b)
  {
    super(dateFormat, b);
  }

  public Object getValue()
  {
    java.util.Date value = (Date) super.getValue();
    if (value==null)
    {
      return null;
    }
    java.sql.Timestamp sqlDate = new java.sql.Timestamp (value.getTime());
    return  new oracle.jbo.domain.Timestamp(sqlDate);       
  }

  @Override
  public Object convert(Class c, Object object)
  {
    setAsText(object.toString());
    return getValue();
  }

}
