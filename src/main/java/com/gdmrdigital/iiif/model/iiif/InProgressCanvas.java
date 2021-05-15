package com.gdmrdigital.iiif.model.iiif;

import com.github.jsonldjava.utils.JsonUtils;

import java.net.URL;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import java.io.IOException;

import uk.co.gdmrdigital.iiif.image.InfoJson;

public class InProgressCanvas extends Canvas {
    protected String _proccessId = "";
    protected String _label = "";
    protected String _baseURI = "";
    protected URL _infoJson = null;
    protected boolean _isInProcess = true;
    public InProgressCanvas(final String pProccessId) {
        super();
        _proccessId = pProccessId;
        _isInProcess = true;
    }

    public URL getInfoJson() {
        return _infoJson;
    }

    public void setInfoJson(final URL pInfoJson) {
        _infoJson = pInfoJson;
    }

    public String getLabel() {
        return _label;
    }

    public void setLabel(final String pLabel) {
        _label = pLabel;
    }

    public String getProcessId() {
        return _proccessId;
    }

    public String getBaseURI() {
        if (_baseURI == null || _baseURI.isEmpty()) {
            _baseURI = _infoJson.toString().replace("/info.json","");
        }
        return _baseURI;
    } 

    public Map<String,Object> createBasic() {
        Map<String, Object> tJson = new HashMap<String,Object>();
        tJson.put("id", this.getBaseURI() + "/canvas/");
        tJson.put("type", "Canvas");
        try {
            tJson.put("label", JsonUtils.fromString("{ \"none\": [ \"" +  _label + "\" ] }"));
        } catch (IOException tExcpt) {
            tExcpt.printStackTrace();
        }

        return tJson;
    }

    public void convertToCanvas(final InfoJson pImageInfo) {
        _json = this.createBasic();
        _json.put("height", pImageInfo.getHeight());
        _json.put("width", pImageInfo.getWidth());

        List<Map<String,Object>> tAnnoPages = new ArrayList<Map<String,Object>>();
        _json.put("items",tAnnoPages);

        Map<String,Object> tAnnoPage = new HashMap<String,Object>();
        tAnnoPages.add(tAnnoPage);

        tAnnoPage.put("id", this.getBaseURI() + "/AnnoPage/");
        tAnnoPage.put("type", "AnnotationPage");

        List<Map<String,Object>> tAnnos = new ArrayList<Map<String,Object>>();
        tAnnoPage.put("items", tAnnos);

        Map<String,Object> tAnno = new HashMap<String,Object>();
        tAnnos.add(tAnno);
        tAnno.put("id", this.getBaseURI() + "/annotation/");
        tAnno.put("type", "Annotation");
        tAnno.put("motivation", "Painting");
        tAnno.put("target", (String)_json.get("id"));
        
        Map<String, Object> tBody = new HashMap<String, Object>();
        tAnno.put("body", tBody);
        if (pImageInfo.getVersion().equals(InfoJson.VERSION3)) {
            tBody.put("id", pImageInfo.getId() + "/full/max/0/default.jpg");
        } else {
            tBody.put("id", pImageInfo.getId() + "/full/full/0/default.jpg");
        }
        tBody.put("type", "Image");
        tBody.put("format", "image/jpeg");
        tBody.put("height", pImageInfo.getHeight());
        tBody.put("width", pImageInfo.getWidth());

        List<Map> tServices = new ArrayList<Map>();
        tServices.add(pImageInfo.toJson());
        tBody.put("service", tServices);

        _isInProcess = false;
    }

    public Map<String,Object> toJson() {
        if (_isInProcess) {
            _json = this.createBasic();
            _json.put("proccess_id", _proccessId);

            return _json;
        } else {
            return super.toJson();
        }
    }
}
