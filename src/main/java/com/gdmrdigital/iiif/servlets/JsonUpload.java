package com.gdmrdigital.iiif.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.model.iiif.Collection;

import org.eclipse.egit.github.core.Repository;

import com.github.jsonldjava.utils.JsonUtils;

import uk.co.gdmrdigital.iiif.image.InfoJson;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.net.URL;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@MultipartConfig(
   /* fileSizeThreshold = 0,
    maxRequestSize = 10L * 1024 * 1024,
    maxFileSize = 10L * 1024 * 1024*/
)
public class JsonUpload extends JSONServlet {
    private static final Logger log = Logger.getLogger(JsonUpload.class.getName());


	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
    }

    // /upload/#{project}/manifests
    protected void doPost(final HttpServletRequest pReq, final HttpServletResponse pRes) throws ServletException, IOException {
        String[] tRequestURI = pReq.getRequestURI().split("/");
        String tType = tRequestURI[tRequestURI.length - 1];

        String tName = pReq.getParameter("id");

        Repo tRepo = new Repo();
        tRepo.setSession(pReq.getSession());
        Repository tRepoObj = tRepo.getRepo(tRequestURI[tRequestURI.length - 2]);
        RepositoryPath tPath = new RepositoryPath(tRepoObj, tType + "/" + tName);

        Part tFilePart = pReq.getPart("file");
        long fileSize = tFilePart.getSize();
        String fileName = tFilePart.getSubmittedFileName();

        Map<String,Object> tJson = (Map<String,Object>)JsonUtils.fromInputStream(tFilePart.getInputStream());

        if (tJson.containsKey("@id")) {
            tJson.put("@id", tPath.getWeb());
        } else if (tJson.containsKey("id")) {
            tJson.put("id", tPath.getWeb());
        }
        
        tRepo.uploadFile(tPath, JsonUtils.toPrettyString(tJson));

        if (tType.equals("manifests")) {
            Manifest tManifest = new Manifest();
            tManifest.loadJson(tJson);
            Collection tManifests = tRepo.getManifests(tRepoObj);
            tManifests.add(tManifest);

            tRepo.saveManifests(tRepoObj, tManifests);
        }
        
        sendJson(pRes, 200, tJson);
    }

    // /upload/#{project}/manifests
    protected void doDelete(final HttpServletRequest pReq, final HttpServletResponse pRes) throws ServletException, IOException {
        System.out.println("Request URI " + pReq.getRequestURI());
        String[] tRequestURI = pReq.getRequestURI().split("/");
        String tType = tRequestURI[tRequestURI.length - 2];

        String tName = tRequestURI[tRequestURI.length - 1]; // manifests2.json

        Repo tRepo = new Repo();
        tRepo.setSession(pReq.getSession());
        Repository tRepoObj = tRepo.getRepo(tRequestURI[tRequestURI.length - 3]);
        System.out.println("Path: " + tRepoObj.generateId() + "/" + tType + "/" + tName);
        RepositoryPath tPath = tRepo.processPath(tRepoObj.getName() + "/" + tType + "/" + tName);

        tRepo.deleteFile(tPath);

        System.out.println("Type " + tType);
        if (tType.equals("manifests")) {
            System.out.println("Removing " + tPath.getWeb());
            Collection tManifests = tRepo.getManifests(tRepoObj);
            tManifests.remove(tPath.getWeb());

            tRepo.saveManifests(tRepoObj, tManifests);
        }

        Map<String,Object> tJson = new HashMap<String,Object>();
        tJson.put("id", tPath.getWeb());
        sendJson(pRes, 200, tJson);
    }
}
