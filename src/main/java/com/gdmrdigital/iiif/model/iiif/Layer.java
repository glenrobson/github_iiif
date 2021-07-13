package com.gdmrdigital.iiif.model.iiif;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import java.net.URL;
import java.net.MalformedURLException;

import java.io.IOException;

import com.gdmrdigital.iiif.model.github.GitHubObj;

import com.github.jsonldjava.utils.JsonUtils;

public class Layer extends GitHubObj {
    protected List<URL> _annotationLists = new ArrayList<URL>();
    protected Map<String, Object> _json = null;

    public static Layer createEmpty(final String pId, final String pLabel) {
        Layer tLayer = new Layer();
        Map<String, Object> tJson = new HashMap<String,Object>();
        tJson.put("@context", "http://iiif.io/api/presentation/2/context.json");
        tJson.put("@id", pId);
        tJson.put("@type", "sc:Layer");
        if (pLabel != null) {
            tJson.put("label", pLabel);
        }
        tJson.put("otherContent", new ArrayList<URL>());

        tLayer.loadJson(tJson);
        return tLayer;

    }

    public Layer() {
    }

    public String getId() {
        return (String)_json.get("@id");
    }

    public void addAnnotationList(final URL pURL) {
        _annotationLists.add(pURL);
    }

    public void remove(final URL pURL) {
        _annotationLists.remove(pURL);
    }

    public void loadJson(final Map<String, Object> pJson) {
        _json = pJson;
        loadAnnotations();
    }

    public boolean hasAnnotationList(final Map<String, Object> pJson) {
        for (URL tAnnoURL : _annotationLists) {
            if (tAnnoURL.toString().equals(pJson.get("@id"))) {
                return true;
            }
        }
        return false;
    }

    protected void loadAnnotations() {
        _annotationLists = new ArrayList<URL>();

        if (_json.containsKey("otherContent")) {
            for (String tURL : (List<String>)_json.get("otherContent")) {
                try { 
                    _annotationLists.add(new URL(tURL));
                } catch (MalformedURLException tExcpt) {
                    System.err.println("Failed to add " + tURL + " due to:");
                    tExcpt.printStackTrace();
                }
            }
        }
    }

    public Map<String,Object> toJson() {
        _json.put("otherContent", _annotationLists);
        return _json;
    }
}
