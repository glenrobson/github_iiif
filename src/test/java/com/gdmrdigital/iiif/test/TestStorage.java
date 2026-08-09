package com.gdmrdigital.iiif.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.RequestException;
import com.gdmrdigital.iiif.model.github.RepositoryPath;

import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

import java.net.URL;

import javax.servlet.http.HttpSession;

import com.github.jsonldjava.utils.JsonUtils;

import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.test.mock.MockContentService;
import com.gdmrdigital.iiif.test.mock.MockRepo;
import com.gdmrdigital.iiif.test.mock.MockRepositoryService;
import com.gdmrdigital.iiif.test.mock.HttpServletMocks;

public class TestStorage extends TestUtils {
    final Logger _logger = LoggerFactory.getLogger(TestStorage.class);

    public TestStorage() {
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
    public void testSaveImageManifest() throws IOException {
        File tTestDir = super.newFolder("target/manifest");
        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession));
        tRepoService.setContentService(new MockContentService(tTestDir));
        
        Manifest tManifest = new Manifest();
        tManifest.loadJson(super.getJSON("/json-ld/image_manifest.json"));

        tRepoService.saveImageManifest(super.createRepo("test_repo", "user"), tManifest);

        File tManifestFile = new File(tTestDir, "user/test_repo/images/manifest.json");
        assertTrue("Expected Manifest file to be created " + tManifestFile.getPath(), tManifestFile.exists());
        assertEquals("Expected Manifest in session to be updated", tManifest, tSession.get("user/test_repo/images/manifest.json"));
    }


    @Test
    /**
     * Test not adding inProcess to saved Manifest
     */
    public void testInProcessStorage() throws IOException {
        File tTestDir = super.newFolder("target/test_project");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession));
        tRepoService.setContentService(new MockContentService(tTestDir));

        Repository tRepoObj = super.createRepo("project", "user");
        Manifest tImages = tRepoService.getImages(tRepoObj);
        // addInProcess(final String pProccessId, final URL pInfoJson, final String pLabel)
        tImages.addInProcess("process_id", new URL("https://example.com/test/image/info.json"), "Test image");

        tRepoService.saveImageManifest(tRepoObj, tImages);
        RepositoryPath tPath = new RepositoryPath(tRepoObj, "images/manifest.json");
        String tManifestPath = tRepoObj.generateId() + tPath.getPath();
        Manifest tSessionManifest = (Manifest)tSession.get(tManifestPath);

        // Remove session version to ensure we get the version from disk
        tSession.remove(tManifestPath);
        Manifest tSavedManifest = tRepoService.getImages(tRepoObj);

        assertEquals("Expected no canvases as inProcess should not be saved.\n" + JsonUtils.toPrettyString(tSavedManifest.toJson()), 0, tSavedManifest.getCanvases().size());
        // Double check saved Manifest
        Manifest tFromFileStore = new Manifest();
        tFromFileStore.loadJson(new File(tTestDir, "user/project/images/manifest.json"));
        // Session version should still have inProcess 
        assertEquals("Session Manifest should have the inProcess canvas: " + JsonUtils.toPrettyString(tSessionManifest.toJson()), 1, tSessionManifest.getCanvases().size());
    }

    /**
     * Test fixing an in process manifest
     */
    @Test
    public void testFix() throws IOException {
        File tTestDir = super.newFolder("target/broken_project");

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession));
        tRepoService.setContentService(new MockContentService(tTestDir));

        Repository tRepoObj = super.createRepo("project", "user");

        Manifest tNewManifest = Manifest.createEmpty("https://example.com/1/manifest.json", "In Process Manifest");
        tNewManifest.addInProcess("broken_process_id", new URL("https://example.com/test/image/info.json"), "example broken image");

        new File(tTestDir, "user/project/images").mkdirs();
        JsonUtils.writePrettyPrint(new BufferedWriter(new FileWriter(new File(tTestDir, "user/project/images/manifest.json"))), tNewManifest.toJson()); 
        _logger.debug("Saved manifest to: " +  new File(tTestDir, "user/project/images/manifest.json").getPath());
        
        Manifest tSavedManifest = tRepoService.getImages(tRepoObj);

        assertEquals("Expected no canvases as inProcess should not be shown in the list.\n" + JsonUtils.toPrettyString(tSavedManifest.toJson()), 0, tSavedManifest.getCanvases().size());
    }

    /**
     * Test fixing an in process manifest
     */
    @Test
    public void testUpgrade() throws IOException {
        File tTestDir = super.newFolder("target/oldProject");
        new File(tTestDir, "user/project/images").mkdirs();
        new File(tTestDir, "user/project/manifests").mkdirs();
        new File(tTestDir, "user/project/collections").mkdirs();
        new File(tTestDir, "user/project/annotations").mkdirs();

        MockRepo tRepoService = new MockRepo();
        Map<String, Object> tSession = new HashMap<String, Object>();
        tRepoService.setSession(HttpServletMocks.createSession(tSession, super.createRepo("project", "user")));
        tSession.remove("repo/project");
        tRepoService.setContentService(new MockContentService(tTestDir));
        tRepoService.setRepositoryService(new MockRepositoryService(tTestDir));

        Repository tRepoObj = tRepoService.getRepo("project");

        assertNotNull("Should have found repo", tRepoObj);
        assertTrue("Github actions should have been added once the project noted it wasn't present", new File(tTestDir, "user/project/.github/workflows/convert_images.yml").exists());
    }


   


    // If an image is inProcess type but not actually in process then we shouldn't remove it...

    // Test adding an image which already exists
    /* 
        Failed to create and upload tiles due to: org.eclipse.egit.github.core.client.RequestException: Invalid request.

For 'properties/sha', nil is not a string. (422)
org.eclipse.egit.github.core.client.RequestException: Invalid request.

For 'properties/sha', nil is not a string. (422)
	at org.eclipse.egit.github.core.client.GitHubClient.createException(GitHubClient.java:622)
	at org.eclipse.egit.github.core.client.GitHubClient.sendJson(GitHubClient.java:713)
	at org.eclipse.egit.github.core.client.GitHubClient.put(GitHubClient.java:848)
	at com.gdmrdigital.iiif.model.github.ExtendedContentService.setContents(ExtendedContentService.java:49)
	at com.gdmrdigital.iiif.model.github.ExtendedContentService.setContents(ExtendedContentService.java:25)
	at com.gdmrdigital.iiif.controllers.Repo.uploadDirectory(Repo.java:430)
	at com.gdmrdigital.iiif.controllers.Repo.uploadDirectory(Repo.java:418)
	at com.gdmrdigital.iiif.controllers.Repo.uploadDirectory(Repo.java:418)
	at com.gdmrdigital.iiif.controllers.Repo.uploadDirectory(Repo.java:418)
	at com.gdmrdigital.iiif.processor.ImageProcessor.run(ImageProcessor.java:66)
    */
    // Test invalid image names
}
