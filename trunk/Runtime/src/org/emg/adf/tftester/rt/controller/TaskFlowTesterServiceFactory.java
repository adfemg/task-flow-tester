package org.emg.adf.tftester.rt.controller;

import javax.faces.context.FacesContext;

import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;

/**
 * Factory that returns instance of TaskFlowTesterService. Currently the instance is stoired in sessionScope and within
 * a session the same instance is returned for every call to getInstance.
 */
public class TaskFlowTesterServiceFactory
{
  private final static String TASK_FLOW_TESTER_SERVICE_KEY = "org.emg.adf.tftester.TaskFlowTesterService";
  
  public static TaskFlowTesterService getInstance()
  {
    TaskFlowTesterService instance = (TaskFlowTesterService) JsfUtils.getFromSession(TASK_FLOW_TESTER_SERVICE_KEY);
    if (instance==null)
    {
      instance = new TaskFlowTesterService();
      FacesContext context = FacesContext.getCurrentInstance();
      context.getExternalContext().getSessionMap().put(TASK_FLOW_TESTER_SERVICE_KEY, instance);
    }
    return instance;
  }  

}
