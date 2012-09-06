package org.emg.adf.tftester.rt.model.beaneditor;

import java.beans.PropertyEditorSupport;

import java.util.Date;

import java.util.StringTokenizer;

import oracle.jbo.Key;

import org.apache.commons.beanutils.Converter;

public class JboKeyEditor
  extends PropertyEditorSupport
  implements Converter
{
  String textValue;
  
  public JboKeyEditor(Object object)
  {
    super(object);
  }

  public JboKeyEditor()
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
    StringTokenizer tokenizer = new StringTokenizer(value, ",");
    String[] elements = new String[tokenizer.countTokens()];
    for (int i = 0; tokenizer.hasMoreTokens(); i++)
    {
      elements[i] = (tokenizer.nextToken()).trim();
    }    
    Key key = new Key(elements);
    return key;       
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
