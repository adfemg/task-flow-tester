/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.List;

import oracle.adf.controller.internal.metadata.TaskFlowInputParameter;
//import oracle.adf.controller.metadata.model.TaskFlowInputParameter;

/**
 * Model class that holds all data about a task flow input parameter.
 * The value of the input parameter is stored in a separate ValueObject.
 * A ValueObject in itself can contain a child list of Value Object instances to
 * decompose the complex value object into -eventually- literal values that can be entered by the end user.
 * @see ValueObject
 */
public class InputParameter implements Serializable
{
  @SuppressWarnings("compatibility:-8269468157821682674")
  private static final long serialVersionUID = 1L;
  private transient TaskFlowInputParameter paramDef;
  private ValueObject valueObject;

  public InputParameter(TaskFlowInputParameter paramDef)
  {
    super();
    this.paramDef = paramDef;
    this.valueObject = new ValueObject(getName(),getType(),null,false);
  }

  public String getName()
  {
    return paramDef.getName();
  }

  public String getType()
  {
    return paramDef.getType()!=null ? paramDef.getType() : "java.lang.Object";
  }

  public boolean isRequired()
  {
    return paramDef.isRequired();
  }

  public Object getParamValue()
  {
    return valueObject.getValue();
  }

  public void setParamDef(TaskFlowInputParameter paramDef)
  {
    this.paramDef = paramDef;
  }

  public TaskFlowInputParameter getParamDef()
  {
    return paramDef;
  }

  public ValueObject getValueObject()
  {
    return valueObject;
  }

  /**
   * Returns the value object as list with one entry, needed on tester page to be able to reuse complex value editor decl component
   * @return
   */
  public List<ValueObject> getValueObjects()
  {
    List<ValueObject>  vos = new ArrayList<ValueObject>();
    vos.add(getValueObject());
    return vos;
  }

  public void setValueObject(ValueObject valueObject)
  {
    this.valueObject = valueObject;
  }
}
