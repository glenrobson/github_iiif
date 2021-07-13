package com.gdmrdigital.iiif.model.iiif;

import java.util.Map;

public class Canvas {
    protected Map<String,Object> _json = null;

    protected Canvas() {
    }

    public Canvas(final Map<String, Object> pJson) {
        _json = pJson;
    }

    public String getId() {
        if (_json.containsKey("id")) {
            return (String)_json.get("id");
        } else if (_json.containsKey("@id")) {
            return (String)_json.get("@id");
        } else {
            throw new IllegalArgumentException("Missing id in Canvas");
        }
    }

    public Map<String, Object> toJson() {
        return _json;
    }
}
