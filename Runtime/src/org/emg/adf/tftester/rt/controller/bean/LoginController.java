 /*******************************************************************************
  Copyright: see readme.txt
  
  $revision_history$
  28-may-2016   Alexis Lopez
  1.0           initial creation
 ******************************************************************************/
package org.emg.adf.tftester.rt.controller.bean;

import java.io.Serializable;

import javax.faces.application.FacesMessage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import javax.security.auth.login.FailedLoginException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpSession;

import oracle.adf.view.rich.component.rich.RichPopup;

import org.apache.myfaces.trinidad.util.ComponentReference;

/**
 * Controller class that handles login/logout behavior.
 */
public class LoginController implements Serializable {
    @SuppressWarnings("compatibility:9073279249635476465")
    private static final long serialVersionUID = 1L;
    
    private String username;
    private String password;
    private ComponentReference<RichPopup> popLogin;

    public LoginController() {
        super();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public void setPopLogin(RichPopup popLogin) {
        this.popLogin = ComponentReference.newUIComponentReference(popLogin);
    }

    public RichPopup getPopLogin() {
        return popLogin != null ? popLogin.getComponent() : null;
    }

    /**
     * Logs the user in the application using HttpServletRequest.login method.
     * @return null.
     */
    public String login() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) ctx.getExternalContext().getRequest();
        try {
            req.login(getUsername(), getPassword());
            getPopLogin().hide();
            refreshPage();
        } catch (ServletException e) {
            if (e.getCause() instanceof FailedLoginException) {
                FacesMessage msg =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login error", "Please verify user and password.");
                ctx.addMessage(null, msg);
            } else {
                FacesMessage msg =
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Login error", "Unexpected error: " + e.getMessage());
                ctx.addMessage(null, msg);
            }
        }

        return null;
    }

    /**
     * Cancels login, hides login popup.
     */
    public void cancelLogin() {
        getPopLogin().hide();
    }

    /**
     * Logs the user out of the application.
     * @return null.
     */
    public String logout() {
        FacesContext ctx = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest) ctx.getExternalContext().getRequest();
        try {
            req.logout();
            refreshPage();
        } catch (ServletException e) {
            FacesMessage msg =
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Logout error", "Unexpected error: " + e.getMessage());
            ctx.addMessage(null, msg);
        }

        return null;
    }

    /**
     * Refreshes current page.
     */
    protected void refreshPage() {
        FacesContext fctx = FacesContext.getCurrentInstance();
        String page = fctx.getViewRoot().getViewId();
        ViewHandler viewH = fctx.getApplication().getViewHandler();
        UIViewRoot UIV = viewH.createView(fctx, page);
        UIV.setViewId(page);
        fctx.setViewRoot(UIV);

    }
}
