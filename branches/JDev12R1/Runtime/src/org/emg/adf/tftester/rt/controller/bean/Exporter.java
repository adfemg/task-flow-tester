/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.RichPopup;

import org.emg.adf.tftester.rt.controller.TaskFlowTesterServiceFactory;
import org.emg.adf.tftester.rt.model.TaskFlowTesterService;

/**
 * Controller class to support Export to XML function in user interface.
 * The actual export to XML is delegated to TaskFlowTesterService
 * @see TaskFlowTesterService
 */
public class Exporter implements Serializable
{
  private static ADFLogger sLog = ADFLogger.createADFLogger(Exporter.class);
  @SuppressWarnings("compatibility:2359777570024811736")
  private static final long serialVersionUID = 1L;
  private TaskFlowTesterService taskFlowTesterService = TaskFlowTesterServiceFactory.getInstance();
  private String xml;
  private transient RichPopup popup;

  public Exporter()
  {
    super();
  }

  public void exportToXml(ActionEvent event)
  {
    String xml = taskFlowTesterService.exportToXML();
    setXml(xml);
    getPopup().show(new RichPopup.PopupHints());
  }

  public void exportToFile(FacesContext facesContext, OutputStream out)
  {
    InputStream in = null;
    try
    {
      in = new ByteArrayInputStream(getXml().getBytes("UTF-8"));
    }
    catch (UnsupportedEncodingException e)
    {
      sLog.severe("Error while exporting XML to file: " + e.getMessage());
    }
    byte[] buffer = new byte[8192];
    int bytesRead = 0;
    try
    {
      while ((bytesRead = in.read(buffer, 0, 8192)) != -1)
      {
        out.write(buffer, 0, bytesRead);
      }
      in.close();
    }
    catch (IOException e)
    {
      sLog.severe("Error while exporting XML to file: " + e.getMessage());
    }
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
}
