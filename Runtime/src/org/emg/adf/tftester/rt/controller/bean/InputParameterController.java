package org.emg.adf.tftester.rt.controller.bean;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.context.AdfFacesContext;
import oracle.adf.view.rich.event.DialogEvent;
import oracle.adf.view.rich.event.PopupFetchEvent;

import oracle.jbo.JboException;

import org.emg.adf.tftester.rt.model.ValueObject;
import org.emg.adf.tftester.rt.util.JsfUtils;


public class InputParameterController
{

  public InputParameterController()
  {
    super();
  }
  /**
   * The raw value can either be a JSF EL expression, or a literal value that can be used
   * to create the actual value. For simple parameters, the literal rawValue is simply
   * copied over to the value.
   * @param rawParamValue
   */
  public void setRawParamValue(String rawParamValue)
  {
    ValueObject param = getValueObject();
    String className = param.getClassName()!=null ?param.getClassName() : param.getType();
    if (rawParamValue==null || "".equals(rawParamValue))
    {
      param.setValueAsString(null);         
      param.setValue(null);         
      return;       
    } 
    if (rawParamValue.startsWith("#{"))
    {
      param.setElExpressionUsed(true);
      param.setValueAsString(rawParamValue);
      // EL expression is evaluated when test is run
    }
    else if (param.isComplexType())
    {
       String messagePart = param.isMapType() ? "to populate the map with values" : "to construct an instance of "+param.getType();
       throw new JboException("Invalid value for parameter "+param.getName()+". You need to enter an EL expression or click the edit icon "+messagePart);        
      // we do not call setParamValue in this case, might wipe out complex-constructed value
    }
    else
    {
      // in setValueAsString we use Spring bean wrapper to get automatic type conversions
      // using registered property editors (unless elExpressionUsed = true)
      param.setValueAsString(rawParamValue);
    }
  }

  public String getRawParamValue()
  {
    ValueObject param = getValueObject();
    if (param.getValueAsString()!=null)
    {
      return param.getValueAsString();
    }
    else if (!param.isComplexType() && param.getValue()!=null)
    {
      return param.getValue().toString();
    }
    return null;
  }

  public void addParamMapEntry(ActionEvent event)
  {
    ValueObject valueObject = getValueObject();
    valueObject.addMapEntry();
    AdfFacesContext.getCurrentInstance().addPartialTarget(event.getComponent().getParent().getParent().getParent());
  }

  private ValueObject getValueObject()
  {
    ValueObject param = (ValueObject) JsfUtils.getExpressionValue("#{prop}");
    if (param==null)
    {
      // we are NOT in context of decl comp
      param = (ValueObject) JsfUtils.getExpressionValue("#{row.valueObject}");
    }
    return param;
  }

  public void removeMapEntry(ActionEvent event)
  {
    ValueObject pp = (ValueObject) JsfUtils.getExpressionValue("#{prop}");
    pp.remove();
    UIComponent refreshRoot = event.getComponent().getParent().getParent().getParent();
    AdfFacesContext.getCurrentInstance().addPartialTarget(refreshRoot);
    JsfUtils.resetComponentTree();
  }

  public void createMapParamValue(DialogEvent event) throws AbortProcessingException
  {
    if (event.getOutcome()==DialogEvent.Outcome.ok || event.getOutcome()==DialogEvent.Outcome.yes)
    {
      ValueObject param = getValueObject();
      try
      {
        param.constructMapValue();
      }
      catch (JboException e)
      {
        JsfUtils.addError(null, e.getLocalizedMessage());
  //        DCBindingContainer container = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
  //        container.processException(e);
      }
    }
  }

  public void createComplexParamValue(ActionEvent event)
  {
    ValueObject param = getValueObject();
    if (param.isMapType() || !param.isComplexType())
    {
      // refresh test area so we get a map icon and help text
      TaskFlowTester.getInstance().refreshTestArea(false);
      UIComponent component = event.getComponent().getParent().getParent();
      if (component instanceof RichPopup)
      {
        ((RichPopup)component).hide();
      }
      return;
    }
    try
    {
      param.constructComplexValue();
      UIComponent component = event.getComponent().getParent().getParent();
      if (component instanceof RichPopup)
      {
        ((RichPopup)component).hide();
      }
    }
    catch (JboException e)
    {
      JsfUtils.addError(null, e.getLocalizedMessage());
//        DCBindingContainer container = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
//        container.processException(e);
    }
  }

  public void openComplexParamPopup(PopupFetchEvent popupFetchEvent)
  {
    ValueObject valueObject = getValueObject();
    valueObject.initComplexValueObject();
  }

  public void openMapParamPopup(PopupFetchEvent popupFetchEvent)
  {
    ValueObject valueObject = getValueObject();
    valueObject.initMapValueObject();
  }

  /**
   * Refresh the key and value fields when chaging the map entry class, so correct hint text and edit icon
   * will appear if applicable
   * @param event
   */
  public void classNameChanged(ValueChangeEvent event)
  {
    ValueObject vo = getValueObject();
    try
    {
      vo.setClassName((String) event.getNewValue());
      if (vo.isMapEntry())
      {
        // In case of map, set type to same value, so we don't get isAssignable error
        // because we inialized a map entry with type String
        vo.setType(vo.getClassName());        
      }
      // check whether the class is instantiable and can be assigned to the type of the parameter if it is a complex type
      // if it is a simple type, we only check whether it is assignable to the type
      if (vo.isComplexType())
      {
        // in instantiation we also check the type
        vo.instantiateComplexType();        
      }
      else
      {
        vo.checkAssignable();
      }
      // clear existing valueProperties
      vo.getValueProperties().clear();
      // determine new value properties based on new class name
      vo.initComplexValueObject();
      AdfFacesContext.getCurrentInstance().addPartialTarget(event.getComponent().getParent());      
    }
    catch (JboException e)
    {
      // TODO: Add catch code
      DCBindingContainer bc = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
      bc.processException(e);
    }
  }
}
