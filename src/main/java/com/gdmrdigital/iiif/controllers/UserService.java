package com.gdmrdigital.iiif.controllers;

import org.eclipse.egit.github.core.User;

import javax.servlet.http.HttpSession;

import com.github.scribejava.core.model.OAuth2AccessToken;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.annotation.PostConstruct;

@RequestScoped
@ManagedBean
public class UserService extends Session {
    public UserService() {
    }

    public UserService(final HttpSession pSession) {
        super();
        super.setSession(pSession);
    }

    public boolean isAuthenticated() {
        return this.getUser() != null;
    }

    public User getUser() {
        return (User)super.getSession().getAttribute("user");
    }

    public OAuth2AccessToken getToken() {
        return (OAuth2AccessToken)super.getSession().getAttribute("user_token");
    }
}
