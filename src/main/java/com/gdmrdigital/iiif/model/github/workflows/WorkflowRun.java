package com.gdmrdigital.iiif.model.github.workflows;

import java.util.Date; 

public class WorkflowRun {
    protected String id = "";
    protected String name = "";
    protected String path = "";
    protected String workflowId = "";
    protected Date createdAt = null;
    protected Date updatedAt = null;
    protected Date runStartedAt = null;
    protected String status = "";
    
    public WorkflowRun() {

    }

    public String toString() {
        StringBuffer tBuff = new StringBuffer();
        tBuff.append("Id:");
        tBuff.append(id);
        tBuff.append("\nName:");
        tBuff.append(name);
        tBuff.append("\nPath:");
        tBuff.append(path);
        tBuff.append("\nWorkflowId:");
        tBuff.append(workflowId);
        tBuff.append("\nCreatedAt:");
        tBuff.append(createdAt);
        tBuff.append("\nUpdatedAt:");
        tBuff.append(updatedAt);
        tBuff.append("\nRun Started at:");
        tBuff.append(runStartedAt);
        tBuff.append("\nStatus:");
        tBuff.append(status);
        return tBuff.toString();
    }

    /**
     * Get id.
     *
     * @return id as String.
     */
    public String getId() {
        return id;
    }
    
    /**
     * Set id.
     *
     * @param id the value to set.
     */
    public void setId(final String pId) {
         id = pId;
    }
    
    /**
     * Get name.
     *
     * @return name as String.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set name.
     *
     * @param name the value to set.
     */
    public void setName(final String pName) {
         name = pName;
    }
    
    /**
     * Get path.
     *
     * @return path as String.
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Set path.
     *
     * @param path the value to set.
     */
    public void setPath(final String pPath) {
         path = pPath;
    }
    
    /**
     * Get workflowId.
     *
     * @return workflowId as String.
     */
    public String getWorkflowId() {
        return workflowId;
    }
    
    /**
     * Set workflowId.
     *
     * @param workflowId the value to set.
     */
    public void setWorkflowId(final String pWorkflowId) {
         workflowId = pWorkflowId;
    }
    
    /**
     * Get createdAt.
     *
     * @return createdAt as Date.
     */
    public Date getCreatedAt() {
        return createdAt;
    }
    
    /**
     * Set createdAt.
     *
     * @param createdAt the value to set.
     */
    public void setCreatedAt(final Date pCreatedAt) {
         createdAt = pCreatedAt;
    }
    
    /**
     * Get updatedAt.
     *
     * @return updatedAt as Date.
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    /**
     * Set updatedAt.
     *
     * @param updatedAt the value to set.
     */
    public void setUpdatedAt(final Date pUpdatedAt) {
         updatedAt = pUpdatedAt;
    }
    
    /**
     * Get runStartedAt.
     *
     * @return runStartedAt as Date.
     */
    public Date getRunStartedAt() {
        return runStartedAt;
    }
    
    /**
     * Set runStartedAt.
     *
     * @param runStartedAt the value to set.
     */
    public void setRunStartedAt(final Date pRunStartedAt) {
         runStartedAt = pRunStartedAt;
    }
    
    /**
     * Get status.
     *
     * @return status as String.
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * Set status.
     *
     * @param status the value to set.
     */
    public void setStatus(final String pStatus) {
         status = pStatus;
    }

}
