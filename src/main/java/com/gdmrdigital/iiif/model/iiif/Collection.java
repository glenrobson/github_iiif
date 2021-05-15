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

public class Collection extends GitHubObj {
    protected List<Manifest> _manifests = new ArrayList<Manifest>();
    protected Map<String, Object> _json = null;

    public static Collection createEmpty(final String pId, final String pLabel) {
        Collection tCollection = new Collection();
        Map<String, Object> tJson = new HashMap<String,Object>();
        tJson.put("@context", "http://iiif.io/api/presentation/3/context.json");
        tJson.put("id", pId);
        tJson.put("type", "Collection");
        try {
            tJson.put("label", JsonUtils.fromString("{ \"en\": [ \"" + pLabel + "\" ] }"));
        } catch (IOException tExcpt) {
            tExcpt.printStackTrace();
        }
        tJson.put("items", new ArrayList<Map<String, Object>>());

        tCollection.loadJson(tJson);
        return tCollection;
    }

    public Collection() {
    }


    public void add(final Manifest pManifest) {
        _manifests.add(pManifest);
    }

    public void loadJson(final Map<String, Object> pJson) {
        _json = pJson;
        loadManifests();
    }

    protected void loadManifests() {
        _manifests = new ArrayList<Manifest>();
        if (_json.containsKey("items")) {
            for (Map<String, Object> tManifestJson : (List<Map<String,Object>>)_json.get("items")) {
                Manifest tManifest = new Manifest();
                tManifest.loadJson(tManifestJson);
                _manifests.add(tManifest);       
            }
        }
    }

    // toJson
    public Map<String,Object> toJson() {
        List<Map<String,Object>> tManifestsJson = new ArrayList<Map<String,Object>>();
        for (Manifest tManifest : _manifests) {
            tManifestsJson.add(tManifest.toEmbedJson());
        }
        _json.put("items", tManifestsJson); 
        return _json;
    }
}
