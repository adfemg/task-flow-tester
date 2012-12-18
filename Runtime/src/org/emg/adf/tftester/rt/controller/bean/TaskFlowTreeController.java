/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 17-dec-2012  Steven Davelaar
 1.1           Added support for "Group By Directory" option
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import oracle.adf.view.rich.component.rich.data.RichTree;
import oracle.adf.view.rich.event.DialogEvent;

import oracle.jbo.JboException;

import org.apache.myfaces.trinidad.model.ChildPropertyMenuModel;
import org.apache.myfaces.trinidad.model.MenuModel;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlow;
import org.emg.adf.tftester.rt.model.TaskFlowFolder;
import org.emg.adf.tftester.rt.model.TaskFlowTestCase;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;

/**
 * Controller class that handles user actions related to the task flow and testcase tree menu
 */
public class TaskFlowTreeController implements Serializable
{
  @SuppressWarnings("compatibility:5519005825132090313")
  private static final long serialVersionUID = 1L;
  private String taskFlowId;
  private String displayName;
  private boolean showFolders = false;

  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();

  public TaskFlowTreeController()
  {
    super();
  }

  public MenuModel getMenuModel()
  {
    List<TaskFlow> taskflows = taskFlowTesterService.getTestTaskFlows();
    if (showFolders)
    {
       List<TaskFlowFolder> rootFolders = new ArrayList<TaskFlowFolder>();
       List<TaskFlow> rootTaskFlows = new ArrayList<TaskFlow>();
       for (TaskFlow tf : taskflows)
       {
         String folderPath = getFolderPath(tf.getTaskFlowIdString());
         if (folderPath!=null)
         {
           TaskFlowFolder tfFolder = getTaskFlowFolder(folderPath, rootFolders);
           tfFolder.addTaskFlows(tf);         
         }
         else
         {
           // tf is not in subfolder, show in root
           rootTaskFlows.add(tf);
         }
       }
       Collections.sort(rootFolders);
       List rootNodes = rootFolders;
       Collections.sort(rootTaskFlows);
       rootNodes.addAll(rootTaskFlows);
       return new ChildPropertyMenuModel(rootNodes,"children",null);      
    }
    else
    {
      return new ChildPropertyMenuModel(taskflows,"children",null);      
    }
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
   * @return
   */
  public RowKeySet getSelectedRowKeySet()
  {
    List keys = new ArrayList();
    TaskFlow currentTf = TaskFlowTester.getInstance().getCurrentTestTaskFlow();
    if (currentTf!=null)
    {
      MenuModel mm = getMenuModel();  
      keys = findTaskFlowRowKey(currentTf, mm);
      TaskFlowTestCase currentTc = TaskFlowTester.getInstance().getCurrentTestCase();
      if (currentTc!=null)
      {
        RowKeySet disclosedRowKeys = getTreeComponent().getDisclosedRowKeys();
        if (!disclosedRowKeys.contains(keys))
        {
          // a testcase has just been added, we need to expand the parent task flow node as well
          List dkeys = new ArrayList();
          dkeys.addAll(keys);
          disclosedRowKeys.add(dkeys);
        }
        int tcIndex = currentTf.getTestCases().indexOf(currentTc);      
        keys.add(new Integer(tcIndex));        
      }        
    }
    RowKeySetImpl selectedRowKeySet = new RowKeySetImpl();
    selectedRowKeySet.add(keys);      
    return selectedRowKeySet;
  }

  private List findTaskFlowRowKey(TaskFlow currentTf,
                                     MenuModel mm)
  {
    List rowKey = null;
    for (int i = 0; i < mm.getRowCount(); i++)
    {
      mm.setRowIndex(i);  
      if (mm.getRowData() instanceof TaskFlow)
      {
        TaskFlow tf = (TaskFlow) mm.getRowData();
        if (tf==currentTf)
        {
          rowKey = (List) mm.getRowKey();
          break;
        }
      }
      else if (mm.isContainer())
      {
       mm.enterContainer();
       rowKey = findTaskFlowRowKey(currentTf, mm);
       if (rowKey!=null)
       {
         return rowKey;
       }
      }
    }    
    if (mm.getContainerRowKey()!=null)
    {
      mm.exitContainer();      
    }
    return rowKey;
  }

  public List<TaskFlow> getTestTaskFlows()
  {
    return taskFlowTesterService.getTestTaskFlows();
  }

  private String getFolderPath(String taskFlowPath)
  {
    if (taskFlowPath.startsWith("/"))
    {
      taskFlowPath = taskFlowPath.substring(1);
    }  
    // remove WEB-INF
    if (taskFlowPath.startsWith("WEB-INF/"))
    {
      taskFlowPath = taskFlowPath.substring(8);
    }
    // remove tf
    int lastSlash = taskFlowPath.lastIndexOf("/");
    if (lastSlash>-1)
    {
      taskFlowPath = taskFlowPath.substring(0, lastSlash);      
    }
    else
    {
      // TF is not in subfolder
      taskFlowPath = null;
    }
    return taskFlowPath;
  }

  private TaskFlowFolder getTaskFlowFolder(String folderPath, List<TaskFlowFolder> rootFolders)
  {
    String folders[] = folderPath.split("/");
    List<TaskFlowFolder> siblingFolders = rootFolders;
    TaskFlowFolder parent = null;
    for (String folderName : folders)
    {
      TaskFlowFolder newFolder = new  TaskFlowFolder(folderName);       
      if (siblingFolders.contains(newFolder))
      {
        // folder already exists, go to the next level
        int pos = siblingFolders.indexOf(newFolder);
        parent = siblingFolders.get(pos);
        siblingFolders = parent.getSubFolders();
      }
      else
      {
        // create new folder at this level at it to the siblings, and make the new folder the new parent
        siblingFolders.add(newFolder);
        parent = newFolder;
        siblingFolders = newFolder.getSubFolders();
      }
    }
    return parent;
  }

  public static void main(String[] args)
  {
    TaskFlowTreeController c = new TaskFlowTreeController();
    List<String> folders = new ArrayList<String>();
    String f1 = "/WEB-INF/oracle/wc/blog/sometf#srgddserg";
    f1 = c.getFolderPath(f1);
    folders.add(f1);
    folders.add("oracle/wc/erna");
    folders.add("oracle/wc");
    folders.add("oracle/wc/blog/steven");
    System.err.println(folders);
    Collections.sort(folders);
    System.err.println("SORT:"+folders);
    
    List<TaskFlowFolder> rootFolders = new ArrayList<TaskFlowFolder>();
    for (String name : folders)
    {
      c.getTaskFlowFolder(name, rootFolders);
    }    
    System.err.println(rootFolders);
  }

  public void setShowFolders(boolean showFolders)
  {
    this.showFolders = showFolders;
    getTreeComponent().getDisclosedRowKeys().clear();
    getTreeComponent().getSelectedRowKeys().clear();
    discloseSelectedNodePath();
    TaskFlowTester.getInstance().refreshTreeArea();
  }

  public void discloseSelectedNodePath()
  {
    TaskFlow currentTf = TaskFlowTester.getInstance().getCurrentTestTaskFlow();
    if (currentTf!=null)
    {
      MenuModel mm = getMenuModel();  
      RowKeySet rks = getTreeComponent().getDisclosedRowKeys();
      List keys = findTaskFlowRowKey(currentTf, mm);
      // get all ancestor row keys that must be adde to disclosed list
      List ancestors = mm.getAllAncestorContainerRowKeys(keys);
      rks.addAll(ancestors);
      // if there is a current testcase, we need to disclose the tf itself as well
      // and add it to the list
      if (TaskFlowTester.getInstance().getCurrentTestCase()!=null)
      {
        rks.add(keys);
      }
    }
  }

  private RichTree getTreeComponent()
  {
    RichTree tree = (RichTree) JsfUtils.findComponentInRoot("tree");
    return tree;    
  }

  public boolean isShowFolders()
  {
    return showFolders;
  }
}
