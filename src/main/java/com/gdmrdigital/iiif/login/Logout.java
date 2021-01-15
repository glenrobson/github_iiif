package com.gdmrdigital.iiif.login;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import java.io.IOException;

public class Logout extends HttpServlet {
    protected String _redirectURL = "login.html";

	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
        if (pConfig.getInitParameter("post_logout_url") != null) {
            _redirectURL = pConfig.getInitParameter("post_logout_url");
        }
	}

	public void doGet(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
        HttpSession tSession = pReq.getSession();
        tSession.invalidate();
        pRes.sendRedirect(_redirectURL);
        return; 
    }
}
