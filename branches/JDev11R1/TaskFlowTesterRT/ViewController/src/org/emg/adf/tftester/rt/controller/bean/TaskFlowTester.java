package org.emg.adf.tftester.rt.controller.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import javax.faces.event.ValueChangeEvent;

import oracle.adf.controller.TaskFlowId;
import oracle.adf.controller.metadata.ActivityId;
import oracle.adf.controller.metadata.MetadataService;
import oracle.adf.controller.metadata.model.Activity;
import oracle.adf.controller.metadata.model.NamedParameter;
import oracle.adf.controller.metadata.model.ParsingContext;
import oracle.adf.controller.metadata.model.TaskFlowCall;
import oracle.adf.controller.metadata.model.TaskFlowDefinition;
import oracle.adf.controller.metadata.model.UIInfo;
import oracle.adf.controller.metadata.model.ValueMapping;
import oracle.adf.view.rich.context.AdfFacesContext;

import org.apache.myfaces.trinidad.event.ReturnEvent;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.InputParameter;
import org.emg.adf.tftester.rt.model.TaskFlow;
import org.emg.adf.tftester.rt.model.TaskFlowTestCase;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.model.ValueObject;
import org.emg.adf.tftester.rt.util.JsfUtils;

import org.w3c.dom.Node;


//import oracle.adf.controller.internal.metadata.Activity;
//import oracle.adf.controller.internal.metadata.TaskFlowCall;
// import oracle.adf.controller.internal.metadata.ValueMapping;


public class TaskFlowTester
{
  
  private final static String TESTER_TASK_FLOW_ID = "/WEB-INF/adfemg/tftester/tester-tf.xml#tester-tf"; 
  private final static String LAUNCHER_TASK_FLOW_ID = "/WEB-INF/adfemg/tftester/launcher-tf.xml#launcher-tf"; 
  private TaskFlow currentTestTaskFlow;
  private TaskFlowTestCase currentTestCase;

  private String testNavigationOutcome;
  private String runTaskFlowId;
  private String regionTaskFlowId = LAUNCHER_TASK_FLOW_ID;  
  private TaskFlowDefinition taskFlowDefinition;
  private Map runParamMap = new HashMap();
  private Map regionParamMap = new HashMap();
  
  private boolean runInRegion = true;
  private boolean runAsCall = false;
  private boolean testActive = false;
  private boolean stretchLayout = true;
  
  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();

  List<ReturnValue> testReturnValues;
  
  public TaskFlowTester()
  {
    super();
    setRegionParamMap(getLauncherParamMap());
  }

  /**
   * This method is called when the tester is started with taskFlowId request param specified
   * @param taskFlowId
   */
  public void setTestTaskFlowId(String taskFlowId)
  {
    TaskFlow tf = new TaskFlow();
    tf.setTaskFlowIdString(taskFlowId);    
    setCurrentTestTaskFlow(tf);
    getTestTaskFlows().add(tf);
  }

  public String getTestTaskFlowId()
  {
    return getCurrentTestTaskFlow()!=null ? getCurrentTestTaskFlow().getTaskFlowIdString() : null;
  }
  
  public void doTest(ActionEvent event)
  {
    setTestActive(true);
    // set up return values again, so values from previous run are cleared
    setUpDynamicTFCallReturnValues();
    Map testParamMap = new HashMap();
    for (InputParameter param : getCurrentTestTaskFlow().getInputParams())
    {
      // first construct value
      param.getValueObject().constructValue();
      if (param.getParamValue()!=null)
      {
        testParamMap.put(param.getName(), param.getParamValue());             
      }
    }    
    if (!getCurrentTestTaskFlow().isUsesPageFragments())
    {
      setRunTaskFlowId(getTestTaskFlowId());    
      runParamMap = testParamMap;      
    }
    else if (isRunInRegion())
    {
      setRegionTaskFlowId(getTestTaskFlowId());
      regionParamMap = testParamMap;
    }
    else
    {
      setRegionTaskFlowId(this.LAUNCHER_TASK_FLOW_ID);
      setRunTaskFlowId(getTestTaskFlowId());    
      regionParamMap = getLauncherParamMap();
      runParamMap = testParamMap;
    }
  }

  public void reset(ActionEvent event)
  {
    setRegionTaskFlowId(this.LAUNCHER_TASK_FLOW_ID);
    setRegionParamMap(getLauncherParamMap());
    setTestActive(false);
    setUpDynamicTFCallReturnValues();
  }

  public Map getLauncherParamMap()
  {
    Map params = new HashMap();    
    params.put("TaskFlowTester", this);
    return params;
  }

  public Map getRunParamMap()
  {
    return this.runParamMap;
  }

  public void setRunInRegion(boolean runInRegion)
  {
    this.runAsCall = !runInRegion;    
    this.runInRegion = runInRegion;
  }

  public boolean isRunInRegion()
  {
    return runInRegion;
  }

  public void setRunAsCall(boolean runAsCall)
  {
    this.runInRegion = !runAsCall;
    this.runAsCall = runAsCall;    
  }

