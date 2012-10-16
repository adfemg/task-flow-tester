/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-jun-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller;

import java.io.Serializable;

import javax.faces.context.FacesContext;

import oracle.adf.controller.internal.AdfcNavigationHandler;
import oracle.adf.controller.metadata.ActivityId;

import org.emg.adf.tftester.rt.util.JsfUtils;

 /**
  * This class has as only purpose to intercept the action and outcome returned by the tested task flow call, so we can display
  * the navigation outcome in the tester.
  * It will always return null in handleNavigation to ensure default nav handler is used for actual navigation.
  * This class is used by ADF framework because it is specified as class name in file META-INF/services/oracle.adf.controller.internal.AdfcNavigationHandler
  */
public class TesterAdfcNavigationHandler
  extends AdfcNavigationHandler implements Serializable
{
  @SuppressWarnings("compatibility:4702094392488850178")
  private static final long serialVersionUID = 1L;

  public TesterAdfcNavigationHandler()
  {
    super();
  }

  @Override
  public ActivityId handleNavigation(FacesContext context, String fromAction, String outcome)
  {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    currentInstance.getExternalContext().getRequestMap().put("lastNavigationAction",fromAction);
    currentInstance.getExternalContext().getRequestMap().put("lastNavigationOutcome",outcome);
    return null;
  }
}
