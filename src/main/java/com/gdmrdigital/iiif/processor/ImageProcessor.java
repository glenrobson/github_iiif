package com.gdmrdigital.iiif.processor;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.io.IOException;

import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowStatus;
import com.gdmrdigital.iiif.model.iiif.Manifest;

import org.eclipse.egit.github.core.Repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;

public class ImageProcessor {
    private static final Logger _logger = LoggerFactory.getLogger(ImageProcessor.class);
    public enum Status {
        INIT,
        TILE_GENERATION,
        FAILED,
        PAGES_UPDATING,
        FINISHED
    }

    public static final long INIT_GRACE = 60000 * 1; // 1 minute
    protected String _id = "";
    protected Status _status = Status.INIT;
    protected URL _infoJson = null;
    protected Repo _repoControl = null;
    protected Repository _repo = null;
    protected Date _startTime = null;
    
    public ImageProcessor() {
        _id = UUID.randomUUID().toString();
        _instances.put(_id, this);
        _startTime = new Date();
    }

    protected static Map<String, ImageProcessor> _instances = new HashMap<String, ImageProcessor>();
    public static Status getStatus(final String pId) {
        if (_instances.get(pId) != null ) {
            ImageProcessor inst = _instances.get(pId);
            if (inst.isGeneratingTiles()) {
                _logger.debug("Sending TILE_GENERATION for " + pId + " " + inst.getInfoURL());
                return Status.TILE_GENERATION;
            } else if (inst.isUpdatingWebsite()) {
                _logger.debug("Sending PAGES_UPDATING for " + pId + " " + inst.getInfoURL());
                return Status.PAGES_UPDATING;
            } else if (inst.isImageUploaded()) {
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException tExcpt) {}
                // Give the pages update one more chance
                if (inst.isUpdatingWebsite()) {
                    _logger.debug("Sending PAGES_UPDATING for " + pId + " " + inst.getInfoURL());
                    return Status.PAGES_UPDATING; 
                }
                _logger.debug("Sending FINISHED for " + pId + " " + inst.getInfoURL());
                _instances.remove(pId);
                return Status.FINISHED;
            } else {
                if (new Date().getTime() - inst.getStartTime().getTime() > INIT_GRACE) { 
                    _logger.debug("Sending FAILED for " + pId + " " + inst.getInfoURL());
                    return Status.FAILED;
                } else {    
                    _logger.debug("Sending INIT for " + pId + " " + inst.getInfoURL());
                    return Status.INIT;
                }
            }
        } else {
            return null; // Instance not found
        }
    }

    public boolean isGeneratingTiles() {
        WorkflowStatus tStatus = _repoControl.getActiveImageWorkflows(_repo);
        return tStatus.hasActive();
    }

    public boolean isUpdatingWebsite() {
        WorkflowStatus tStatus = _repoControl.getActivePagesWorkflows(_repo);
        return tStatus.hasActive();
    }

    public boolean isImageUploaded() {
        try {
            Manifest tManifest = _repoControl.getImagesFromRepo(_repo);
            System.out.println("Looking for " + _infoJson.toString());
            System.out.println(tManifest.getCanvases());
            return tManifest.hasImage(_infoJson.toString());
        } catch (IOException tExcpt) {
            System.err.println("Failed to find image in manifest.json due to:");
            tExcpt.printStackTrace();
            return false;
        }    
    }

    public Date getStartTime() {
        return _startTime;
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
    
    public void setInfoURL(final URL pInfoJson) {
        _infoJson = pInfoJson;
    } 

    public URL getInfoURL() {
        return _infoJson;
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
}
