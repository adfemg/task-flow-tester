/*******************************************************************************
 Copyright: see readme.txt

 $revision_history$
 09-jan-2013   Wilfred van der Deijl
 1.1           import XML as more versatile javax.xml.transform.Source
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.Serializable;
import java.io.StringReader;

import javax.faces.event.ActionEvent;

import javax.xml.transform.stream.StreamSource;

import oracle.adf.model.BindingContext;
import oracle.adf.model.binding.DCBindingContainer;
import oracle.adf.view.rich.component.rich.RichPopup;

import oracle.jbo.JboException;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;


/**
 * Controller class to support Import from XML function in user interface.
 * The actual import from XML is delegated to TaskFlowTesterService
 * @see TaskFlowTesterService
 */
public class Importer implements Serializable
{
  @SuppressWarnings("compatibility:9210328595572863597")
  private static final long serialVersionUID = 1L;
  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();
  private String xml;
  private transient RichPopup popup;

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
      taskFlowTesterService.importFromXml(new StreamSource(new StringReader(xml)));
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
