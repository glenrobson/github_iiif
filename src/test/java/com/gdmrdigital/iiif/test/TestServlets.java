package com.gdmrdigital.iiif.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

import static org.mockito.Mockito.*;

import org.eclipse.egit.github.core.Repository;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.http.Part;
import javax.servlet.ServletException;

import com.gdmrdigital.iiif.test.mock.HttpServletMocks;
import com.gdmrdigital.iiif.test.mock.MockContentService;
import com.gdmrdigital.iiif.test.mock.MockRepo;

import com.gdmrdigital.iiif.servlets.JsonUpload;
import com.gdmrdigital.iiif.servlets.ImageTiler;
import com.gdmrdigital.iiif.servlets.StatusCheck;
import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.processor.ImageProcessor.Status;
import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.model.iiif.Collection;
import com.gdmrdigital.iiif.model.iiif.Layer;
import com.gdmrdigital.iiif.model.iiif.InProgressCanvas;

import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServlets extends TestUtils {
    final Logger _logger = LoggerFactory.getLogger(TestServlets.class);

    public TestServlets() {
        super();
    }

    @Before
    public void setup() throws IOException {
        super.setup();
    }

    /**
     * 
     */
    @Test
    public void testAddManifest2() throws IOException, ServletException  {
        Map<String,Object> tSession = new HashMap<String, Object>();
        Repository tRepo = super.createRepo("project", "user");

        Map<String,Object> tParams = new HashMap<String, Object>();
        tParams.put("id","test.json");

        String tURL = "/upload/" + tRepo.getName() + "/manifests";
        HttpServletRequest tRequest = HttpServletMocks.createRequest(tURL, "POST", tParams, HttpServletMocks.createSession(tSession, tRepo));

        Part tFilePart = mock(Part.class);
        when(tFilePart.getInputStream()).thenReturn(getClass().getResourceAsStream("/json-ld/bod_github.json"));
        when(tRequest.getPart(anyString())).thenReturn(tFilePart);

        StringWriter tWriter = new StringWriter();
        HttpServletResponse tResp = HttpServletMocks.createResponse(tWriter);

        MockRepo tRepoService = new MockRepo();
        File tTestDir = super.newFolder("manifest");
        tRepoService.setContentService(new MockContentService(tTestDir));

        JsonUpload tUploadServlet = new JsonUpload() {
            @Override
            protected Repo getRepoService() {
                return tRepoService;
            }
            
        };

        tUploadServlet.service(tRequest, tResp);

        Manifest tSavedManifest = new Manifest();
        tSavedManifest.loadJson((Map<String,Object>)JsonUtils.fromString(tWriter.toString()));

        assertEquals("ID of manifest should have been updated", tRepo.getHomepage() + "manifests/test.json", tSavedManifest.getId());

        // Check manifest is in session collection
        Collection tManifests = tRepoService.getManifests(tRepo);
        assertTrue("Session manifest collection should have the manifest we added", tManifests.hasManifest(tSavedManifest)); 

        // Check manifest is in saved collection
        tManifests = new Collection();
        _logger.debug("Reading in from file " + new File(tTestDir, "user/project/manifests/collection.json").getPath());
        tManifests.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/project/manifests/collection.json"))));
        assertTrue("Stored manifest collection should have the manifest we added", tManifests.hasManifest(tSavedManifest)); 

        // Manifest should be saved
        Manifest tManifestOnFile = new Manifest();
        tManifestOnFile.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/project/manifests/test.json"))));
        assertEquals("Manifest on disc should match returned manifest", tSavedManifest.getId(), tManifestOnFile.getId());

        // Now delete it
        tURL = "/upload/" + tRepo.getName() + "/manifests/test.json";
        tRequest = HttpServletMocks.createRequest(tURL, "DELETE", tParams, HttpServletMocks.createSession(tSession, tRepo));

        tWriter = new StringWriter();
        tResp = HttpServletMocks.createResponse(tWriter);

        tUploadServlet.service(tRequest, tResp);
        Map<String,Object> tResponse = (Map<String,Object>)JsonUtils.fromString(tWriter.toString());
        assertNotNull("Expected the response to contain the ID of the deleted manifest", tResponse.get("id"));
        assertEquals("Expected the response to be equalt to the ID of the deleted manifest", tResponse.get("id"), tRepo.getHomepage() + "manifests/test.json");

        assertFalse("File shouldn't exists as its been deleted", new File(tTestDir, "user/project/manifests/test.json").exists());

        // Check manifest is not in collection
        tManifests = tRepoService.getManifests(tRepo);
        assertFalse("Stored manifest collection should not have the manifest we removed", tManifests.hasManifest(tSavedManifest)); 

        // Manifest should be saved
        // Check manifest is not in saved collection
        tManifests = new Collection();
        _logger.debug("Reading in from file " + new File(tTestDir, "user/project/manifests/collection.json").getPath());
        tManifests.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/project/manifests/collection.json"))));
        assertFalse("Stored manifest collection should not have the manifest we removed", tManifests.hasManifest(tSavedManifest));
    }

    /**
     * 
     */
    @Test
    public void testAddAnnotations() throws IOException, ServletException  {
        Map<String,Object> tSession = new HashMap<String, Object>();
        Repository tRepo = super.createRepo("anno_project", "user");

        Map<String,Object> tParams = new HashMap<String, Object>();
        tParams.put("id","annotations1.json");

        String tURL = "/upload/" + tRepo.getName() + "/annotations";
        HttpServletRequest tRequest = HttpServletMocks.createRequest(tURL, "POST", tParams, HttpServletMocks.createSession(tSession, tRepo));

        Part tFilePart = mock(Part.class);
        when(tFilePart.getInputStream()).thenReturn(getClass().getResourceAsStream("/json-ld/annotations.json"));
        when(tRequest.getPart(anyString())).thenReturn(tFilePart);

        StringWriter tWriter = new StringWriter();
        HttpServletResponse tResp = HttpServletMocks.createResponse(tWriter);

        MockRepo tRepoService = new MockRepo();
        File tTestDir = super.newFolder("annotaitons");
        tRepoService.setContentService(new MockContentService(tTestDir));

        JsonUpload tUploadServlet = new JsonUpload() {
            @Override
            protected Repo getRepoService() {
                return tRepoService;
            }
            
        };

        tUploadServlet.service(tRequest, tResp);

        Map<String,Object> tLoadedAnnos = (Map<String,Object>)JsonUtils.fromString(tWriter.toString());

        assertEquals("ID of manifest should have been updated", tRepo.getHomepage() + "annotations/annotations1.json", tLoadedAnnos.get("@id"));

        // Check annos is in session layer 
        Layer tAnnos = tRepoService.getAnnotations(tRepo);
        assertTrue("Session layer collection should have the annotations we added", tAnnos.hasAnnotationList(tLoadedAnnos)); 

        // Check annos is in saved collection
        tAnnos = new Layer();
        _logger.debug("Reading in from file " + new File(tTestDir, "user/anno_project/annotations/collection.json").getPath());
        tAnnos.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/anno_project/annotations/collection.json"))));
        assertTrue("Stored annotations collection should have the annotation list we added", tAnnos.hasAnnotationList(tLoadedAnnos)); 

        // Annos should be saved
        Map<String, Object> tStoredAnnos = (Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/anno_project/annotations/annotations1.json")));
        assertEquals("Annotation list on disc should contain the annotaitonList", tLoadedAnnos.get("@id"), tStoredAnnos.get("@id"));

        // Now delete it
        tURL = "/upload/" + tRepo.getName() + "/annotations/annotations1.json";
        tRequest = HttpServletMocks.createRequest(tURL, "DELETE", tParams, HttpServletMocks.createSession(tSession, tRepo));

        tWriter = new StringWriter();
        tResp = HttpServletMocks.createResponse(tWriter);

        tUploadServlet.service(tRequest, tResp);
        Map<String,Object> tResponse = (Map<String,Object>)JsonUtils.fromString(tWriter.toString());
        assertNotNull("Expected the response to contain the ID of the deleted manifest", tResponse.get("id"));
        assertEquals("Expected the response to be equalt to the ID of the deleted manifest", tResponse.get("id"), tRepo.getHomepage() + "annotations/annotations1.json");

        assertFalse("File shouldn't exists as its been deleted", new File(tTestDir, "user/project/annotations/annotations1.json").exists());

        // Check annotation is not in collection
        tAnnos = tRepoService.getAnnotations(tRepo);
        assertFalse("Stored annotation collection should not have the annotation list we removed", tAnnos.hasAnnotationList(tLoadedAnnos)); 

        // Check annotation list is not in saved collection
        tAnnos = new Layer();
        _logger.debug("Reading in from file " + new File(tTestDir, "user/anno_project/annotations/collection.json").getPath());
        tAnnos.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(new File(tTestDir, "user/anno_project/annotations/collection.json"))));
        assertFalse("Stored annotations collection should not have the annotation list we removed", tAnnos.hasAnnotationList(tLoadedAnnos)); 
    }

    /**
     * Test image upload and delete
     */
    @Test
    public void testImages() throws IOException, ServletException  {
        Map<String,Object> tSession = new HashMap<String, Object>();
        Repository tRepo = super.createRepo("images_project", "user");

        Map<String,Object> tParams = new HashMap<String, Object>();
        tParams.put("id","image1");
        tParams.put("version","2.x");
        tParams.put("repo",tRepo.getName());

        String tURL = "/tiler/" + tRepo.getName();
        HttpServletRequest tRequest = HttpServletMocks.createRequest(tURL, "POST", tParams, HttpServletMocks.createSession(tSession, tRepo));

        Part tFilePart = mock(Part.class);
        when(tFilePart.getInputStream()).thenReturn(getClass().getResourceAsStream("/img/sa.jpg"));
        when(tFilePart.getSubmittedFileName()).thenReturn("image.jpg");
        when(tRequest.getPart(anyString())).thenReturn(tFilePart);

        StringWriter tWriter = new StringWriter();
        HttpServletResponse tResp = HttpServletMocks.createResponse(tWriter);

        MockRepo tRepoService = new MockRepo();
        File tTestDir = super.newFolder("images/");
        new File(tTestDir, "user/images_project/images").mkdirs();
        tRepoService.setContentService(new MockContentService(tTestDir));

        ImageTiler tUploadServlet = new ImageTiler() {
            @Override
            protected Repo getRepoService() {
                _logger.debug("Calling repo service");
                return tRepoService;
            }
            
        };

        tUploadServlet.init(mock(ServletConfig.class));

        // Upload image
        tUploadServlet.service(tRequest, tResp);

        // Manifest is printed out with an in process canvas
        Manifest tReturnedManfiest = new Manifest();
        tReturnedManfiest.loadJson((Map<String,Object>)JsonUtils.fromString(tWriter.toString()));

        List<InProgressCanvas> tInProgress = tReturnedManfiest.getInProgress();
        assertEquals("Expected 1 in progress canvas\n" + JsonUtils.toPrettyString(tReturnedManfiest.toJson()), 1, tInProgress.size());

        List<String> tValidValues = Arrays.asList(Status.INIT.toString(),Status.TILE_GENERATION.toString(), Status.GIT_UPLOAD.toString(), Status.UPDATING_IMG_LIST.toString(), Status.PAGES_UPDATING.toString(), Status.FINISHED.toString());
        StatusCheck tStatusServlet = null;
        Map<String, Object> tStatus = null;
        String tCurrentStatus = "" + Status.INIT;

        while (!tCurrentStatus.equals(Status.FINISHED.toString())) {

            tParams = new HashMap<String, Object>();
            tParams.put("id",tInProgress.get(0).getProcessId());

            tURL = "/status/images";
            tRequest = HttpServletMocks.createRequest(tURL, "GET", tParams, HttpServletMocks.createSession(tSession, tRepo));

            tWriter = new StringWriter();
            tResp = HttpServletMocks.createResponse(tWriter);

            // Check progress
            tStatusServlet = new StatusCheck();
            tStatusServlet.service(tRequest, tResp);

            tStatus = (Map<String,Object>)JsonUtils.fromString(tWriter.toString());
            if (!tStatus.containsKey("status")) {
                // Process finished
                break;
            }
            tCurrentStatus = (String)tStatus.get("status");
            _logger.debug("Got status: " + tCurrentStatus);
            assertTrue("Unexpected status " + tCurrentStatus, tValidValues.contains(tCurrentStatus));
            try {
                Thread.sleep(500);
            } catch (InterruptedException tExcpt) {
            }
        }
    }
}
