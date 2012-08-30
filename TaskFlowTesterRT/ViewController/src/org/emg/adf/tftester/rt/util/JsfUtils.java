package org.emg.adf.tftester.rt.util;

import java.io.IOException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;

import javax.el.ValueExpression;

import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.ApplicationFactory;
import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.PhaseId;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import oracle.adf.controller.ControllerContext;
import oracle.adf.share.logging.ADFLogger;
import oracle.adf.view.rich.component.rich.fragment.RichRegion;
import oracle.adf.view.rich.context.AdfFacesContext;

import oracle.adf.view.rich.util.FacesMessageUtils;

import org.apache.myfaces.trinidad.component.UIXCollection;
import org.apache.myfaces.trinidad.component.UIXCommand;
import org.apache.myfaces.trinidad.component.UIXEditableValue;
import org.apache.myfaces.trinidad.component.UIXForm;
import org.apache.myfaces.trinidad.component.UIXSubform;
import org.apache.myfaces.trinidad.render.ExtendedRenderKitService;
import org.apache.myfaces.trinidad.util.Service;


public class JsfUtils
{

  private static final ADFLogger sLog = ADFLogger.createADFLogger(JsfUtils.class);
  private static final String APPLICATION_FACTORY_KEY =
    "javax.faces.application.ApplicationFactory";

  public JsfUtils()
  {
  }

  public static FacesContext getFacesContext()
  {
    AdfFacesContext ctx;
    return FacesContext.getCurrentInstance();
  }

  /**
   * Defined as static because it is used in getInstance()
   * @return
   */
  public static Application getApplication()
  {
    FacesContext context = FacesContext.getCurrentInstance();
    if (context != null)
    {
      return FacesContext.getCurrentInstance().getApplication();
    }
    else
    {
      ApplicationFactory afactory =
        (ApplicationFactory) FactoryFinder.getFactory(APPLICATION_FACTORY_KEY);
      return afactory.getApplication();
    }
  }

  /**
   * Evaluates JSF EL expression and returns the value. If the expression
   * does not start with "#{" it is assumed to be a literal value, and
   * the value returned will be the same as passed in.
   * <p>
   * Defined as static because it is used in getInstance()
   * @param jsfExpression
   * @return
   */
  public static Object getExpressionValue(String jsfExpression)
  {
    // when specifying EL expression in managed bean as "literal" value
    // so t can be evaluated later, the # is replaced with $, quite strange
    if (jsfExpression == null)
    {
      return jsfExpression;
    }
    if (jsfExpression.startsWith("${"))
    {
      jsfExpression = "#{" + jsfExpression.substring(2);
    }
    if (!jsfExpression.startsWith("#{"))
    {
      if (jsfExpression.equalsIgnoreCase("true"))
      {
        return Boolean.TRUE;
      }
      else if (jsfExpression.equalsIgnoreCase("false"))
      {
        return Boolean.FALSE;
      }
      // there can be literal text preceding the expression...
      else if (jsfExpression.indexOf("#{")<0)
      {
        return jsfExpression;
      }
    }
    ValueExpression ve =  getApplication().getExpressionFactory().createValueExpression(getFacesContext().getELContext(),jsfExpression,Object.class);
    return ve.getValue(getFacesContext().getELContext());
  }

  /**
   * Searches a component relative to a base component. If not found, we take the parent of the base
   * component and search again. We continue until a component is found or the parent is null
   * @param base
   * @param id
   * @return
   */
  public static UIComponent findRelativeComponentAndMoveUpIfNotFound(UIComponent base, String id)
  {
    UIComponent comp = null;
    UIComponent parent = base;
     while (comp==null && parent!=null)
     {
       comp = findComponent(parent,  id);
       parent = parent.getParent();
     }
     return comp;
  } 

  /**
   * <p>Return the {@link UIComponent} (if any) with the specified
   * <code>id</code>, searching recursively starting at the specified
   * <code>base</code>, and examining the base component itself, followed
   * by examining all the base component's facets and children.
   * Unlike findComponent method of {@link UIComponentBase}, which
   * skips recursive scan each time it finds a {@link NamingContainer},
   * this method examines all components, regardless of their namespace
   * (assuming IDs are unique).
   *
   * @param base Base {@link UIComponent} from which to search
   * @param id Component identifier to be matched
   */
  public static UIComponent findComponent(UIComponent base, String id)
  {
    if (id==null || "".equals(id))
    {
      return null;
    }
    // Is the "base" component itself the match we are looking for?
    if (id.equals(base.getId()))
    {
      return base;
    }
    // check for direct child
    UIComponent result = base.findComponent(id);
    if (result!=null)
    {
      return result;
    }

    // Search through our facets and children
    UIComponent kid = null;
    Iterator kids = base.getFacetsAndChildren();
    while (kids.hasNext() && (result == null))
    {
      kid = (UIComponent) kids.next();
      if (id.equals(kid.getId()))
      {
        result = kid;
        break;
      }
      result = findComponent(kid, id);
      if (result != null)
      {
        break;
      }
    }
    return result;
  }

