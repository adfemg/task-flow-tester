/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.Serializable;

import java.util.ArrayList;

import java.util.List;

import javax.faces.event.ActionEvent;

import oracle.adf.view.rich.component.rich.RichPopup;
import oracle.adf.view.rich.event.DialogEvent;

import org.emg.adf.tftester.rt.model.InputParameter;
import org.emg.adf.tftester.rt.model.TaskFlow;
import org.emg.adf.tftester.rt.model.TaskFlowTestCase;
import org.emg.adf.tftester.rt.model.ValueObject;

/**
 * Controller class that handles user actions related to a task flow testcase.
 */
public class TestCaseController implements Serializable
{
  @SuppressWarnings("compatibility:8482710135173048412")
  private static final long serialVersionUID = 1L;
  private String name;
  private String description;
  private RichPopup saveAsPopup;

  public TestCaseController()
  {
    super();
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
  
  public void showSaveAsPopup(ActionEvent event)
  {
    getSaveAsPopup().show(new RichPopup.PopupHints());
  }

  public void saveTestcase(ActionEvent event)
  {
    TaskFlowTester tester = TaskFlowTester.getInstance();
    TaskFlowTestCase tc = tester.getCurrentTestCase();
    saveTestcase(tc);
  }

  public void saveTestcase(TaskFlowTestCase tc)
  {
    TaskFlowTester tester = TaskFlowTester.getInstance();
    TaskFlow tf = tester.getCurrentTestTaskFlow();
    tc.setRunAscall(tester.isRunAsCall());
    tc.setRunInRegion(tester.isRunInRegion());
    tc.setStretchLayout(tester.isStretchLayout());
    List<ValueObject> paramValueObjects = new ArrayList<ValueObject>();
    for (InputParameter param :tf.getInputParams())
    {
      paramValueObjects.add(param.getValueObject().clone());
    }
    tc.setParamValueObjects(paramValueObjects);
    tester.refreshTreeArea();
  }

  public void saveAsNewTestcase(DialogEvent event)
  {
    if (event.getOutcome()==DialogEvent.Outcome.ok || event.getOutcome()==DialogEvent.Outcome.yes)
    {
      TaskFlowTester tester = TaskFlowTester.getInstance();
      TaskFlow tf = tester.getCurrentTestTaskFlow();
      TaskFlowTestCase tc = new TaskFlowTestCase(tf);
      tf.addTestCase(tc);
      tc.setName(getName());
      tc.setDescription(getDescription());
      saveTestcase(tc);      
      tester.setCurrentTestCase(tc);
    }    
  }

  public void setSaveAsPopup(RichPopup saveAsPopup)
  {
    this.saveAsPopup = saveAsPopup;
  }

  public RichPopup getSaveAsPopup()
  {
    return saveAsPopup;
  }
}
