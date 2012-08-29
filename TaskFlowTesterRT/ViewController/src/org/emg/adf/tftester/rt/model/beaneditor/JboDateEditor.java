package org.emg.adf.tftester.rt.model.beaneditor;

import java.text.DateFormat;

import java.util.Date;

import org.apache.commons.beanutils.Converter;

import org.springframework.beans.propertyeditors.CustomDateEditor;

public class JboDateEditor
  extends CustomDateEditor implements Converter
{
  public JboDateEditor(DateFormat dateFormat, boolean b, int i)
  {
    super(dateFormat, b, i);
  }

  public JboDateEditor(DateFormat dateFormat, boolean b)
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
    return  new oracle.jbo.domain.Date(sqlDate);       
  }

  @Override
  public Object convert(Class c, Object object)
  {
    setAsText(object.toString());
    return getValue();
  }

}