  //  public boolean componentTreeHasPendingChanges()
  //  {
  //    UIComponent component = JsfUtils.findComponentInRoot("dataForm");
  //    if (component != null)
  //    {
  //      sLog.info("Executing componentTreeHasPendingChanges of dataForm");
  //      return childHasPendingChanges(component);
  //    }
  //    return false;
  //  }


  public static void resetComponentTree()
  {
    UIComponent component = FacesContext.getCurrentInstance().getViewRoot();
    if (component != null)
    {
      sLog.info("Executing resetComponentTree of dataForm");
      resetComponentTree(component);
    }
  }

  public static void resetComponentTree(UIComponent component)
  {
    if (component!=null)
    {
      UIComponent form = getContainingForm(component);
      if (form!=null)
      {
        resetChildren(form);              
      }
    }
  }

  public static UIComponent getContainingForm(UIComponent component)
  {
    UIComponent previous = component;
    for (UIComponent parent = component.getParent(); parent != null;
         parent = parent.getParent())
    {
      if ((parent instanceof UIForm) || (parent instanceof UIXForm) ||
          (parent instanceof UIXSubform))
        return parent;
      previous = parent;
    }

    return previous;
  }

  public static void resetChildren(UIComponent comp)
  {
    UIComponent kid;
    for (Iterator kids = comp.getFacetsAndChildren(); kids.hasNext();
         resetChildren(kid))
    {
      kid = (UIComponent) kids.next();
      if (kid instanceof UIXEditableValue)
      {
        ((UIXEditableValue) kid).resetValue();
        continue;
      }
      if (kid instanceof EditableValueHolder)
      {
        resetEditableValueHolder((EditableValueHolder) kid);
        continue;
      }
      if (kid instanceof UIXCollection)
        ((UIXCollection) kid).resetStampState();
    }

  }


  public static void resetEditableValueHolder(EditableValueHolder evh)
  {
    evh.setValue(null);
    evh.setSubmittedValue(null);
    evh.setLocalValueSet(false);
    evh.setValid(true);
  }

  public static void resetComponent(UIComponent comp)
  {
    if (comp instanceof EditableValueHolder)
    {
      resetEditableValueHolder((EditableValueHolder) comp);
    }
  }


  public static UIViewRoot getViewRoot()
  {
    if (getFacesContext() != null)
    {
      return getFacesContext().getViewRoot();
    }
    return null;
  }

  /**
   * Convenience method for setting Session variables.
   * @param key object key
   * @param object value to store
   */
  public static void storeOnSession(String key, Object object)
  {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map session = ctx.getExternalContext().getSessionMap();
    session.put(key, object);
  }

  /**
   * Convenience method for getting Session variables.
   * @param key object key
   */
  public static Object getFromSession(String key)
  {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map session = ctx.getExternalContext().getSessionMap();
    return session.get(key);
  }

  /**
   * Convenience method for setting Request attributes.
   * @param key object key
   * @param object value to store
   */
  public static void storeOnRequest(String key, Object object)
  {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map request = ctx.getExternalContext().getRequestMap();
    request.put(key, object);
  }

  /**
   * Convenience method for getting Request attributes.
   * @param key object key
   */
  public static Object getFromRequest(String key)
  {
    FacesContext ctx = FacesContext.getCurrentInstance();
    Map request = ctx.getExternalContext().getRequestMap();
    return request.get(key);
  }

  public static void redirect(String redirectUrl)
  {
    try
    {
      ExternalContext ext =
        FacesContext.getCurrentInstance().getExternalContext();
      ext.redirect(redirectUrl);
    }
    catch (IOException e)
    {
      sLog.severe("Error redirecting to " + redirectUrl + ": " +
                 e.getMessage());
    }
  }

  public static void redirectToSelf() {
      FacesContext fctx = FacesContext.getCurrentInstance();
      ExternalContext ectx = fctx.getExternalContext();
      String viewId = fctx.getViewRoot().getViewId();
      ControllerContext controllerCtx = null;
      controllerCtx = ControllerContext.getInstance();
      String activityURL = controllerCtx.getGlobalViewActivityURL(viewId);
      try {
          ectx.redirect(activityURL);
          fctx.responseComplete();
      } catch (IOException e) {
          //Can't redirect
          sLog.severe("Error redirecting to self (" + activityURL + "): " +
                     e.getMessage());
          fctx.renderResponse();
      }
  }

