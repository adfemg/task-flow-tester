/*******************************************************************************
 Copyright: see readme.txt
 
 $revision_history$
 06-dec-2012   Steven Davelaar
 1.0           initial creation
******************************************************************************/
package org.emg.adf.tftester.rt.controller;


import javax.faces.context.FacesContext;

import javax.faces.application.NavigationHandler;



/**
 * This class has as only purpose to intercept the action and outcome returned by the tested task flow call, so we can display
 * the navigation outcome in the tester.
 * It will always return null in handleNavigation to ensure default nav handler is used for actual navigation.
 * This class is used because it is specified in faces-config.xml
 */
public class TesterNavigationHandler
  extends NavigationHandler
{

  private NavigationHandler _delegate;

  public TesterNavigationHandler(NavigationHandler delegate)
  {
    _delegate = delegate;
  }

  @Override
  public void handleNavigation(FacesContext context, String fromAction,
                               String outcome)
  {
    FacesContext currentInstance = FacesContext.getCurrentInstance();
    currentInstance.getExternalContext().getRequestMap().put("lastNavigationAction",
                                                             fromAction);
    currentInstance.getExternalContext().getRequestMap().put("lastNavigationOutcome",
                                                             outcome);
    _delegate.handleNavigation(context, fromAction, outcome);
  }
}
