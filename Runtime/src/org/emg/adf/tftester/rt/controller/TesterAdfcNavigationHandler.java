package org.emg.adf.tftester.rt.controller;

import javax.faces.context.FacesContext;

import oracle.adf.controller.internal.AdfcNavigationHandler;
import oracle.adf.controller.internal.metadata.ActivityId;

import org.emg.adf.tftester.rt.util.JsfUtils;

/**
 * This class has as only purpose to intercept the action and outcome returned by the tested task flow call.
 * It will always return null in handleNavigation to ensure default nav handler is used for actual navigation
 */
public class TesterAdfcNavigationHandler
  extends AdfcNavigationHandler
{
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
