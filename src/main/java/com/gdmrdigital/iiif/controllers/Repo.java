package com.gdmrdigital.iiif.controllers;

import com.gdmrdigital.iiif.model.github.ExtendedRepositoryServices;
import com.gdmrdigital.iiif.model.github.ExtendedContentService;
import com.gdmrdigital.iiif.model.github.ExtendedContentService.ContentResponse;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowRun;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowStatus;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.model.iiif.Manifest;
import com.gdmrdigital.iiif.model.iiif.Layer;
import com.gdmrdigital.iiif.model.iiif.Collection;
import com.gdmrdigital.iiif.Config;

import com.github.jsonldjava.utils.JsonUtils;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.annotation.PostConstruct;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.client.RequestException;

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.HashMap;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RequestScoped
@ManagedBean
public class Repo extends Session {
    final Logger _logger = LoggerFactory.getLogger(Repo.class);

    public Repo() {
        super();
    }

    @PostConstruct
    public void init() {
    }

    public GitHubClient getClient() {
        UserService tService = new UserService(super.getSession());

        GitHubClient tClient = new GitHubClient();
        tClient.setOAuth2Token(tService.getToken().getAccessToken());

        return tClient;
    }

    protected ExtendedContentService getContentService() {
        return new ExtendedContentService(this.getClient());
    }

    protected ExtendedRepositoryServices getRepositoryService() {
        return new ExtendedRepositoryServices(this.getClient());
    }

    protected User getUser() {
        UserService tService = new UserService(super.getSession());
        return tService.getUser();
    }

    // test3/manifests/manifest2.json (repo/path)
    public RepositoryPath processPath(final String pPath) throws IOException {
        String tRepoID = "";
        String tPath = null;
        if (pPath.indexOf("/") == -1) {
            // No path so root
            tRepoID = pPath;
        } else {
            String[] tSplitPath = pPath.split("/");
            tRepoID = tSplitPath[0];
            if (tSplitPath.length > 1) {
                tPath = pPath.substring(pPath.indexOf("/") + 1);
            }
        }

        Repository tRepo = this.getRepo(tRepoID);
        return processPath(tRepo, tPath);
    }

