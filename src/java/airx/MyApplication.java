package airx;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author sherwinp
 */
public class MyApplication extends HttpServlet {




    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        super.init(config);
    }

    @Override
    public void destroy() {
    }

    @Override
    public String getServletInfo() {
        return "MyAppliction Init Servlet";
    }// </editor-fold>
}
