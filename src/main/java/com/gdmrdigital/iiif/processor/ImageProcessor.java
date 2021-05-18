package com.gdmrdigital.iiif.processor;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.File;
import java.io.IOException;

import uk.co.gdmrdigital.iiif.image.Tiler;
import uk.co.gdmrdigital.iiif.image.IIIFImage;
import uk.co.gdmrdigital.iiif.image.InfoJson;
import uk.co.gdmrdigital.iiif.image.ImageInfo;

import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.iiif.Manifest;

import java.net.HttpURLConnection;

import org.eclipse.egit.github.core.Repository;

public class ImageProcessor extends Thread {
    public enum Status {
        INIT,
        TILE_GENERATION,
        GIT_UPLOAD,
        FAILED,
        UPDATING_IMG_LIST,
        PAGES_UPDATING,
        FINISHED
    }
    protected String _id = "";
    protected Status _status = Status.INIT;
    protected String _fileId = "";
    protected String _iiifVersion = "";
    protected String _baseURI = null;
    protected File _inputImage = null;
    protected File _tileDestination = null;
    protected Repo _repoControl = null;
    protected Repository _repo = null;
    protected String _githubPath = "";
    
    public ImageProcessor() {
        _id = UUID.randomUUID().toString();
        _instances.put(_id, this);
    }

    protected static Map<String, ImageProcessor> _instances = new HashMap<String, ImageProcessor>();
    public static Status getStatus(final String pId) {
        if (_instances.get(pId) != null) {
            return _instances.get(pId).getStatus();
        } else {
            return null; // Instance not found
        }
    }

    public void run() {
        try {
            setStatus(Status.TILE_GENERATION);

            IIIFImage tImage = new IIIFImage(_inputImage);
            tImage.setId(_fileId);
            ImageInfo tImageInfo = new ImageInfo(tImage);
            Tiler.createImage(tImageInfo, _tileDestination, _baseURI, _iiifVersion);
            InfoJson tInfo =  new InfoJson(tImageInfo, _baseURI, _iiifVersion);

            setStatus(Status.GIT_UPLOAD);
            _repoControl.uploadDirectory(_repo, _githubPath, new File(_tileDestination, _fileId));

            setStatus(Status.UPDATING_IMG_LIST);

            Manifest tImageManifest = _repoControl.getImages(_repo);
            URL tInfoJson = new URL(tInfo.getId() + "/info.json");
            if (!tImageManifest.hasProcess(_id)) {
                tImageManifest.addInProcess(_id, tInfoJson, _fileId);
            }
            tImageManifest.addCanvas(_id, tInfo);

            _repoControl.saveImageManifest(_repo, tImageManifest);

            setStatus(Status.PAGES_UPDATING);

            int tTries = 10;
            int tTry = 0;
            boolean tSuccess = false;
            HttpURLConnection con = null;
            while (tTry < tTries) {
                con = (HttpURLConnection) tInfoJson.openConnection();
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
            if (!tSuccess) {
                setStatus(Status.FAILED);
                System.out.println("Failed to get " + tInfoJson.toString() + " after " + tTries);
            } else {
                setStatus(Status.FINISHED);
                _instances.remove(_id);
            }
        } catch (IOException tExcpt) {
            setStatus(Status.FAILED);
            System.err.println("Failed to create and upload tiles due to: " + tExcpt);
            tExcpt.printStackTrace();
        }
    }
    
    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getIdentifier() {
        return _id;
    }
    
    /**
     * Get status.
     *
     * @return status as String.
     */
    public Status getStatus() {
        return _status;
    }
    
    /**
     * Set status.
     *
     * @param status the value to set.
     */
    public void setStatus(final Status pStatus) {
         _status = pStatus;
    }
    
    /**
     * Get fileId.
     *
     * @return fileId as String.
     */
    public String getFileId() {
        return _fileId;
    }
    
    /**
     * Set fileId.
     *
     * @param fileId the value to set.
     */
    public void setFileId(final String pFileId) {
         _fileId = pFileId;
    }
    
    /**
     * Get iiifVersion.
     *
     * @return iiifVersion as String.
     */
    public String getIiifVersion() {
        return _iiifVersion;
    }
    
    /**
     * Set iiifVersion.
     *
     * @param iiifVersion the value to set.
     */
    public void setIiifVersion(final String pIiifVersion) {
         _iiifVersion = pIiifVersion;
    }
    
    /**
     * Get baseURI.
     *
     * @return baseURI as URI.
     */
    public String getBaseURI() {
        return _baseURI;
    }
    
    /**
     * Set baseURI.
     *
     * @param baseURI the value to set.
     */
    public void setBaseURI(final String pBaseURI) {
         _baseURI = pBaseURI;
         if (!_baseURI.endsWith("/")) {
            _baseURI += "/";
         }
    }
    
    /**
     * Get inputImage.
     *
     * @return inputImage as File.
     */
    public File getInputImage() {
        return _inputImage;
    }
    
    /**
     * Set inputImage.
     *
     * @param inputImage the value to set.
     */
    public void setInputImage(final File pInputImage) {
         _inputImage = pInputImage;
    }
    
    /**
     * Get tileDestination.
     *
     * @return tileDestination as File.
     */
    public File getTileDestination() {
        return _tileDestination;
    }
    
    /**
     * Set tileDestination.
     *
     * @param tileDestination the value to set.
     */
    public void setTileDestination(final File pTileDestination) {
         _tileDestination = pTileDestination;
    }
    
    /**
     * Get repoControl.
     *
     * @return repoControl as Repo.
     */
    public Repo getRepoControl() {
        return _repoControl;
    }
    
    /**
     * Set repoControl.
     *
     * @param repoControl the value to set.
     */
    public void setRepoControl(final Repo pRepoControl) {
         _repoControl = pRepoControl;
    }
    
    /**
     * Get repo.
     *
     * @return repo as Repository
     */
    public Repository getRepo() {
        return _repo;
    }
    
    /**
     * Set repo.
     *
     * @param repo the value to set.
     */
    public void setRepo(final Repository pRepo) {
         _repo = pRepo;
    }
    
    /**
     * Get githubPath.
     *
     * @return githubPath as String.
     */
    public String getGithubPath() {
        return _githubPath;
    }
    
    /**
     * Set githubPath.
     *
     * @param githubPath the value to set.
     */
    public void setGithubPath(final String pGithubPath) {
         _githubPath = pGithubPath;
    }
}
