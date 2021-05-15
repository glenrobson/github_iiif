package com.gdmrdigital.iiif.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.processor.ImageProcessor.Status;

public class StatusCheck extends JSONServlet {
    public void doGet(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
        // URI: status/images?id=
        //String[] tRequestURI = pReq.getRequestURI().split("/");
        //Repository tPath = tRepo.getRepo(tRequestURI[tRequestURI.length - 2]);
        Map<String,Object> tStatusJson = new HashMap<String,Object>();
        if (pReq.getParameter("id") != null) {
            String tId = pReq.getParameter("id");
            tStatusJson.put("id", tId);
            Status tStatus = ImageProcessor.getStatus(tId);
            if (tStatus != null) {
                tStatusJson.put("status", tStatus.toString());

                super.sendJson(pRes, 200, tStatusJson);
            } else {
                tStatusJson.put("message", "Failed to find id in process list. It might be finished.");
                super.sendJson(pRes, 200, tStatusJson);
            }
        } else {
            tStatusJson.put("message", "Missing parameter id");

            super.sendJson(pRes, 400, tStatusJson);
        }
    }
}
