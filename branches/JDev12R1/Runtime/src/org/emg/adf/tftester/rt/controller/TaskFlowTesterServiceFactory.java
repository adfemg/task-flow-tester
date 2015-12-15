/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;

 /**
  * Factory that returns instance of TaskFlowTesterService. Currently the instance is stored in sessionScope and within
  * a session the same instance is returned for every call to getInstance.
  */
public class TaskFlowTesterServiceFactory implements Serializable
{
  private final static String TASK_FLOW_TESTER_SERVICE_KEY = "org.emg.adf.tftester.TaskFlowTesterService";
  @SuppressWarnings("compatibility:8021911303533961639")
  private static final long serialVersionUID = 1L;

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
