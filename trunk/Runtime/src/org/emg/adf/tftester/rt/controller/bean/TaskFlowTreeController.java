package org.emg.adf.tftester.rt.controller.bean;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.event.DialogEvent;

import oracle.jbo.JboException;

import org.apache.myfaces.trinidad.event.SelectionEvent;
import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlow;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;


public class TaskFlowTreeController
{
  private String taskFlowId;
  private String displayName;

  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();
  RowKeySet selectedRowKeySet = new RowKeySetImpl();

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
        int count = taskFlowTesterService.getTestTaskFlows().size();
        List keys = new ArrayList();
        // first add key of parent region, then country id!!
        keys.add(new Integer(count-1));
        RowKeySet rksSelected = new RowKeySetImpl();
        rksSelected.add(keys);
        setSelectedRowKeySet(rksSelected);
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

  public RowKeySet getSelectedRowKeySet()
  {
    List keys = new ArrayList();
    // first add key of parent region, then country id!!
    keys.add(new Integer(0));
    RowKeySet rksSelected = new RowKeySetImpl();
    rksSelected.add(keys);
//    return rksSelected;
    return selectedRowKeySet;
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return taskFlowTesterService.getTestTaskFlows();
  }

  public void treeNodeSelected(SelectionEvent selectionEvent)
  {
    setSelectedRowKeySet(selectionEvent.getAddedSet());
  }

  public void setSelectedRowKeySet(RowKeySet selectedRowKeySet)
  {
    this.selectedRowKeySet = selectedRowKeySet;
  }
}
