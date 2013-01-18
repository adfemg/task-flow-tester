/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 15-jan-2013   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model.beaneditor;

import java.beans.PropertyEditorSupport;


import java.sql.SQLException;

import oracle.jbo.domain.Number;

import org.apache.commons.beanutils.Converter;

/**
 * Custom Java-bean property editor to convert a String value into an oracle.jbo.domain.Number object
 */
public class JboNumberEditor
  extends PropertyEditorSupport
  implements Converter
{
  String textValue;
  
  public JboNumberEditor(Object object)
  {
    super(object);
  }

  public JboNumberEditor()
  {
    super();
  }
  
  public Object getValue()
  {
    String value = getAsText();
    if (value==null)
    {
      return null;
    }
    Number number = null;
    try
    {
      number = new Number(value);
    }
    catch (SQLException e)
    {
    }
    return number;       
  }


  public void setAsText(String text)
    throws IllegalArgumentException
  {
    textValue = text;
  }

  public String getAsText()
  {
    return textValue;
  }

  @Override
  public Object convert(Class c, Object object)
  {
    setAsText(object.toString());
    return getValue();
  }
}
