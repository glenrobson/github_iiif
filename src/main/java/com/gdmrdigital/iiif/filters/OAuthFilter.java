package com.gdmrdigital.iiif.filters;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;

public class OAuthFilter implements Filter {
    protected String _loginPage = "/login.xhtml";

	public void init(final FilterConfig pConfig) {
	}

	public void doFilter(final ServletRequest pRequest, final ServletResponse pRes, final FilterChain pChain) throws IOException, ServletException {
        HttpServletRequest pReq = (HttpServletRequest)pRequest; 
        HttpSession tSession = pReq.getSession();
        String tCallingURL = pReq.getRequestURI();
        System.out.println("Calling auth URL on " + tCallingURL);
        if (tSession.getAttribute("user") == null && !tCallingURL.endsWith("login.xhtml")) {
            if (pReq.getQueryString() != null) {
                tCallingURL += "?" + pReq.getQueryString();
            }

            tSession.setAttribute("oauth_url", tCallingURL);        
           ((HttpServletResponse)pRes).sendRedirect(_loginPage);
            return;
        } else {
            System.out.println("Found logged in user: " + tSession.getAttribute("user"));
        }
        pChain.doFilter(pReq, pRes);
	}

	public void destroy() {
	}
}
