package org.emg.adf.tftester.rt.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import oracle.adf.controller.TaskFlowId;
import oracle.adf.controller.internal.metadata.MetadataService;
import oracle.adf.controller.internal.metadata.NamedParameter;
import oracle.adf.controller.internal.metadata.TaskFlowDefinition;
import oracle.adf.controller.internal.metadata.TaskFlowInputParameter;
//import oracle.adf.controller.metadata.MetadataService;
//import oracle.adf.controller.metadata.model.NamedParameter;
//import oracle.adf.controller.metadata.model.TaskFlowDefinition;
//import oracle.adf.controller.metadata.model.TaskFlowInputParameter;


import oracle.adf.share.logging.ADFLogger;

import oracle.jbo.JboException;

public class TaskFlow
{
  
  private static ADFLogger sLog = ADFLogger.createADFLogger(TaskFlow.class);
  private TaskFlowDefinition taskFlowDefinition;
  private List<org.emg.adf.tftester.rt.model.InputParameter> inputParams;
  private List<TaskFlowTestCase> testCases = new ArrayList<TaskFlowTestCase>();
  private String displayName;
  private String taskFlowIdString;
  private TaskFlowId taskFlowId;
  
  public TaskFlow()
  {
    super();
  }


  public TaskFlowDefinition getTaskFlowDefinition()
  {
    if (taskFlowDefinition==null)
    {
      MetadataService service = MetadataService.getInstance();
      TaskFlowId taskFlowId = TaskFlowId.parse(getTaskFlowIdString());
      taskFlowDefinition =
        service.getTaskFlowDefinition(taskFlowId);      
    }
    return taskFlowDefinition;
  }

  
  public List<org.emg.adf.tftester.rt.model.InputParameter> getInputParams()
  {
    if (inputParams==null)
    {
      inputParams = new ArrayList<org.emg.adf.tftester.rt.model.InputParameter>();
      TaskFlowDefinition tfdef = getTaskFlowDefinition();
      Collection<TaskFlowInputParameter> params = tfdef.getInputParameters().values();
      for (TaskFlowInputParameter paramDef : params)
      {
        org.emg.adf.tftester.rt.model.InputParameter param = new org.emg.adf.tftester.rt.model.InputParameter(paramDef);
        inputParams.add(param);
      }
    }
    return inputParams;
  }


  public void setDisplayName(String description)
  {
    this.displayName = description;
  }

  public String getName()
  {
    return getDisplayName();
  }

  /**
   * Returns the custom displayName set by tester. If not set, returns displayName of task flow definition,
   * if not set returns task flow id
   * @return
   */
  public String getDisplayName()
  {
    if (displayName!=null)
    {
      return displayName;      
    }
    if (getTaskFlowDefinition().getDisplayName()!=null)
    {
      return getTaskFlowDefinition().getDisplayName();
    }
    return getTaskFlowId().getLocalTaskFlowId();
  }

  public void setTaskFlowIdString(String taskFlowIdString)
  {
    this.taskFlowIdString = taskFlowIdString;
    setTaskFlowId(TaskFlowId.parse(taskFlowIdString));
  }

  public String getTaskFlowIdString()
  {
    return taskFlowIdString;
  }

  public Map<String, NamedParameter> getReturnValueDefs()
  {
    return getTaskFlowDefinition().getReturnValues();
  }

  public void setTaskFlowId(TaskFlowId taskFlowId)
  {
    this.taskFlowId = taskFlowId;
  }

  public TaskFlowId getTaskFlowId()
  {
    return taskFlowId;
  }
  
  public String getDataControlScope()
  {
    return getTaskFlowDefinition().getDataControlScopeType().toString();
  }

  public String getInitializer()
  {
    return getTaskFlowDefinition().getInitializer();
  }

  public String getFinalizer()
  {
    return getTaskFlowDefinition().getFinalizer();
  }

  public String getTransactionType()
  {
    if (getTaskFlowDefinition().getTransactionType()!=null)
    {
      return getTaskFlowDefinition().getTransactionType().toString();      
    }
    return "No controller transaction";
  }

  public boolean isUsesPageFragments()
  {
    return getTaskFlowDefinition().usePageFragments();
  }

  public void addTestCase(TaskFlowTestCase testCase)
  {
    getTestCases().add(testCase);
  }

  public void removeTestCase(TaskFlowTestCase testCase)
  {
    getTestCases().remove(testCase);
  }

  public void setTestCases(List<TaskFlowTestCase> testCases)
  {
    this.testCases = testCases;
  }

  public List<TaskFlowTestCase> getTestCases()
  {
    return testCases;
  }
  
  public InputParameter getInputParameter(String name)
  {
    InputParameter ipfound = null;
    for (InputParameter ip : getInputParams())
    {
      if (ip.getName().equals(name))
      {
        ipfound = ip;
        break;
      }
    }
    return ipfound;
  }
  
  public void applyParamValueObjects(List<ValueObject> paramValueObjects)
  {
    if (paramValueObjects!=null)
    {
      for (ValueObject vo : paramValueObjects)
      {
        InputParameter ip = getInputParameter(vo.getName());
        if (ip==null)
        {
          sLog.warning("Input parameter "+vo.getName()+" not found in task flow "+getTaskFlowIdString());
        }
        else
        {
          ip.setValueObject(vo.clone());          
        }
      }      
    }
  }

  public void clearParamValueObjects()
  {
    for (InputParameter ip : getInputParams())
    {
      ip.getValueObject().setValue(null);
      ip.getValueObject().setValueAsString(null);
      ip.getValueObject().getValueProperties().clear();
      // reset type and class name as well
      ip.getValueObject().setClassName(ip.getType());
      ip.getValueObject().setType(ip.getType());
    }      
  }
  
}
