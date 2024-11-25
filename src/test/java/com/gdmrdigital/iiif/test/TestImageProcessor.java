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
import com.gdmrdigital.iiif.test.mock.MockRepositoryService;
import com.gdmrdigital.iiif.servlets.JsonUpload;
import com.gdmrdigital.iiif.servlets.ImageTiler;
import com.gdmrdigital.iiif.servlets.StatusCheck;
import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.processor.ImageProcessor.Status;
import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowStatus;
import com.gdmrdigital.iiif.model.iiif.Collection;
import com.gdmrdigital.iiif.model.iiif.Layer;
import com.gdmrdigital.iiif.model.iiif.InProgressCanvas;

import com.github.jsonldjava.utils.JsonUtils;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.InputStream;

import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.URL;

public class TestImageProcessor extends TestUtils {
    final Logger _logger = LoggerFactory.getLogger(TestServlets.class);

    public TestImageProcessor() {
    }

    @Before
    public void setup() throws IOException {
        super.setup();
    }
    
    @Test
    public void testImageWorkflows() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tService.setWorkflowJson(new File(getClass().getResource("/workflows/image.json").getFile()));
        tRepoService.setRepositoryService(tService);

        WorkflowStatus tStatus = tRepoService.getActiveImageWorkflows(tRepoObj);
        assertTrue("Workflows should be active", tStatus.hasActive());
    }


    @Test
    public void testPagesWorkflows() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tService.setWorkflowJson(new File(getClass().getResource("/workflows/pages.json").getFile()));
        tRepoService.setRepositoryService(tService);

        WorkflowStatus tStatus = tRepoService.getActivePagesWorkflows(tRepoObj);
        assertTrue("Workflows should be active", tStatus.hasActive());
        assertEquals("Expected to see the pages workflow", "pages build and deployment", tStatus.getWorkflowRuns().get(0).getName());
    }

    @Test
    public void testImageProcessor() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tService.setWorkflowJson(new File(getClass().getResource("/workflows/image.json").getFile()));
        tRepoService.setRepositoryService(tService);

        ImageProcessor tProcessor = new ImageProcessor();
        tProcessor.setRepoControl(tRepoService);
        tProcessor.setRepo(tRepoObj);

        assertEquals("Expected status to be generating images", ImageProcessor.Status.TILE_GENERATION, ImageProcessor.getStatus(tProcessor.getIdentifier()));
    }

    @Test
    public void testPagesProcessor() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tService.setWorkflowJson(new File(getClass().getResource("/workflows/pages.json").getFile()));
        tRepoService.setRepositoryService(tService);

        ImageProcessor tProcessor = new ImageProcessor();
        tProcessor.setRepoControl(tRepoService);
        tProcessor.setRepo(tRepoObj);

        assertEquals("Expected status to be generating images", ImageProcessor.Status.PAGES_UPDATING, ImageProcessor.getStatus(tProcessor.getIdentifier()));
    }

    @Test
    public void testFail() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tRepoService.setRepositoryService(tService);

        ImageProcessor tProcessor = new ImageProcessor();
        tProcessor.setRepoControl(tRepoService);
        tProcessor.setRepo(tRepoObj);
        tProcessor.setInfoURL(new URL("https://iiif-test.github.io/test3/images/IMG_0614"));

        // TODO need to add timeout so this passes the greace period 
        //assertEquals("Expected status to be generating images", ImageProcessor.Status.FAILED, ImageProcessor.getStatus(tProcessor.getIdentifier()));
    }

    @Test
    public void testSuccess() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        Repository tRepoObj = super.createRepo("project", "user");

        MockRepo tRepoService = new MockRepo();

        Manifest tManifest = new Manifest();
        tManifest.loadJson((Map<String,Object>)JsonUtils.fromInputStream(getClass().getResourceAsStream("/json-ld/images-manifest.json")));
        tRepoService.setImages(tManifest);

        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, tRepoObj));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));

        MockRepositoryService tService = new MockRepositoryService(tTestDir);
        tRepoService.setRepositoryService(tService);

        ImageProcessor tProcessor = new ImageProcessor();
        tProcessor.setRepoControl(tRepoService);
        tProcessor.setRepo(tRepoObj);
        tProcessor.setInfoURL(new URL("https://iiif-test.github.io/test3/images/IMG_0614"));

        assertEquals("Expected status to be generating images", ImageProcessor.Status.FINISHED, ImageProcessor.getStatus(tProcessor.getIdentifier()));
    }
}
