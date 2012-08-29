package org.emg.adf.tftester.rt.model.beaneditor;

import java.text.DateFormat;

import java.util.Date;

import org.apache.commons.beanutils.Converter;

import org.springframework.beans.propertyeditors.CustomDateEditor;

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
