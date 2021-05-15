package com.gdmrdigital.iiif.model.github.pages;

public class SiteStatus {
    protected String _url = "";
    protected String _status = "";
    protected String _cname = "";
    protected String _custom404 = "";
    protected String _htmlUrl = "";
    protected Source _source = null;

    public SiteStatus() {
    }
    
    /**
     * Get url.
     *
     * @return url as String.
     */
    public String getUrl() {
        return _url;
    }
    
    /**
     * Set url.
     *
     * @param url the value to set.
     */
    public void setUrl(final String pUrl) {
         _url = pUrl;
    }
    
    /**
     * Get status.
     *
     * @return status as String.
     */
    public String getStatus() {
        return _status;
    }
    
    /**
     * Set status.
     *
     * @param status the value to set.
     */
    public void setStatus(final String pStatus) {
         _status = pStatus;
    }
    
    /**
     * Get cname.
     *
     * @return cname as String.
     */
    public String getCname() {
        return _cname;
    }
    
    /**
     * Set cname.
     *
     * @param cname the value to set.
     */
    public void setCname(final String pCname) {
         _cname = pCname;
    }
    
    /**
     * Get custom404.
     *
     * @return custom404 as String.
     */
    public String getCustom404() {
        return _custom404;
    }
    
    /**
     * Set custom404.
     *
     * @param custom404 the value to set.
     */
    public void setCustom404(final String pCustom404) {
         _custom404 = pCustom404;
    }
    
    /**
     * Get htmlUrl.
     *
     * @return htmlUrl as String.
     */
    public String getHtmlUrl() {
        return _htmlUrl;
    }
    
    /**
     * Set htmlUrl.
     *
     * @param htmlUrl the value to set.
     */
    public void setHtmlUrl(final String pHtmlUrl) {
         _htmlUrl = pHtmlUrl;
    }
    
    /**
     * Get source.
     *
     * @return source as Source.
     */
    public Source getSource() {
        return _source;
    }
    
    /**
     * Set source.
     *
     * @param source the value to set.
     */
    public void setSource(final Source pSource) {
         _source = pSource;
    }
}
