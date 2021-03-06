 /*******************************************************************************
  Copyright: see readme.txt
  
  $revision_history$
  17-dec-2012   Steven Davelaar
  1.1           added sorting: now implements Comparable
  06-jun-2012   Steven Davelaar
  1.0           initial creation
 ******************************************************************************/
 package org.emg.adf.tftester.rt.model;

 import java.io.Serializable;

 import java.util.ArrayList;
 import java.util.List;

 /**
  * Model class that holds all data about a task flow testcase.
  */
 public class TaskFlowTestCase implements Serializable, Comparable
 {
   @SuppressWarnings("compatibility:-40669652387037820")
   private static final long serialVersionUID = 1L;
   private TaskFlow taskFlow;
   private String name; 
   private String description;
   private boolean runInRegion = true;;
   private boolean runAscall = false;
   private boolean stretchLayout = false;
   private List<ValueObject> paramValueObjects = new ArrayList<ValueObject>();

   public TaskFlowTestCase(TaskFlow taskFlow)
   {
     super();
     this.taskFlow = taskFlow;
   }

   public void setName(String name)
   {
     this.name = name;
   }

   public String getName()
   {
     return name;
   }

   public void setDescription(String description)
   {
     this.description = description;
   }

   public String getDescription()
   {
     return description;
   }

   public void setRunInRegion(boolean runInRegion)
   {
     this.runInRegion = runInRegion;
   }

   public boolean isRunInRegion()
   {
     return runInRegion;
   }

   public void setRunAscall(boolean runAscall)
   {
     this.runAscall = runAscall;
   }

   public boolean isRunAscall()
   {
     return runAscall;
   }

   public void setStretchLayout(boolean stretchLayout)
   {
     this.stretchLayout = stretchLayout;
   }

   public boolean isStretchLayout()
   {
     return stretchLayout;
   }

   public void setParamValueObjects(List<ValueObject> paramValueObjects)
   {
     this.paramValueObjects = paramValueObjects;
   }

   public List<ValueObject> getParamValueObjects()
   {
     return paramValueObjects;
   }

   public void setTaskFlow(TaskFlow taskFlow)
   {
     this.taskFlow = taskFlow;
   }

   public TaskFlow getTaskFlow()
   {
     return taskFlow;
   }

   /**
    * Dummy method required because "children" is defined as child property in tree menu model
    * @return
    */
   public List getChildren()
   {
     return null;
   }

   public int compareTo(Object o)
   {
     if (o instanceof TaskFlowTestCase)
     {
       String name = getName();
       String otherName = ((TaskFlowTestCase)o).getName();
       return name.toUpperCase().compareTo(otherName.toUpperCase());
     }
     return 0;  
   }
 }
