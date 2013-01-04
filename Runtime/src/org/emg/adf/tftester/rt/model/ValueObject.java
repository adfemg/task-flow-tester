/*******************************************************************************
 Copyright: see readme.txt

 $revision_history$
 17-dec-2012   Steven Davelaar
 1.1           Use java.util.HashMap as class when param type is java.util.Map
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model;

import java.beans.PropertyDescriptor;

import java.io.Serializable;

import java.math.BigDecimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.JboException;
import oracle.jbo.domain.Date;
import oracle.jbo.domain.Timestamp;

import org.apache.commons.beanutils.PropertyUtils;

import org.emg.adf.tftester.rt.model.beaneditor.ConverterHelperBean;
import org.emg.adf.tftester.rt.util.JsfUtils;

import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;


/**
 * Model class that holds all data about a task flow input parameter value.
 * If the value is a map or a complex type, the ValueObject is decomposed into
 * a list of child ValueObjects. This nesting of value objects is unlimited and
 * continues until the level where the end user can enter a simple value (or EL expression)
 * in the user interface to populate a property of the (nested) complex type.
 */
public class ValueObject implements Serializable
{
  private static ADFLogger sLog = ADFLogger.createADFLogger(ValueObject.class);
  @SuppressWarnings("compatibility:-122814710278353953")
  private static final long serialVersionUID = 1L;
  private String type;
  private String name;
  private transient Object value;
  String valueAsString;
  private static List<Class> simpleTypes = new ArrayList<Class>();
  String className;
  Class clazz;
  List<ValueObject> valueProperties = new ArrayList<ValueObject>();
  int propCounter = 0;
  int index;
  private ValueObject parent;
  private boolean mapEntry;
  private boolean elExpressionUsed = false;

  static
  {
    simpleTypes.add(Integer.class);
    simpleTypes.add(String.class);
    simpleTypes.add(BigDecimal.class);
    simpleTypes.add(Long.class);
    simpleTypes.add(Short.class);
    simpleTypes.add(Double.class);
    simpleTypes.add(Float.class);
    simpleTypes.add(Boolean.class);
    simpleTypes.add(Character.class);
    simpleTypes.add(Byte.class);
    simpleTypes.add(oracle.jbo.domain.Number.class);
    simpleTypes.add(java.util.Date.class);
    simpleTypes.add(oracle.jbo.domain.Date.class);
    simpleTypes.add(oracle.jbo.domain.Timestamp.class);
    simpleTypes.add(oracle.jbo.Key.class);
  }

  public ValueObject(String name, String type, ValueObject parent, boolean mapEntry)
  {
    super();
    this.type = type;
    if (Map.class.getName().equals(getType()))
    {
      setClassName(HashMap.class.getName());
    }
    else
    {
      setClassName(type);
    }
    this.name = name;
    this.parent = parent;
    this.index = propCounter++;
    this.mapEntry = mapEntry;
  }

  public boolean isObject()
  {
    String className = getClassName(true);
    return className==null || className.equals(Object.class.getName()) ;
  }

  public boolean isDate()
  {
    String className = getClassName(true);
    if (className==null)
    {
      return false;
    }
    return className.equals(Date.class.getName())
      || className.equals(java.util.Date.class.getName())
      || className.equals(Timestamp.class.getName());
  }

