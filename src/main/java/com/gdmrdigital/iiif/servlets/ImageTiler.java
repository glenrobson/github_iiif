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

import java.util.Base64;

import java.net.URL;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.controllers.UserService;
import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.Config;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.RequestException;

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
        if (Config.getConfig().getStorage() == null) {
            _tmpDir = new File(System.getProperty("java.io.tmpdir")).getAbsoluteFile();
        } else {
            _tmpDir = new File(Config.getConfig().getStorage());
        }
    }

    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {

        String tId = req.getParameter("id");
        String tVersion = "3";
        if (req.getParameter("version").equals("2.x")) {
            tVersion = "2";
        }
        String tRepoName = req.getParameter("repo");

        Part tFilePart = req.getPart("file");
        long fileSize = tFilePart.getSize();
        String filename = tFilePart.getSubmittedFileName();
        
        Repo tRepo = this.getRepoService();
        tRepo.setSession(req.getSession());
        Repository tRepoObj = tRepo.getRepo(tRepoName);
        RepositoryPath tPath = new RepositoryPath(tRepoObj, "/images/uploads/" + tVersion + "/" + filename);

        byte imageData[] = new byte[(int)fileSize];
        tFilePart.getInputStream().read(imageData);
        String base64Image = Base64.getEncoder().encodeToString(imageData);
        try {
            tRepo.uploadEncodedFile(tPath, base64Image);

            UserService tService = new UserService(req.getSession());

            ImageProcessor tProcessor = new ImageProcessor();
            StringBuffer tBuffer = new StringBuffer("https://");
            tBuffer.append(tService.getUser().getLogin());
            tBuffer.append(".github.io/");
            tBuffer.append(tRepoName);
            tBuffer.append("/images/");

            // filename without extension
            String name = "";
            if (filename == null || filename.lastIndexOf(".") == -1) {
                name = filename;
            } else {
                name = filename.substring(0, filename.lastIndexOf("."));
            }

            tBuffer.append(name);

            URL tInfoJson = new URL(tBuffer.toString());
            System.out.println("Created info.json: " + tInfoJson);

            tProcessor.setInfoURL(tInfoJson);
            tProcessor.setRepoControl(tRepo);

            tProcessor.setRepo(tRepoObj);

            // ... // Update file information (status: new => stored) in DB.
            //log.info("ID: " + tId + ", version: " + tVersion + " File uploaded to: " + tImageFile.getPath());
            Map<String, Object> tResponse = new HashMap<String,Object>();
            tResponse.put("process_id", tProcessor.getIdentifier());
            tResponse.put("status", "UPLOADED");

            Manifest tImages = tRepo.getImages(tRepoObj);
            // addInProcess(final String pProccessId, final URL pInfoJson, final String pLabel)
            tImages.addInProcess(tProcessor.getIdentifier(), tInfoJson, tId);
            sendJson(resp, 200, tImages.toJson());
        } catch (RequestException tExcpt) {
            if (tExcpt.getMessage().indexOf("For 'properties/sha', nil is not a string") != -1) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Image with same filename already exists in this project.");
            } else {
                resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to upload image due to " + tExcpt.getMessage());
            }    
            return;
        } catch (IOException tExcpt) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to upload image due to " + tExcpt.getMessage());
            tExcpt.printStackTrace();
            return;
        }
    }
}

