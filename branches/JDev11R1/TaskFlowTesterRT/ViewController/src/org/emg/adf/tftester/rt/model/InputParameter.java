package org.emg.adf.tftester.rt.model;

import java.util.ArrayList;
import java.util.List;

import oracle.adf.controller.internal.metadata.TaskFlowInputParameter;
//import oracle.adf.controller.metadata.model.TaskFlowInputParameter;


public class InputParameter
{
  TaskFlowInputParameter paramDef;
  ValueObject valueObject;

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
    return paramDef.getType();
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
