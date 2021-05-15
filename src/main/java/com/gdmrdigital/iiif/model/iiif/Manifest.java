package com.gdmrdigital.iiif.model.iiif;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.net.URL;

import java.io.IOException;

import uk.co.gdmrdigital.iiif.image.InfoJson;
import com.gdmrdigital.iiif.model.github.GitHubObj;

import com.github.jsonldjava.utils.JsonUtils;

public class Manifest extends GitHubObj {
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
        InProgressCanvas tCanvas = new InProgressCanvas(pProccessId);
        tCanvas.setLabel(pLabel);
        tCanvas.setInfoJson(pInfoJson);

        _canvases.add(0, tCanvas);
    }

    public void loadJson(final Map<String, Object> pJson) {
        _json = pJson;
        loadCanvases();
    }

    protected void loadCanvases() {
        _canvases = new ArrayList<Canvas>();
        if (_json.containsKey("items")) {
            for (Map<String, Object> tCanvas : (List<Map<String,Object>>)_json.get("items")) {
                _canvases.add(new Canvas(tCanvas));       
            }
        }
    }

    // toJson
    public Map<String,Object> toJson() {
        List<Map<String,Object>> tCanvasesJson = new ArrayList<Map<String,Object>>();
        for (Canvas tCanvas : _canvases) {
            tCanvasesJson.add(tCanvas.toJson());
        }
        _json.put("items", tCanvasesJson); 
        return _json;
    }

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
