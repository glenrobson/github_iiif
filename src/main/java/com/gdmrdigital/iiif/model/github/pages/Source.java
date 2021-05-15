package com.gdmrdigital.iiif.model.github.pages;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.HashMap;

public class Source {
    @SerializedName("branch") 
    protected String _branch = null;
    @SerializedName("path")
    protected String _path = "/";

    public Source() {
    }

    public Source(final String pBranch, final String pPath) {
        this.setBranch(pBranch);
        this.setPath(pPath);
    }

    public String toString() {
        return "Branch " + _branch + " _path: " + _path;
    }

    public Map<String,Object> toMap() {
        Map tMap = new HashMap<String,String>();
        tMap.put("branch", _branch);
        tMap.put("path", _path);

        return tMap;
    }

    /**
     * Get _branch.
     *
     * @return _branch as String.
     */
    public String getBranch() {
        return _branch;
    }
    
    /**
     * Set _branch.
     *
     * @param _branch the value to set.
     */
    public void setBranch(final String pBranch) {
         _branch = pBranch;
    }
    
    /**
     * Get _path.
     *
     * @return _path as String.
     */
    public String getPath() {
        return _path;
    }
    
    /**
     * Set _path.
     *
     * @param _path the value to set.
     */
    public void setPath(final String pPath) {
         _path = pPath;
    }
}
