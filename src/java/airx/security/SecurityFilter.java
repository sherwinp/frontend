/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package airx.security;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.annotation.WebFilter;

/**
 *
 * @author sherwinp
 */
//@WebFilter(filterName = "SecurityFilter", urlPatterns = {"/secure/*"}, dispatcherTypes = {DispatcherType.FORWARD, DispatcherType.REQUEST, DispatcherType.INCLUDE})
public class SecurityFilter implements Filter {

    private static final boolean debug = true;
    private static String login_page = "";
    // The filter configuration object we are associated with.  If
    // this value is null, this filter instance is not currently
    // configured. 
    private FilterConfig filterConfig = null;

    public SecurityFilter() {
    }

    /**
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        if (debug) {
            log("SecurityFilter:doFilter()");
        }

        log("SecurityFilter: Request received ...");

        boolean authorized = authorized = ((HttpServletRequest) request).getUserPrincipal() != null;

        if (authorized) {
            chain.doFilter(request, response);
            return;
        } 
        
        if (login_page.equalsIgnoreCase(login_page) && request.getDispatcherType().equals(DispatcherType.REQUEST)) {
            chain.doFilter(request, response);
            return;
        }
        
        log("SecurityFilter: Show Login page = " + login_page);

        //request.getDispatcherType().equals( DispatcherType.REQUEST ) 
        request.getRequestDispatcher(login_page).forward(request, response);

        log("SecurityFilter: Response dispatched ...");

    }

    /**
     * Return the filter configuration object for this filter.
     */
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }

    /**
     * Set the filter configuration object for this filter.
     *
     * @param filterConfig The filter configuration object
     */
    public void setFilterConfig(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter 
     */
    @Override
    public void destroy() {
    }

    /**
     * Init method for this filter 
     */
    @Override
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        if (filterConfig != null) {

            if (debug) {
                log("SecurityFilter:Initializing filter");
            }

            login_page = filterConfig.getInitParameter("login_page");
        }
    }

    /**
     * Return a String representation of this object.
     */
    @Override
    public String toString() {
        if (filterConfig == null) {
            return ("SecurityFilter()");
        }
        StringBuilder sb = new StringBuilder("SecurityFilter(");
        sb.append(filterConfig);
        sb.append(")");
        return (sb.toString());
    }

    public void log(String msg) {
        filterConfig.getServletContext().log(msg);
    }
}
