package com.gdmrdigital.iiif.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;

import java.util.Map;

import com.github.jsonldjava.utils.JsonUtils;

import com.gdmrdigital.iiif.controllers.Repo;
import org.eclipse.egit.github.core.Repository;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.model.iiif.Collection;

public class JSONServlet extends HttpServlet {
    public void doGet(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
        Repo tRepo = new Repo();
        tRepo.setSession(pReq.getSession());

        // http://localhost:8080/iiif/test2/images/manifest.json
        // http://localhost:8080/iiif/test2/manifests/collection.json
        String[] tRequestURI = pReq.getRequestURI().split("/");
        Repository tPath = tRepo.getRepo(tRequestURI[tRequestURI.length - 3]);

        if (pReq.getRequestURI().indexOf("images/manifest.json") != -1) {
            Manifest tManifest = tRepo.getImages(tPath);
            this.sendJson(pRes, 200, tManifest.toJson());
        } else if (pReq.getRequestURI().indexOf("manifests/collection.json") != -1){
            Collection tCollection = tRepo.getManifests(tPath);
            this.sendJson(pRes, 200, tCollection.toJson());
        } else {
            pRes.sendError(pRes.SC_NOT_FOUND, "Couldn't find " + pReq.getRequestURI());
        }
    }

     protected void sendJson(final HttpServletResponse pRes, final int pCode, final Map<String,Object> pPayload) throws IOException {
        pRes.setStatus(pCode);
        pRes.setContentType("application/json");
        pRes.setCharacterEncoding("UTF-8");
        JsonUtils.writePrettyPrint(pRes.getWriter(), pPayload);
    }
}
