/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-dec-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskFlowFolder
  implements Comparable
{
  private String name;
  private List<TaskFlow> taskFlows  =  new ArrayList<TaskFlow>();
  private List<TaskFlowFolder> subFolders =  new ArrayList<TaskFlowFolder>();

  public TaskFlowFolder(String name)
  {
    this.name = name;
  }
  
  public void setName(String name)
  {
    this.name = name;
  }

  public String getName()
  {
    return name;
  }

  public void addTaskFlows(TaskFlow taskFlow)
  {
    this.taskFlows.add(taskFlow);
  }

  public List<TaskFlow> getTaskFlows()
  {
    return taskFlows;
  }

  public void addSubFolders(TaskFlowFolder subFolder)
  {
    this.subFolders.add(subFolder);
  }

  public List<TaskFlowFolder> getSubFolders()
  {
    return subFolders;
  }

  /**
   * Method called because "children" is defined as child property in tree menu model
   * @return
   */
  public List getChildren()
  {
    List nodes = new ArrayList();
    Collections.sort(getSubFolders());
    Collections.sort(getTaskFlows());
    nodes.addAll(getSubFolders());
    nodes.addAll(getTaskFlows());
    return nodes;
  }
  
  public int compareTo(Object o)
  {
    if (o instanceof TaskFlowFolder)
    {
      String name = getName();
      String otherName = ((TaskFlowFolder)o).getName();
      return name.toUpperCase().compareTo(otherName.toUpperCase());
    }
    else
    {
      // task flows should be sequenced after folders
      return -1;      
    }
  }

  @Override
  public boolean equals(Object obj)
  {
    return toString().equalsIgnoreCase(obj.toString());
  }

  @Override
  public String toString()
  {
    return getName();
  }
}
