/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.security;

import java.io.IOException;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.*;
import javax.servlet.ServletException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Administrator
 */
@ManagedBean
@RequestScoped
public class AuthenticationController {
    public void login() {
        FacesContext context = FacesContext.getCurrentInstance();

        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
     
        if(request.getUserPrincipal()==null)
            context.addMessage(null, new FacesMessage("Unknown login"));
            
    }
    public String logout(){
        log( "AuthenticationController.logout: logging - out." );
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        request.getSession().invalidate();
        return  "../index.xhtml?faces-redirect=true&";
    }
    
    private void log(String message){
        
        FacesContext.getCurrentInstance().getExternalContext().log(message);
    }
}
