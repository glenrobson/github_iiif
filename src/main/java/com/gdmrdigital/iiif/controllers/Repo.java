package com.gdmrdigital.iiif.controllers;

import com.gdmrdigital.iiif.model.github.ExtendedRepositoryServices;
import com.gdmrdigital.iiif.model.github.ExtendedContentService;
import com.gdmrdigital.iiif.model.github.ExtendedContentService.ContentResponse;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.model.iiif.Manifest;
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

import com.github.scribejava.core.model.OAuth2AccessToken;

import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

@RequestScoped
@ManagedBean
public class Repo extends Session {

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

    protected User getUser() {
        UserService tService = new UserService(super.getSession());
        return tService.getUser();
    }

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
        RepositoryPath tRepoPath = new RepositoryPath(tRepo, tPath);
        tRepoPath.setContents(this.getFiles(tRepoPath));

        return tRepoPath;
    }

    public List<SearchRepository> getRepos() throws IOException {
        RepositoryService tService = new RepositoryService(this.getClient());
        
        Map<String, String> tParams = new HashMap<String,String>();
        tParams.put("topic", "iiif-training-workbench");
        //tParams.put("topic", "iiif-search");
        tParams.put("user", this.getUser().getLogin());
        System.out.println("User " + this.getUser().getLogin());

        List<SearchRepository> tResults = tService.searchRepositories(tParams);
        System.out.println("Found " + tResults.size() + " compatiable repos");
        int max = 0;
        for (SearchRepository tRepo : tResults) {
            System.out.println("Found repo "+  tRepo.getName() + " id: " + tRepo.getId());
            if (max++ > 10) { 
                break;
            }
        }

        return tResults;
    }

    public Repository getRepo(final String pId) throws IOException {
        System.out.println("Looking for repo " + pId);
        String tCacheId = "repo/" + pId;
        Repository tRepo = null;
        if (super.getSession().getAttribute(tCacheId) == null) {
            RepositoryService tService = new RepositoryService(this.getClient());

            tRepo = tService.getRepository(this.getUser().getLogin(), pId);
            super.getSession().setAttribute(tCacheId, tRepo);
        } else {
            System.out.println("Cache");
            tRepo = (Repository)super.getSession().getAttribute(tCacheId);
        }
        return tRepo;
    }

    public Manifest getImages(final Repository pRepo) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "images/manifest.json");
        String tManifestPath = pRepo.generateId() + tPath.getPath();
        if (super.getSession().getAttribute(tManifestPath) != null) {
            Manifest tManifest = (Manifest)super.getSession().getAttribute(tManifestPath);
            System.out.println("Sha: " + tManifest.getSha());
            return (Manifest)super.getSession().getAttribute(tManifestPath);
        } else {
            // Get from repo
            Manifest tImagesManifest = new Manifest();
            try {
                List<RepositoryContents> tManifestList = this.getFiles(tPath);
                if (tManifestList.get(0).getContent() == null) {
                    throw new IOException("Empty manifest found");
                } else {
                    tImagesManifest.loadJson(this.contents2Json(tManifestList.get(0)));
                    System.out.println("Sha: " + tManifestList.get(0).getSha());
                    tImagesManifest.setSha(tManifestList.get(0).getSha());
                }
            } catch (IOException tExcpt) {    
                System.err.println("Failed to retrieve /images/manifest.json so creating new"); 
                tExcpt.printStackTrace();
                // create new manifest
                tImagesManifest = Manifest.createEmpty(tPath.getWeb(), "All images loaded in " + pRepo.generateId() + " project");
            }

            super.getSession().setAttribute(tManifestPath, tImagesManifest);
            return tImagesManifest;
        }
    }

    public Map<String, Object> contents2Json(final RepositoryContents pContents) throws IOException {
        String tContent = pContents.getContent();
        if (pContents.getEncoding() != null && pContents.getEncoding().equals(RepositoryContents.ENCODING_BASE64)) {
            tContent = decode(tContent);
        }

        return (Map<String, Object>)JsonUtils.fromString(tContent);
    }

    public void saveImageManifest(final Repository pRepo, final Manifest pManifest) throws IOException {
        RepositoryPath tPath = new RepositoryPath(pRepo, "/images/manifest.json");
        String tManifestPath = pRepo.generateId() + tPath.getPath();
        super.getSession().setAttribute(tManifestPath, pManifest);
        
        ContentResponse tResponse = this.uploadFile(tPath, JsonUtils.toPrettyString(pManifest.toJson()), pManifest.getSha());

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

        ExtendedContentService tService = new ExtendedContentService(this.getClient());
        return tService.setContents(pPath.getRepo(), tFile);
    }

    public List<RepositoryContents> getFiles(final RepositoryPath pRepoPath) throws IOException {
        List<RepositoryContents> tContents = null;
       
        ContentsService tService = new ContentsService(this.getClient());
        if (pRepoPath.getPath() == null) {
            tContents = tService.getContents(pRepoPath.getRepo());
        } else {    
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
        return tLocalCopy;
    }

    public List<RepositoryContents> uploadDirectory(final IRepositoryIdProvider pRepo, final String pPath, final File pDataDir) throws IOException {
        ExtendedContentService tService = new ExtendedContentService(this.getClient());
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
