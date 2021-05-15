package com.gdmrdigital.iiif.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.eclipse.egit.github.core.Repository;

import com.gdmrdigital.iiif.controllers.Repo;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.io.IOException;

public class Projects extends HttpServlet {
    public void doPost(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {

        if (pReq.getRequestURI().endsWith("/project/")) {
            // Request to create project
            Repo tRepo = new Repo();
            tRepo.setSession(pReq.getSession());

            System.out.println(pReq.getParameter("name"));

            Repository tRepoData = tRepo.createRepo(pReq.getParameter("name"));

            pRes.setStatus(HttpServletResponse.SC_CREATED);
            pRes.setContentType("application/ld+json; charset=UTF-8");
            pRes.setCharacterEncoding("UTF-8");
            pRes.getOutputStream().println(new Gson().toJson(tRepoData));
        }
    }
}
