package com.gdmrdigital.iiif.controllers;

import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;

public class Session {
    private HttpSession _session = null;

    public void setSession(final HttpSession pSession) {
        _session = pSession;
    }
    
    protected HttpSession getSession() {
        if (_session == null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession tSession = (HttpSession)facesContext.getExternalContext().getSession(true);
            return tSession;
        } else {
            return _session;
        }
    }
}
