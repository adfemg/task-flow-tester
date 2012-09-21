package org.emg.adf.tftester.rt.controller.bean;

import javax.faces.component.UIComponent;
import javax.faces.event.ActionEvent;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.jbo.JboException;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;
import org.emg.adf.tftester.rt.util.JsfUtils;

public class Importer
{
  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();
  private String xml;
  private RichPopup popup;

  public Importer()
  {
    super();
  }

  public void showImportPopup(ActionEvent event)
  {
    getPopup().show(new RichPopup.PopupHints());
  }

  public void setXml(String xml)
  {
    this.xml = xml;
  }

  public String getXml()
  {
    return xml;
  }

  public void setPopup(RichPopup popup)
  {
    this.popup = popup;
  }

  public RichPopup getPopup()
  {
    return popup;
  }

  public void doImport(ActionEvent event)
  {
    try
    {
      taskFlowTesterService.importFromXml(xml);
      getPopup().hide();      
      TaskFlowTester.getInstance().refreshTreeArea();
      TaskFlowTester.getInstance().setCurrentTestTaskFlowIfNeeded();
    }
    catch (JboException e)
    {
      // TODO: Add catch code
      DCBindingContainer container = (DCBindingContainer) BindingContext.getCurrent().getCurrentBindingsEntry();
      container.processException(e);
    }
  }

}