  public boolean isRunAsCall()
  {
    return runAsCall;
  }

  public void setRunTaskFlowId(String runTaskFlowId)
  {
    this.runTaskFlowId = runTaskFlowId;
  }

  public TaskFlowId getRunTaskFlowId()
  {
    return TaskFlowId.parse(runTaskFlowId);
  }

  public void setRegionTaskFlowId(String regionTaskFlowId)
  {
    this.regionTaskFlowId = regionTaskFlowId;
  }

  public TaskFlowId getRegionTaskFlowId()
  {
    return TaskFlowId.parse(regionTaskFlowId);
  }

  public void setRegionParamMap(Map regionParamMap)
  {
    this.regionParamMap = regionParamMap;
  }

  public Map getRegionParamMap()
  {
    return regionParamMap;
  }

  public void setTestActive(boolean testActive)
  {
    this.testActive = testActive;
  }

  public boolean isTestActive()
  {
    return testActive;
  }

  public void setUpDynamicTFCallReturnValues()
  {
    setTestNavigationOutcome(null);
    // get return value definitions from TF that we are going to call
    Map<String, NamedParameter> returnValueDefs = getCurrentTestTaskFlow().getReturnValueDefs();

    Map<String,ValueMapping> returnValueMappings = getTaskFlowCallReturnValueMappings();

    // now clear dummy returnValues that might be specified for our dynamic TF call
    // and set the return values based on definitions in test TF
    // we create the value expression to store the return values based on the name
    returnValueMappings.clear();
    // Also set up return values map that will be populated with actual retunr values
    testReturnValues = new ArrayList<ReturnValue>();
    for (NamedParameter param : returnValueDefs.values())
    {
      String name = param.getName();
      String expression = "#{pageFlowScope."+name+"}";
      ValueMapping vm = new ValueMappingImpl(name,expression);
      returnValueMappings.put(param.getName(), vm);
      testReturnValues.add(new ReturnValue(name,null,expression));
    }
    
  }

  private Map<String, ValueMapping> getTaskFlowCallReturnValueMappings()
  {
    // now get the TF call activity and change return values to map the return value defs of the TF we are going to test
    TaskFlowId launcherTfi = getCurrentTestTaskFlow().isUsesPageFragments() ? TaskFlowId.parse(LAUNCHER_TASK_FLOW_ID) : TaskFlowId.parse(TESTER_TASK_FLOW_ID);    
    TaskFlowDefinition launcherTFDef = MetadataService.getInstance().getTaskFlowDefinition(launcherTfi);
    Map<ActivityId, Activity> activities = launcherTFDef.getActivities();
    // loop over activites until  activity  'testTaskFlowCall' is found 
    Map<String,ValueMapping> returnValues = null;
    for (ActivityId actId: activities.keySet())
    {
      if (actId.getLocalActivityId().equals("testTaskFlowCall"))
      {
        Activity act = activities.get(actId);
        TaskFlowCall tfc = (TaskFlowCall) act.getMetadataObject();
        returnValues = tfc.getReturnValues();
        break;
      }
    }
    return returnValues;        
  }

  public void setCurrentTestTaskFlow(TaskFlow currentTestTaskFlow)
  {
    this.currentTestTaskFlow = currentTestTaskFlow;
    setCurrentTestCase(null);
    currentTestTaskFlow.clearParamValueObjects();
    if (!currentTestTaskFlow.isUsesPageFragments())
    {
      setRunAsCall(true);
    }
    setUpDynamicTFCallReturnValues();
    setTestActive(false);
    refreshTestArea();
  }

  public TaskFlow getCurrentTestTaskFlow()
  {
    return currentTestTaskFlow;
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return taskFlowTesterService.getTestTaskFlows();
  }

  /**
   * This methid is called by the after-listener on the dynamic TF Call
   * It populates return values list used to display return values in UI and
   * refreshes the Task Flow config area
   */
  public void returnedFromTestTaskFlowCall()
  { 
    refreshTestArea();
    populateTestReturnValues();
    setTestActive(false);
  }
  
  public void refreshTaskFlowArea()
  {
    UIComponent tfarea = JsfUtils.findComponentInRoot("tfarea");
    refreshComponent(tfarea);
    JsfUtils.resetComponentTree(tfarea);
  }

  public void setStretchLayout(boolean stretchLayout)
  {
    this.stretchLayout = stretchLayout;
  }

  public boolean isStretchLayout()
  {
    return stretchLayout;
  }

  public void setCurrentTestCase(TaskFlowTestCase currentTestCase)
  {
    if (currentTestCase==null)
    {
      this.currentTestCase = null;
      return;
    }
    TaskFlow tf = currentTestCase.getTaskFlow();
    setCurrentTestTaskFlow(tf);
    // set testcase after call to setCurrentTestTaskFlow because that c;lears out current testcase
    this.currentTestCase = currentTestCase;
    // clone and apply test case param value objects to current task flow
    tf.applyParamValueObjects(currentTestCase.getParamValueObjects());
    setRunAsCall(currentTestCase.isRunAscall());
    setRunInRegion(currentTestCase.isRunInRegion());
    setStretchLayout(currentTestCase.isStretchLayout());
    refreshTestArea();
  }