  public boolean isJboKey()
  {
   return getClassName(true).equals(oracle.jbo.Key.class.getName());
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getType()
  {
    return type;
  }

  public void setValue(Object value)
  {
    this.value = value;
  }

  public Object getValue()
  {
    return value;
  }

  public String getLabel()
  {
    return getName();
  }

  public String getHintText()
  {
    if (isComplexType())
    {
      return "Enter EL expression or click edit icon to construct/view instance of " + getClassName(true);
    }
    else if (isDate())
    {
      return "Enter EL expression or literal date value using format dd-MM-yyyy";
    }
    else if (isJboKey())
    {
      return "Enter EL expression or comma-delimited list of key attribute values. Note that the values must be specified in the same sequence as the attributes are defined in the key.";
    }
    return "Enter EL expression or literal value matching " + getClassName(true);
  }

  public String getClassName(boolean defaultToType)
  {
    if (getClassName()!=null)
    {
      return getClassName();
    }
    else if (defaultToType)
    {
      return getType();
    }
    return null;
  }

  public boolean isComplexType()
  {
    if (getClassName(true)==null)
    {
      // can happen with map entry
      return false;
    }
    Class c = getClass(getClassName(true));
    if (c==null)
    {
      // c  is null when property is primitive like int or char
      return false;
    }
    boolean complex = !simpleTypes.contains(c) && !c.isPrimitive();
    return complex;
  }

  public List<ValueObject> getValueProperties()
  {
    return valueProperties;
  }

  public boolean isMapType()
  {
    Class c = getClass(getClassName(true));
    if (c==null)
    {
      return false;
    }
    boolean ismap = Map.class.isAssignableFrom(c);
    return ismap;
  }

  public void initMapValueObject()
  {
    if (isMapType())
    {
      refreshMapEntries();
    }
  }

  public void initComplexValueObject()
  {
    if (isComplexType() && getValueProperties().size()==0)
    {
      String className = getClassName(true);
      Class c = getClass(className);
      setClassName(className);
      //    BeanWrapper bw = paramValue!=null ? new BeanWrapperImpl(paramValue) : new BeanWrapperImpl(c);
      BeanWrapper bw;
      try
      {
        bw = new BeanWrapperImpl(c);
      }
      catch (BeanInstantiationException e)
      {
        sLog.warning("Failure initializing complex type " + getClassName(), e);
        throw new JboException("Class " + c.getName() + " cannot be instantiated");
      }
      registerPropertyEditors(bw);
      PropertyDescriptor[] props = bw.getPropertyDescriptors();
      for (PropertyDescriptor prop: props)
      {
        if (!(prop.getName().equals("class")) && bw.isWritableProperty(prop.getName()) )
        {
          ValueObject vo =
            new ValueObject(prop.getName(), prop.getPropertyType().getName(), this, false);
          valueProperties.add(vo);
        }
      }
    }
  }

  public void setClassName(String className)
  {
    this.className = className;
    if (isMapEntry())
    {
      setType(className);
    }
  }

  public String getClassName()
  {
    return className;
  }

  public static Class getClass(String type)
  {
    try
    {
      Class c = Class.forName(type);
      return c;
    }
    catch (ClassNotFoundException e)
    {
    }
    return null;
  }

  private void refreshMapEntries()
  {
    if (getValue() instanceof Map)
    {
      valueProperties = new ArrayList<ValueObject>();
      Map map = (Map) getValue();
      Iterator keys = map.keySet().iterator();
      while (keys.hasNext())
      {
        String name = (String) keys.next();
        ValueObject vo = new ValueObject(name, map.get(name).getClass().getName(), this, true);
        vo.setValue(map.get(name));
        valueProperties.add(vo);
      }
    }
  }

  public void addMapEntry()
  {
    if (valueProperties == null)
    {
      valueProperties = new ArrayList<ValueObject>();
    }
    valueProperties.add(new ValueObject(null,"java.lang.String", this, true));
  }

  public Object instantiateComplexType()
  {
    Object instance = null;
    try
    {
      if (getClassName()==null)
      {
        throw new JboException("Class name is required");
      }
      String className = getClassName();
      // if it is java.util.Map, we use HashMap as instantiable type
      if ("java.util.Map".equals(className))
      {
        className = "java.util.HashMap";
      }
      Class c = getClass(className);
      if (c == null)
      {
        throw new JboException("Class " + getClassName() + " does not exist");
      }
      instance = c.newInstance();
      checkAssignable();
    }
    catch (InstantiationException e)
    {
      sLog.warning("Failure instantiating complex type " + getClassName(), e);
      throw new JboException("Class " + getClassName() + " cannot be instantiated");
    }
    catch (IllegalAccessException e)
    {
      sLog.warning("Failure instantiating complex type " + getClassName(), e);
      throw new JboException("Class " + getClassName() + " cannot be instantiated (IllegalAccessException)");
    }
    return instance;
  }

  /**
   *  Method checks whether the class name specified is assignable to the type of the value object.
   *  If not, a JboException is thrown
   */
  public void checkAssignable()
  {
    if (!(getType().equals(getClassName())))
    {
      //check whether we can cast the specified class name to the parameter type
      Class typeClass = getClass(getType());
      Class classClass = getClass(getClassName());
      boolean isAssignable = classClass==null || typeClass.isAssignableFrom(classClass);
      if (!isAssignable)
      {
        throw new JboException("Class " + getClassName() + " cannot be cast to " + getType());
      }
    }
  }


  /**
   * This method is called just before a test is run to ensure the value object is constructed
   * based on the current memory state. This is important when using EL expressions, and also ensures a complex
   * value object is constructed when the input params are loaded straight from XML and not entered manually
   */
  public void constructValue()
  {
    if (isElExpressionUsed())
    {
      setValue(JsfUtils.getExpressionValue(getValueAsString()));
    }
    else if (isMapType())
    {
      constructMapValue();
    }
    else if (isComplexType())
    {
      constructComplexValue();
    }
    // in other cases, we don't have to construct the value because it is aready set through call top setValueAsString
  }

  public void constructMapValue()
  {
    if (!isMapType())
    {
      return;
    }
    if (getValueProperties().size()==0)
    {
      this.value = null;
    }
    else
    {
      this.value = instantiateComplexType();
    }
    for (ValueObject pp: getValueProperties())
    {
        try
        {
          // call construct value first!
          pp.constructValue();
          if ((pp.getValue()==null || "".equals(pp.getValue())) && pp.isPrimitive())
          {
            // we cannot nullify a primitive, do nothing
            // TODO if property had a valid value before, we do not clear it now, need to figure out
            // how to handle that situation
            throw new JboException("Cannot nullify map value "+pp.getName()+" , you should remove the map entry");
          }
          else
          {
             Object convertedValue = pp.getValue();
            // Spring beanwrapper does not support map
            // we use helper bean that has all types as properties so we can still
            // use Spring to do type conversions using registered property editors
            if (!pp.isElExpressionUsed() && pp.getValue() instanceof String)
            {
              convertedValue = ConverterHelperBean.convertValue(pp.getClassName(true), (String) pp.getValue());
            }
            PropertyUtils.setProperty(value, pp.getName(), convertedValue);
          }
        }
        catch (Exception e)
        {
          sLog.warning("Failure setting map entry " + pp.getName(), e);
          throw new JboException("Cannot set map entry " + pp.getName() + " to value " + pp.getValue() + ": " +
                                 e.getMessage());
        }
    }
  }

  public void constructComplexValue()
  {
    this.value = null;
    for (ValueObject pp: getValueProperties())
    {
        try
        {
          // call construct value first!
          pp.constructValue();
          if ((pp.getValue()==null || "".equals(pp.getValue())) && pp.isPrimitive())
          {
            // we cannot nullify a primitive, do nothing
            // TODO if property had a valid value before, we do not clear it now, need to figure out
            // how to handle that situation
            sLog.fine("Cannot nullify property "+pp.getName()+" because it is a primitive type "+pp.getType());
          }
          else
          {
            if (value==null)
            {
              this.value = instantiateComplexType();
            }
            BeanWrapper bw = new BeanWrapperImpl(value);
            registerPropertyEditors(bw);
            if (bw.isWritableProperty(pp.getName()))
            {
              bw.setPropertyValue(pp.getName(), pp.getValue());
            }
          }
        }
        catch (Exception e)
        {
          sLog.warning("Failure setting property " + pp.getName(), e);
          throw new JboException("Cannot set property " + pp.getName() + " to value " + pp.getValue() + ": " +
                                 e.getMessage());
        }
    }
  }

  public boolean isPrimitive()
  {
    return getClass(getType())==null;
  }

  /**
   * Store String representation of value, and convert to actual value type when the string value
   * is not an EL expression
   * @param valueAsString
   */
  public void setValueAsString(String valueAsString)
  {
    this.valueAsString = valueAsString;
    if ((valueAsString==null || "".equals(valueAsString)) && isPrimitive())
    {
      // we cannot nullify a primitive, do nothing
      // TODO if property had a valid value before, we do not clear it now, need to figure out
      // how to handle that situation
      sLog.fine("Cannot nullify property "+getName()+" because it is a primitive type "+getType());
    }
    else if (!isElExpressionUsed())
    {
      // convert to actucal value using spring beanwrapper to get automatic type conversions.
      this.value = ConverterHelperBean.convertValue(getClassName(true), valueAsString);
    }
  }

  public String getValueAsString()
  {
    if (valueAsString==null && value!=null && !isComplexType())
    {
      return value.toString();
    }
    return valueAsString;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void setIndex(int index)
  {
    this.index = index;
  }

  public int getIndex()
  {
    return index;
  }

  public void setParent(ValueObject parent)
  {
    this.parent = parent;
  }

  public ValueObject getParent()
  {
    return parent;
  }

  public boolean equals(Object o)
  {
    if (o instanceof ValueObject)
    {
      return getIndex() == ((ValueObject) o).getIndex();
    }
    return false;
  }

  public void remove()
  {
    if (getParent()!=null)
    {
      getParent().getValueProperties().remove(this);
    }
  }

  public void setMapEntry(boolean mapEntry)
  {
    this.mapEntry = mapEntry;
  }

  public boolean isMapEntry()
  {
    return mapEntry;
  }

  private void registerPropertyEditors(BeanWrapper beanWrapper)
  {
    ConverterHelperBean.registerPropertyEditors(beanWrapper);
  }

  public void clearValue()
  {

  }

  public void setElExpressionUsed(boolean elExpressionUsed)
  {
    this.elExpressionUsed = elExpressionUsed;
  }

  public boolean isElExpressionUsed()
  {
    return elExpressionUsed;
  }

  public ValueObject clone()
  {
    ValueObject clone = new ValueObject(getName(),getType(),getParent(),isMapEntry());
    clone.setValue(getValue());
    // need to set ELExpressionUsed before calling setValueAsString otherwise
    // we get type converter mismatch
    clone.setElExpressionUsed(isElExpressionUsed());
    clone.setValueAsString(getValueAsString());
    clone.setIndex(getIndex());
    clone.setClassName(getClassName());
    if (getValueProperties()!=null)
    {
      for (ValueObject childVo : getValueProperties())
      {
        ValueObject childClone = childVo.clone();
        clone.getValueProperties().add(childClone);
        childClone.setParent(clone);
      }
    }
    return clone;
  }
}
