package com.gdmrdigital.iiif.model.iiif;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.net.URL;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

import uk.co.gdmrdigital.iiif.image.InfoJson;
import com.gdmrdigital.iiif.processor.ImageProcessor;
import com.gdmrdigital.iiif.processor.ImageProcessor.Status;
import com.gdmrdigital.iiif.model.github.GitHubObj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.utils.JsonUtils;

public class Manifest extends GitHubObj {
    final Logger _logger = LoggerFactory.getLogger(Manifest.class);

    protected List<Canvas> _canvases = new ArrayList<Canvas>();
    protected Map<String, Object> _json = null;

    public static Manifest createEmpty(final String pId, final String pLabel) {
        Manifest tManifest = new Manifest();
        Map<String, Object> tJson = new HashMap<String,Object>();
        tJson.put("@context", "http://iiif.io/api/presentation/3/context.json");
        tJson.put("id", pId);
        tJson.put("type", "Manifest");
        try {
            tJson.put("label", JsonUtils.fromString("{ \"en\": [ \"" + pLabel + "\" ] }"));
        } catch (IOException tExcpt) {
            tExcpt.printStackTrace();
        }
        tJson.put("items", new ArrayList<Map<String, Object>>());

        tManifest.loadJson(tJson);
        return tManifest;
    }

    public Manifest() {
    }

    public String getId() {
        if (_json != null) {
            if (_json.containsKey("@id")) {
                return (String)_json.get("@id");
            } else if (_json.containsKey("id")) {
                return (String)_json.get("id");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public List<Canvas> getCanvases() {
        return _canvases;
    }

    public boolean hasProcess(final String pId) {
        for (Canvas tCanvas : _canvases) {
            if (tCanvas instanceof InProgressCanvas) {
                InProgressCanvas tInProgress = (InProgressCanvas)tCanvas;
                if (tInProgress.getProcessId().equals(pId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Remove any in process canvases that have finished either through failure, success or just
     * the process Id is unkown. This can happen if the workbench server is restarted. 
     * @return boolean if a canvas was removed
     */ 
    public boolean removeFinishedInProcess() {
        boolean deleted = false;
        Iterator<Canvas> tCanvases = _canvases.iterator();
        while (tCanvases.hasNext()) {
            Canvas tCanvas = tCanvases.next();
            if (tCanvas instanceof InProgressCanvas) {
                InProgressCanvas tInProcess = (InProgressCanvas)tCanvas;
                Status tStatus = ImageProcessor.getStatus(tInProcess.getProcessId());
                if (tStatus == null || tStatus.equals(Status.FINISHED)|| tStatus.equals(Status.FAILED)) {
                    tCanvases.remove();
                    deleted = true;
                }
            }
        }
        return deleted;
    }

    public boolean hasImage(final String pImageId) {
        boolean tFound = false;
        for (Canvas tCanvas : _canvases) {
            if (tCanvas.getImageId().equals(pImageId)) {
                tFound = true;
                break;
            }
        }
        return tFound;
    }

    // addCanvas(final String pProccessId, final InfoJson pImageInfo)
    public void addCanvas(final String pProccessId, final InfoJson pImageInfo) {
        for (Canvas tCanvas : _canvases) {
            if (tCanvas instanceof InProgressCanvas) {
                InProgressCanvas tInProgress = (InProgressCanvas)tCanvas;
                if (tInProgress.getProcessId().equals(pProccessId)) {
                    tInProgress.convertToCanvas(pImageInfo);
                }
            }
        }
    }

    // addInProcess(final String pProccessId, final URL pInfoJson, final String pLabel)
    public void addInProcess(final String pProccessId, final URL pInfoJson, final String pLabel) {
        InProgressCanvas tCanvas = new InProgressCanvas(pProccessId, pInfoJson);
        tCanvas.setLabel(pLabel);

        _canvases.add(0, tCanvas);
    }

    public List<InProgressCanvas> getInProgress() {
        List<InProgressCanvas> tInProgress = new ArrayList<InProgressCanvas>();
        if (_json.containsKey("items")) {
            for (Canvas tCanvas : _canvases) {
                if (tCanvas instanceof InProgressCanvas) {
                    tInProgress.add((InProgressCanvas)tCanvas);       
                }
            }
        }
        return tInProgress;
    }

    public void loadJson(final Map<String, Object> pJson) {
        _json = pJson;
        loadCanvases();
    }


    public void loadJson(final File pFile) throws IOException {
        this.loadJson((Map<String,Object>)JsonUtils.fromInputStream(new FileInputStream(pFile)));
    }

    protected void loadCanvases() {
        _canvases = new ArrayList<Canvas>();
        if (_json.containsKey("items")) {
            for (Map<String, Object> tCanvas : (List<Map<String,Object>>)_json.get("items")) {
                if (tCanvas.containsKey("proccess_id")) {
                    _canvases.add(new InProgressCanvas(tCanvas));       
                } else {
                    _canvases.add(new Canvas(tCanvas));       
                }
            }
        }
    }

    // toJson
    /**
     * Returns the manifest as JSON. This will include inProcess canvases.
     */
    public Map<String,Object> toJson() {
        List<Map<String,Object>> tCanvasesJson = new ArrayList<Map<String,Object>>();
        for (Canvas tCanvas : _canvases) {
            tCanvasesJson.add(tCanvas.toJson());
        }
        _json.put("items", tCanvasesJson); 
        return _json;
    }

    /**
     * Returns the manifest without any in process canvses
     */
    public Map<String,Object> toStoreJson() {
        List<Map<String,Object>> tCanvasesJson = new ArrayList<Map<String,Object>>();
        for (Canvas tCanvas : _canvases) {
            if (!(tCanvas instanceof InProgressCanvas) || !((InProgressCanvas)tCanvas).isInProcess()) {
                tCanvasesJson.add(tCanvas.toJson());
            }
        }
        _json.put("items", tCanvasesJson); 
        return _json;

    }

    /**
     * Returns the minimum amount of detail for the manifest
     * to allow it to be embedded in a collection.
     * This info is id, type and label.
     */
    public Map<String, Object> toEmbedJson() {
        Map<String,Object> tJson = new HashMap<String,Object>();
        if (_json.containsKey("id")) {
            tJson.put("id", _json.get("id"));
            tJson.put("type", _json.get("type"));
            tJson.put("label", _json.get("label"));
        } else {
            tJson.put("@id", _json.get("@id"));
            tJson.put("@type", _json.get("@type"));
            tJson.put("label", _json.get("label"));
        }

        return tJson;
    }
}
