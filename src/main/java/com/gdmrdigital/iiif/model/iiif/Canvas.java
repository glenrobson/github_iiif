package com.gdmrdigital.iiif.model.iiif;

import java.util.Map;

public class Canvas {
    protected Map<String,Object> _json = null;

    protected Canvas() {
    }

    public Canvas(final Map<String, Object> pJson) {
        _json = pJson;
    }

    public Map<String, Object> toJson() {
        return _json;
    }
}