  public TaskFlowTestCase getCurrentTestCase()
  {
    return currentTestCase;
  }
  
  public String getHeaderText()
  {
    if (getCurrentTestCase()!=null)
    {
      return "Task Flow Testcase "+getCurrentTestCase().getName();
    }
    else if (getCurrentTestTaskFlow()!=null)
    {
      return "Task Flow "+getCurrentTestTaskFlow().getDisplayName();
    }
    else if (getCurrentTestTaskFlow()!=null)
    {
      return "Task Flow "+getCurrentTestTaskFlow().getDisplayName();
    }
    else if (taskFlowTesterService.getTestTaskFlows().size()>0)
    {
      return "Select a task flow from the tree menu or add a new task flow by clicking the green plus Icon";
    }
    return "Add a task flow by clicking the green plus icon";
  }

  public void changeRunAsOption(ValueChangeEvent valueChangeEvent)
  {
    setRunInRegion((Boolean)valueChangeEvent.getNewValue());
    refreshTestArea(false);
  }

  public void returnedFromTestDialog(ReturnEvent returnEvent)
  {
    refreshTestArea();
  }

  public void setTestNavigationOutcome(String testNavigationOutcome)
  {
    this.testNavigationOutcome = testNavigationOutcome;
  }

  public String getTestNavigationOutcome()
  {
    return testNavigationOutcome;
  }

  private class ValueMappingImpl implements ValueMapping
  {
    String name;
    String expression;

    public ValueMappingImpl(String name, String expression)
    {
      this.name = name;
      this.expression = expression;
    }

    public String getValueName()
    {
      return name; 
    }

    /**
     * @return the value EL expression for this mapping
     */
    public String getValueExpression()
    {
      return expression;       
    }

    @Override
    public boolean passByValue()
    {
      return false;
    }

    @Override
    public UIInfo getUIInfo()
    {
      return null;
    }

    @Override
    public String getDescription()
    {
      return null;
    }

    @Override
    public String getDisplayName()
    {
      return null;
    }

    @Override
    public String getLargeIcon()
    {
      return null;
    }

    @Override
    public String getSmallIcon()
    {
      return null;
    }

    @Override
    public boolean setDescription(String description)
    {
      return false;
    }

    @Override
    public boolean setDisplayName(String displayName)
    {
      return false;
    }

    @Override
    public boolean setLargeIcon(String largeIcon)
    {
      return false;
    }

    @Override
    public boolean setSmallIcon(String smallIcon)
    {
      return false;
    }

    @Override
    public ParsingContext getParsingContext()
    {
      return null;
    }

    @Override
    public boolean validate()
    {
      return false;
    }

    @Override
    public String getIdAttribute()
    {
      return null;
    }

    @Override
    public boolean setIdAttribute(String id)
    {
      return false;
    }

    @Override
    public Node getNode()
    {
      return null;
    }
  }

  public void populateTestReturnValues()
  {
    // if test active, we need to populate with actual values
    for (ReturnValue param : testReturnValues)
    {
      String name = param.getName();
      String expression = param.getValueExpression();
      Object returnValue = JsfUtils.getExpressionValue(expression);
      param.setValue(returnValue);
    }         
    String outcome = (String) JsfUtils.getExpressionValue("#{lastNavigationOutcome}");
    setTestNavigationOutcome(outcome);
  }

  public List<ReturnValue> getTestReturnValues()
  {
    return testReturnValues;
  }
        
  public class ReturnValue
  {
    private String name;
    private String valueExpression;
    private Object value;

    public ReturnValue(String name,Object value, String valueExpression)
    {
      this.name = name;
      this.value = value;   
      this.valueExpression = valueExpression;
    }

    public void setName(String name)
    {
      this.name = name;
    }

    public String getName()
    {
      return name;
    }

    public void setValue(Object value)
    {
      this.value = value;
    }

    public Object getValue()
    {
      return value;
    }

    public void setValueExpression(String valueExpression)
    {
      this.valueExpression = valueExpression;
    }

    public String getValueExpression()
    {
      return valueExpression;
    }
  }

  public void refreshTreeArea()
  {
    UIComponent treeArea = JsfUtils.findComponentInRoot("treeArea");
    refreshComponent(treeArea);
  }

  public void refreshTestArea()
  {
    refreshTestArea(true);
  }

  public void refreshTestArea(boolean resetComponentTree)
  {
    UIComponent testArea = JsfUtils.findComponentInRoot("testArea");
    refreshComponent(testArea);
    if (resetComponentTree)
    {
      JsfUtils.resetComponentTree(testArea);      
    }
  }
  
  public void refreshComponent(UIComponent comp)
  {
    if (comp!=null)
    {
      AdfFacesContext.getCurrentInstance().addPartialTarget(comp);      
    }    
  }

  public static TaskFlowTester getInstance()
  {
    return (TaskFlowTester) JsfUtils.getExpressionValue("#{pageFlowScope.TaskFlowTester}");
  }
}