  public static boolean isValidExpression(String expression)
  {
    boolean valid = true;
    try
    {
      getExpressionValue(expression);
    }
    catch (Exception ex)
    {
      valid = false;
    }
    return valid;
  }

  /**
   * Programmatic invocation of a method that an EL evaluates
   * to. The method must not take any parameters.
   *
   * @param methodExpression EL of the method to invoke
   * @return Object that the method returns
   */
  public static Object invokeELMethod(String methodExpression)
  {
    return invokeELMethod(methodExpression, new Class[0], new Object[0]);
  }

  /**
   * Programmatic invocation of a method that an EL evaluates to.
   *
   * @param methodExpression EL of the method to invoke
   * @param paramTypes Array of Class defining the types of the
   * parameters
   * @param params Array of Object defining the values of the
   * parametrs
   * @return Object that the method returns
   */
  public static Object invokeELMethod(String methodExpression, Class[] paramTypes,
                                Object[] params)
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ELContext elContext = facesContext.getELContext();
    MethodExpression exp = getMethodExpression(methodExpression, null,paramTypes);
    return exp.invoke(elContext, params);
  }

  public static MethodExpression getMethodExpression(String methodExpression, Class returnType, Class[] paramTypes)
  {
    FacesContext facesContext = FacesContext.getCurrentInstance();
    ELContext elContext = facesContext.getELContext();
    ExpressionFactory expressionFactory =
      facesContext.getApplication().getExpressionFactory();
    MethodExpression exp =
      expressionFactory.createMethodExpression(elContext, methodExpression, returnType,
                                               paramTypes);
    return exp;
  }

  public static UIComponent findComponentInRoot(String id)
  {
    UIComponent ret = null;

    FacesContext context = FacesContext.getCurrentInstance();
    if (context != null)
    {
      UIComponent root = context.getViewRoot();
      ret = findComponent(root, id);
    }

    return ret;
  }

  public static UIComponent findComponentMatchingClientId(String clientCompId) 
  {
    FacesContext context = FacesContext.getCurrentInstance();
    UIComponent root = context.getViewRoot();
    return findComponentMatchingClientId(root, clientCompId);
  }

  /**
   * Method to parse the active component clientId to identify the UIComponent
   * instance. 
   * Code based on sample from Frank Nimphius on ADF Code Corner
   *
   * @param startComp The top-level component where we start finding. 
   * @param clientCompId clientId or ClientLocatorId. A clientId contains all the
   *        naming comntainers between the document root and the UI component. A
   *        client locator also contains a row indes if the component is part of a
   *        table rendering
   * @return
   */
  public static UIComponent findComponentMatchingClientId(UIComponent startComp, String clientCompId) {

      String components[] = { };
      if (clientCompId != null) {
          components = clientCompId.split(":");
      }
      UIComponent component = null;

      //get the component

      if (components.length > 0) {
          String componentId = components[0];
          component = startComp.findComponent(componentId);

          if (component != null) {
              for (int i = 1; i < components.length; ++i) {
                  //if the component is in a table, then the clientId
                  //contains an integer vaue that indicates the row index
                  //to parse this out, we use a try/catch block
                  try {
                      Integer.parseInt(components[i]);
                  } catch (NumberFormatException nf) {
                      //the id is not a number, so lets try a get the component
                      if (component != null) {
                          component =
                                  findComponent(component, components[i]);
                      }
                  }
              }
          }
      }

      //if we are here then we have a component or null
      return component;
  }

  public static RichRegion findParentRegion(UIComponent component) {
    UIComponent parent = component.getParent();
    RichRegion region = null;
    while (parent!=null)
    {
      if (parent instanceof RichRegion)
      {
        region = (RichRegion) parent;
        break;
      }
      parent = parent.getParent();
    }
    return region;
  }

  public static void addError(String clientId, String message)
  {
    FacesMessage fm = new FacesMessage(message);
    fm.setSeverity(FacesMessage.SEVERITY_ERROR);
    FacesContext.getCurrentInstance().addMessage(clientId,fm);
  }

  public static void setInputFocus(String clientId)
  {
    String script ="comp = AdfPage.PAGE.findComponent('" + clientId + "');\n" 
                       +
        "if (comp!=null) comp.focus(); ";
    writeJavaScriptToClient(script); 
  }

  public static void writeJavaScriptToClient(String script)
  {
    FacesContext fctx = FacesContext.getCurrentInstance();
    ExtendedRenderKitService erks =
      Service.getRenderKitService(fctx, ExtendedRenderKitService.class);
    erks.addScript(fctx, script);
  }

}