    public boolean checkAvilable(final URL pInfoJson, final int pTries) throws IOException {
        int tTry = 0;
        boolean tSuccess = false;
        HttpURLConnection con = null;
        while (tTry < pTries) {
            con = (HttpURLConnection) pInfoJson.openConnection();
            con.setRequestMethod("HEAD");
            int status = con.getResponseCode();
            if (status == 200) {
                tSuccess = true;
                break;
            }
            tTry++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException tExcpt) {
            }
        }
        con.disconnect();
        return tSuccess;
    }


    public RepositoryPath processPath(final Repository pRepo, final String pPath) throws IOException {
        RepositoryPath tRepoPath = new RepositoryPath(pRepo, pPath);
        tRepoPath.setContents(this.getFiles(tRepoPath));

        return tRepoPath;
    }

    public List<SearchRepository> getRepos() throws IOException {
        RepositoryService tService = this.getRepositoryService();
        
        Map<String, String> tParams = new HashMap<String,String>();
        tParams.put("topic", "iiif-training-workbench");
        //tParams.put("topic", "iiif-search");
        tParams.put("user", this.getUser().getLogin());
        tParams.put("sort", "updated");
        System.out.println("User " + this.getUser().getLogin());

        List<SearchRepository> tResults = tService.searchRepositories(tParams);

        Collections.sort(tResults, new Comparator() {
                            public int compare(Object o1, Object o2) {
                                SearchRepository sa = (SearchRepository)o1;
                                SearchRepository sb = (SearchRepository)o2;

                                return sb.getCreatedAt().compareTo(sa.getCreatedAt());           
                            }
                        });
        return tResults;
    }

    public Repository getRepo(final String pId) throws IOException {
        System.out.println("Looking for repo " + pId);
        String tCacheId = "repo/" + pId;
        Repository tRepo = null;
        if (super.getSession().getAttribute(tCacheId) == null) {
            RepositoryService tService = this.getRepositoryService();

            tRepo = tService.getRepository(this.getUser().getLogin(), pId);

            _logger.debug("Checking repo " + pId+ " has latest code");
            this.checkHasLatestCode(tRepo);
            super.getSession().setAttribute(tCacheId, tRepo);
        } else {
            _logger.debug("Got " + pId + " from Cache using key: " +tCacheId);
            _logger.debug(super.getSession().getAttribute(tCacheId).toString());
            tRepo = (Repository)super.getSession().getAttribute(tCacheId);
        }
        return tRepo;
    }

    public void checkHasLatestCode(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, ".github/workflows/convert_images.yml");

        ExtendedContentService tService = this.getContentService();
        if (!tService.exists(tPath)) {
            String[] tUpdates = {
                ".github/workflows/convert_images.yml",
                "images/uploads/2/README.md",
                "images/uploads/3/README.md"
            };
            this.uploadTemplateFiles(pRepo, tUpdates);
        }
    }


    public Manifest getImagesFromRepo(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "images/manifest.json");
        String tManifestPath = pRepo.generateId() + tPath.getPath();
        Manifest tImagesManifest = new Manifest();
        // Get from repo
        try {
            List<RepositoryContents> tManifestList = this.getFiles(tPath);
            if (tManifestList.get(0).getContent() == null) {
                throw new IOException("Empty manifest found");
            } else {
                tImagesManifest.loadJson(this.contents2Json(tManifestList.get(0)));
                System.out.println("Sha: " + tManifestList.get(0).getSha());
                tImagesManifest.setSha(tManifestList.get(0).getSha());
                if (tImagesManifest.removeFinishedInProcess()) {
                    // in Process canvases were removed so save new copy. 
                    saveImageManifest(pRepo, tImagesManifest);
                }
            }
            _logger.debug("Getting manifest from GitHub");
        } catch (IOException tExcpt) {    
            if (tExcpt.getMessage().equals("Empty manifest found") || (tExcpt instanceof RequestException && tExcpt.getMessage().equals("Not Found (404)")) ) {
                _logger.debug("Failed to retrieve " + pRepo.generateId() + "/images/manifest.json so creating new. Failed due to: " + tExcpt); 
                // create new manifest
                tImagesManifest = Manifest.createEmpty(tPath.getWeb(), "All images loaded in " + pRepo.generateId() + " project");
            } else {
                throw tExcpt;
            }
        }
        super.getSession().setAttribute(tManifestPath, tImagesManifest);
        return tImagesManifest;
    }

    public Manifest getImages(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "images/manifest.json");
        String tManifestPath = pRepo.generateId() + tPath.getPath();
        Manifest tImagesManifest = new Manifest();
        if (super.getSession().getAttribute(tManifestPath) != null) {
            tImagesManifest = (Manifest)super.getSession().getAttribute(tManifestPath);
            _logger.debug("Getting manifest from session");
        } else {
            tImagesManifest = this.getImagesFromRepo(pRepo);
        }

        _logger.debug("Returning manifest {}", JsonUtils.toPrettyString(tImagesManifest.toJson()));
        return tImagesManifest;
    }

    public Map<String, Object> contents2Json(final RepositoryContents pContents) throws IOException {
        String tContent = pContents.getContent();
        if (pContents.getEncoding() != null && pContents.getEncoding().equals(RepositoryContents.ENCODING_BASE64)) {
            tContent = decode(tContent);
        }

        return (Map<String, Object>)JsonUtils.fromString(tContent);
    }

    public void saveImageManifest(final Repository pRepo, final Manifest pManifest) throws IOException {
        _logger.debug("Saving image manifest {}", JsonUtils.toPrettyString(pManifest.toJson()));
        RepositoryPath tPath = new RepositoryPath(pRepo, "/images/manifest.json");
        String tManifestPath = pRepo.generateId() + tPath.getPath();
        super.getSession().setAttribute(tManifestPath, pManifest);
        
        ContentResponse tResponse = this.uploadFile(tPath, JsonUtils.toPrettyString(pManifest.toStoreJson()), pManifest.getSha());

        List<RepositoryContents> tManifestList = this.getFiles(tPath);
        pManifest.setSha(tManifestList.get(0).getSha());
    }

    public Collection getManifests(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "manifests/collection.json");
        String tCollectionPath = pRepo.generateId() + tPath.getPath();
        if (super.getSession().getAttribute(tCollectionPath) != null) {
            Collection tCollection = (Collection)super.getSession().getAttribute(tCollectionPath);
            System.out.println("Sha: " + tCollection.getSha());
            return tCollection;
        } else {
            // Get from repo
            Collection tManifests = new Collection();
            try {
                List<RepositoryContents> tManifestList = this.getFiles(tPath);
                if (tManifestList.get(0).getContent() == null) {
                    throw new IOException("Empty collection found");
                } else {
                    tManifests.loadJson(this.contents2Json(tManifestList.get(0)));
                    System.out.println("Sha: " + tManifestList.get(0).getSha());
                    tManifests.setSha(tManifestList.get(0).getSha());
                }
            } catch (IOException tExcpt) {    
                System.err.println("Failed to retrieve /manifests/collections.json so creating new"); 
                //tExcpt.printStackTrace();
                // create new manifest
                tManifests = Collection.createEmpty(tPath.getWeb(), "All manifests loaded in " + pRepo.generateId() + " project");
            }

            super.getSession().setAttribute(tCollectionPath, tManifests);
            return tManifests;
        }

    }

    public void saveManifests(final Repository pRepo, final Collection pCollection) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "/manifests/collection.json");
        String tCollectionPath = pRepo.generateId() + tPath.getPath();
        super.getSession().setAttribute(tCollectionPath, pCollection);

        ContentResponse tResponse = this.uploadFile(tPath, JsonUtils.toPrettyString(pCollection.toJson()), pCollection.getSha());

        List<RepositoryContents> tManifestList = this.getFiles(tPath);
        pCollection.setSha(tManifestList.get(0).getSha());
    }

    public Layer getAnnotations(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "annotations/collection.json");
        String tCollectionPath = pRepo.generateId() + tPath.getPath();
        if (super.getSession().getAttribute(tCollectionPath) != null) {
            Layer tCollection = (Layer)super.getSession().getAttribute(tCollectionPath);
            System.out.println("Sha: " + tCollection.getSha());
            return tCollection;
        } else {
            // Get from repo
            Layer tAnnos = new Layer();
            try {
                List<RepositoryContents> tAnnoList = this.getFiles(tPath);
                if (tAnnoList.get(0).getContent() == null) {
                    throw new IOException("Empty collection found");
                } else {
                    tAnnos.loadJson(this.contents2Json(tAnnoList.get(0)));
                    System.out.println("Sha: " + tAnnoList.get(0).getSha());
                    tAnnos.setSha(tAnnoList.get(0).getSha());
                }
            } catch (IOException tExcpt) {    
                System.err.println("Failed to retrieve /annotations/collection.json so creating new"); 
                //tExcpt.printStackTrace();
                // create new manifest
                tAnnos = Layer.createEmpty(tPath.getWeb(), "All annotations loaded in " + pRepo.generateId() + " project");
            }

            super.getSession().setAttribute(tCollectionPath, tAnnos);
            return tAnnos;
        }

    }

    public void saveAnnotations(final Repository pRepo, final Layer pCollection) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "/annotations/collection.json");
        String tCollectionPath = pRepo.generateId() + tPath.getPath();
        super.getSession().setAttribute(tCollectionPath, pCollection);
        
        ContentResponse tResponse = this.uploadFile(tPath, JsonUtils.toPrettyString(pCollection.toJson()), pCollection.getSha());

        List<RepositoryContents> tAnnoList = this.getFiles(tPath);
        pCollection.setSha(tAnnoList.get(0).getSha());
    }


    public ContentResponse uploadFile(final RepositoryPath pPath, final String pContents) throws IOException {
        try {
            return uploadFile(pPath, pContents, null);
        } catch (IOException tExcpt) {
            System.err.println("Failed to load " + pPath + " due to " + tExcpt);
            tExcpt.printStackTrace();
            throw tExcpt;
        }
    }
    // Sha required if its an update
    public ContentResponse uploadFile(final RepositoryPath pPath, final String pContents, final String pSHA) throws IOException {
        RepositoryContents tFile = new RepositoryContents();
        tFile.setName(pPath.getName());
        if (!pPath.equals("/")) {
            tFile.setPath(pPath.getParentPath());
        }
        tFile.setEncoding("base64");
        tFile.setType(RepositoryContents.TYPE_FILE);
        tFile.setContent(encode(pContents));
        if (pSHA != null) {
            tFile.setSha(pSHA);
            System.out.println("Sha: " + tFile.getSha());
        }

        ExtendedContentService tService = this.getContentService();
        return tService.setContents(pPath.getRepo(), tFile);
    }

    // Sha required if its an update
    public ContentResponse uploadEncodedFile(final RepositoryPath pPath, final String pContents) throws IOException {
        RepositoryContents tFile = new RepositoryContents();
        tFile.setName(pPath.getName());
        if (!pPath.equals("/")) {
            tFile.setPath(pPath.getParentPath());
        }
        tFile.setEncoding("base64");
        tFile.setType(RepositoryContents.TYPE_FILE);
        tFile.setContent(pContents);

        ExtendedContentService tService = this.getContentService();
        return tService.setContents(pPath.getRepo(), tFile);
    }

    public void replaceFile(final RepositoryPath pRepoPath, String pContent) throws IOException {
        List<RepositoryContents> tGitHubFile = this.getFiles(pRepoPath);

        if (tGitHubFile.size() != 1) {
            throw new IOException("Expected 1 file called " + pRepoPath + " but found " + tGitHubFile.toString());
        } else {
            this.uploadFile(pRepoPath, pContent, tGitHubFile.get(0).getSha());
        }
    }

    public void deleteFile(final RepositoryPath pPath) throws IOException {
        try {
            ExtendedContentService tService = this.getContentService();
            tService.deleteFile(pPath, "Removing " + pPath.getName());
        } catch (RequestException tExcpt) {
            if (tExcpt.getStatus() != 200) {
                System.out.println("Failed to delete file due to exception");
                tExcpt.printStackTrace();
                throw tExcpt;
            }
        }
    }

    public void deleteRecursive(final RepositoryPath pDir) throws IOException {
        Repository tRepo = pDir.getRepo();
        for (RepositoryContents tChild: pDir.getContents()) {
            if (tChild.getType().equals("dir")) {
                RepositoryPath tChildPath = this.processPath(tRepo, tChild.getPath());
                System.out.println("Calling  deleteRecursive on " + tChildPath);
                this.deleteRecursive(tChildPath);
            } else {
                RepositoryPath tPath = new RepositoryPath(tRepo, tChild.getPath());
                System.out.println("Calling delete file on " + tPath);
                this.deleteFile(tPath);
            }
        }
    }

    public List<RepositoryContents> getFiles(final RepositoryPath pRepoPath) throws IOException {
        List<RepositoryContents> tContents = null;
       
        ContentsService tService = this.getContentService();
        if (pRepoPath.getPath() == null) {
            tContents = tService.getContents(pRepoPath.getRepo());
        } else {    
            _logger.debug("Contents service {}", tService.getClass().getName()); 
            _logger.debug("Repo service {}", getClass().getName()); 
            tContents = tService.getContents(pRepoPath.getRepo(), pRepoPath.getPath());
        }
        return tContents;
    }

    public Repository createRepo(final String pName) throws IOException {
        ExtendedRepositoryServices tService = new ExtendedRepositoryServices(this.getClient());
        
        Repository tNewRepo = new Repository();
        tNewRepo.setName(pName);
        tNewRepo.setHomepage("https://" + this.getUser().getLogin() + ".github.io/" + pName + "/index.html");
        System.out.println(new Gson().toJson(tNewRepo));

        Repository tLocalCopy = tService.createRepository(tNewRepo);
        tService.setTopic(tLocalCopy, "iiif-training-workbench");
        
        uploadDirectory(tLocalCopy, "/", Config.getConfig().getRepoTemplate());

        tService.createPages(tLocalCopy, tLocalCopy.getDefaultBranch(), "/");
        tService.pagesEnforceHttps(tLocalCopy, tLocalCopy.getDefaultBranch(), "/");
        return tLocalCopy;
    }

    public WorkflowStatus getActiveImageWorkflows(final IRepositoryIdProvider pRepo) {
        ExtendedRepositoryServices tService = this.getRepositoryService();

        return tService.getWorkflowRuns(pRepo, "convert_images.yml");
    }

    public WorkflowStatus getActivePagesWorkflows(final IRepositoryIdProvider pRepo) {
        ExtendedRepositoryServices tService = this.getRepositoryService();

        WorkflowStatus tStatus = tService.getWorkflowRuns(pRepo);
        if (tStatus.getWorkflowRuns() != null) {
            List<WorkflowRun> tPages = new ArrayList<WorkflowRun>();
            System.out.println("Found " + tStatus.getWorkflowRuns().size() + " workflows");
            for (WorkflowRun tRun : tStatus.getWorkflowRuns()) {
                System.out.println("Checking " + tRun.getName());
                if (tRun.getName().equals("pages build and deployment")) {
                    tPages.add(tRun);
                }
            }
            tStatus.setWorkflowRuns(tPages);
        } else {
            System.out.println("No pages workflow running");
        }
        return tStatus;
    }

    public List<ContentResponse> uploadTemplateFiles(final IRepositoryIdProvider pRepo, final String[] pFiles) throws IOException {
        List<ContentResponse> tAdditions = new ArrayList<ContentResponse>();
        File tTemplateDir = Config.getConfig().getRepoTemplate();
        ExtendedContentService tService = this.getContentService();

        for (int i = 0; i < pFiles.length; i++) {
            File tFile = new File(tTemplateDir, pFiles[i]);

            System.out.println("Uploading " + tFile.getPath() + " to " + new File(pFiles[i]).getParentFile().getPath() + "/" + tFile.getName());
            RepositoryContents tRepFile = new RepositoryContents();
            tRepFile.setName(tFile.getName());
            tRepFile.setPath(new File(pFiles[i]).getParentFile().getPath());
            tRepFile.setEncoding("base64");
            tRepFile.setType(RepositoryContents.TYPE_FILE);
            tRepFile.setContent(encode(tFile));

            tAdditions.add(tService.setContents(pRepo, tRepFile));
        }
        return tAdditions;        
    }    

    public List<RepositoryContents> uploadDirectory(final IRepositoryIdProvider pRepo, final String pPath, final File pDataDir) throws IOException {
        ExtendedContentService tService = this.getContentService();
        File[] tFiles = pDataDir.listFiles();

        for (int i = 0; i < tFiles.length; i++) {
            if (tFiles[i].isDirectory()) {
                String tNewPath = "";
                if (pPath.equals("/")) {
                    tNewPath = "/";
                } else {
                    tNewPath = pPath + "/";
                }
                uploadDirectory(pRepo, tNewPath + tFiles[i].getName(), tFiles[i]);
            } else {
                System.out.println("Uploading " + tFiles[i].getName() + " to " + pPath);
                RepositoryContents tFile = new RepositoryContents();
                tFile.setName(tFiles[i].getName());
                if (!pPath.equals("/")) {
                    tFile.setPath(pPath);
                }
                tFile.setEncoding("base64");
                tFile.setType(RepositoryContents.TYPE_FILE);
                tFile.setContent(encode(tFiles[i]));

                tService.setContents(pRepo, tFile);
            }
        }

        return null;
    }

    public static String decode(final String pString) throws IOException {
        byte[] decodedBytes = Base64.getDecoder().decode(pString.replace("\n","").getBytes(StandardCharsets.UTF_8));
        return new String(decodedBytes);
    }

    public static String encode(final String pString) throws IOException {
        return Base64.getEncoder().encodeToString(pString.getBytes());
    }

    public static String encode(final File pFile) throws IOException {
        String base64Image = "";
        FileInputStream imageInFile = new FileInputStream(pFile);
        // Reading a Image file from file system
        byte imageData[] = new byte[(int) pFile.length()];
        imageInFile.read(imageData);
        base64Image = Base64.getEncoder().encodeToString(imageData);
        return base64Image;
    }
}
