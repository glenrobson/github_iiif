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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ImageTiler extends JSONServlet {
    private static final Logger log = LoggerFactory.getLogger(ImageTiler.class);
    private volatile File _tmpDir = null;


	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
        _tmpDir = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        String tId = req.getParameter("id");
        String tVersion = InfoJson.VERSION3;
        if (req.getParameter("version").equals("2.x")) {
            tVersion =  InfoJson.VERSION211;
        }
        String tRepoName = req.getParameter("repo");

        Part tFilePart = req.getPart("file");
        long fileSize = tFilePart.getSize();
        String fileName = tFilePart.getSubmittedFileName();
        
        File tImageWorkDir = new File(_tmpDir, UUID.randomUUID().toString());
        Files.createDirectories(tImageWorkDir.toPath());

        // ... // Register file information (status: new) for current user in DB.
        log.info("File has been uploaded: " + tImageWorkDir + " with filename " + fileName);

        File tImageFile = new File(tImageWorkDir, fileName);
        Files.copy(tFilePart.getInputStream(), tImageFile.toPath());

        File tTiles = new File(tImageWorkDir, "tiles"); 
        
        ImageProcessor tProcessor = new ImageProcessor();
        tProcessor.setFileId(tId);
        tProcessor.setIiifVersion(tVersion);

        tProcessor.setInputImage(tImageFile);
        tProcessor.setTileDestination(tTiles);

        Repo tRepo = this.getRepoService();
        tRepo.setSession(req.getSession());

        RepositoryPath tWeb = tRepo.processPath(tRepoName + "/images");
        tProcessor.setBaseURI(tWeb.getWeb());
        tProcessor.setRepoControl(tRepo);

        Repository tRepoObj = tRepo.getRepo(tRepoName);
        tProcessor.setRepo(tRepoObj);
        tProcessor.setGithubPath("/images/" + tId);

        tProcessor.start();
        // ... // Update file information (status: new => stored) in DB.
        log.info("ID: " + tId + ", version: " + tVersion + " File uploaded to: " + tImageFile.getPath());
        Map<String, Object> tResponse = new HashMap<String,Object>();
        tResponse.put("process_id", tProcessor.getIdentifier());
        tResponse.put("status", "UPLOADED");

        Manifest tImages = tRepo.getImages(tRepoObj);
        // addInProcess(final String pProccessId, final URL pInfoJson, final String pLabel)
        tImages.addInProcess(tProcessor.getIdentifier(), new URL(tWeb.getWeb() + "/" +  tId + "/info.json"), tId);
        sendJson(resp, 200, tImages.toJson());
    }
}

