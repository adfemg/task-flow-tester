package org.emg.adf.tftester.rt.controller.bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.event.DialogEvent;

import oracle.jbo.JboException;

import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlow;
import org.emg.adf.tftester.rt.model.TaskFlowTestCase;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;


public class TaskFlowTreeController
{
  private String taskFlowId;
  private String displayName;

  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();

  public TaskFlowTreeController()
  {
    super();
  }

  public MenuModel getMenuModel()
  {
    return new ChildPropertyMenuModel(taskFlowTesterService.getTestTaskFlows(),"testCases",null);
  }

  public void setTaskFlowId(String taskFlowId)
  {
    this.taskFlowId = taskFlowId;
  }

  public String getTaskFlowId()
  {
    return taskFlowId;
  }

  public void setDisplayName(String displayName)
  {
    this.displayName = displayName;
  }

  public String getDisplayName()
  {
    return displayName;
  }
  
  public void addTaskFlowListener(DialogEvent dialogEvent)
  {
    if (dialogEvent.getOutcome()==DialogEvent.Outcome.ok)
    {
      try
      {
        TaskFlow tf = taskFlowTesterService.addTaskFlow(getTaskFlowId(), getDisplayName(),true, false);              
        TaskFlowTester.getInstance().refreshTreeArea();
        TaskFlowTester.getInstance().setCurrentTestTaskFlow(tf);
      }
      catch (JboException e)
      {
        UIComponent tfiInput = JsfUtils.findComponent(dialogEvent.getComponent(), "tfiInput");
        JsfUtils.addError(tfiInput.getClientId(FacesContext.getCurrentInstance()), e.getMessage());
        JsfUtils.setInputFocus(tfiInput.getClientId(FacesContext.getCurrentInstance()));
      }
    }
  }

  /**
   * Return the rowkey set of the selected task flow or testcase.
   * Returns the first task flow by default
   * @return
   */
  public RowKeySet getSelectedRowKeySet()
  {
    List keys = new ArrayList();
    TaskFlow currentTf = TaskFlowTester.getInstance().getCurrentTestTaskFlow();
    if (currentTf!=null)
    {
      int tfIndex = getTestTaskFlows().indexOf(currentTf);      
      keys.add(new Integer(tfIndex));
      TaskFlowTestCase currentTc = TaskFlowTester.getInstance().getCurrentTestCase();
      if (currentTc!=null)
      {
        int tcIndex = currentTf.getTestCases().indexOf(currentTc);      
        keys.add(new Integer(tcIndex));        
      }
    }
    RowKeySetImpl selectedRowKeySet = new RowKeySetImpl();
    selectedRowKeySet.add(keys);      
    return selectedRowKeySet;
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return taskFlowTesterService.getTestTaskFlows();
  }

}