package org.emg.adf.tftester.rt.controller.bean;

import javax.faces.event.ActionEvent;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;


public class TaskFlowLoader
{
  private String startDir = "/WEB-INF/";
  private boolean recurseInSubDirs = true;
  private boolean searchAdfLibs = true;

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
    TaskFlowTesterServiceFactory.getInstance().loadTaskFlowsFromClassPath(getStartDir(), isRecurseInSubDirs(), isSearchAdfLibs());
    TaskFlowTester.getInstance().refreshTreeArea();
  }

}


