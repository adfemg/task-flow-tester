/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.Serializable;

import javax.faces.event.ActionEvent;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlow;

/**
 * Controller class to support task flow loader function in user interface.
 * The actual loading of available task flows is delegated to TaskFlowTesterService
 * @see TaskFlowTesterService
 */
public class TaskFlowLoader implements Serializable
{
  @SuppressWarnings("compatibility:8502151451512450874")
  private static final long serialVersionUID = 1L;
  private String startDir = "/WEB-INF/";
  private boolean recurseInSubDirs = true;
  private boolean searchAdfLibs = false;

  public TaskFlowLoader()
  {
    super();
  }

  public void setStartDir(String startFolder)
  {
    this.startDir = startFolder;
  }

  public String getStartDir()
  {
    return startDir;
  }

  public void setSearchAdfLibs(boolean searchAdfLibs)
  {
    this.searchAdfLibs = searchAdfLibs;
  }

  public boolean isSearchAdfLibs()
  {
    return searchAdfLibs;
  }

  public void setRecurseInSubDirs(boolean recurseInSubDirs)
  {
    this.recurseInSubDirs = recurseInSubDirs;
  }

  public boolean isRecurseInSubDirs()
  {
    return recurseInSubDirs;
  }
  
  public void load(ActionEvent event)
  {
    TaskFlowTesterServiceFactory.getInstance().loadAvailableTaskFlows(getStartDir(), isRecurseInSubDirs(), isSearchAdfLibs());
    TaskFlowTester tester = TaskFlowTester.getInstance();    
    tester.refreshTreeArea();
    tester.setCurrentTestTaskFlowIfNeeded();
  }

}


