package org.emg.adf.tftester.rt.controller.bean;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.event.DialogEvent;

import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlow;
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
  
  public void dialogListener(DialogEvent dialogEvent)
  {
    if (dialogEvent.getOutcome()==DialogEvent.Outcome.ok)
    {
      TaskFlow tf = new TaskFlow();
      tf.setTaskFlowIdString(getTaskFlowId());
      try
      {
        tf.getTaskFlowDefinition();
        tf.setDisplayName(getDisplayName());    
        taskFlowTesterService.getTestTaskFlows().add(tf);                    
      }
      catch (Exception e)
      {
        UIComponent tfiInput = JsfUtils.findComponent(dialogEvent.getComponent(), "tfiInput");
        JsfUtils.addError(tfiInput.getClientId(FacesContext.getCurrentInstance()), "Invalid Task Flow Id");
      }
    }
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return taskFlowTesterService.getTestTaskFlows();
  }
}
