package com.gdmrdigital.iiif.model.iiif;

import java.util.Map;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    public String getLabel() {
        if (_json.containsKey("label")) {
            StringBuffer tBuff = new StringBuffer();
            for (String tLang : ((Map<String,Object>)_json.get("label")).keySet()) {
                tBuff.append(((Map<String,List<String>>)_json.get("label")).get(tLang).get(0));
            }
            return tBuff.toString();
        } else {
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public String getImageId() {
        Map<String,Object> tAnnotationPage = ((List<Map<String,Object>>)_json.get("items")).get(0);
        Map<String,Object> tAnnotation = ((List<Map<String,Object>>)tAnnotationPage.get("items")).get(0);
        Map<String,Object> tBody = (Map<String,Object>)tAnnotation.get("body");
        Map<String,Object> tService = ((List<Map<String,Object>>)tBody.get("service")).get(0);
        if (tService.get("@id") != null) {
            return (String)tService.get("@id");
        } else {
            return (String)tService.get("id");
        }
    }

    public Map<String, Object> toJson() {
        return _json;
    }

    public String toString() {
        StringBuffer tBuff = new StringBuffer("Label:");
        tBuff.append(this.getLabel());
        tBuff.append("\n");
        tBuff.append("Image id: ");
        tBuff.append(this.getImageId());

        return tBuff.toString();
    }
}
