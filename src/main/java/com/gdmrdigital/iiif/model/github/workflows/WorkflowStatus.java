package com.gdmrdigital.iiif.model.github.workflows;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class WorkflowStatus {
    @SerializedName("workflow_runs")
    protected List<WorkflowRun> _workflowRuns = null;

    public WorkflowStatus() {
    }

    // get active workflows

    public void setWorkflowRuns(final List<WorkflowRun> pWorkflows) {
        _workflowRuns = pWorkflows;
    }

    public List<WorkflowRun> getWorkflowRuns() {
        return _workflowRuns;
    }

    public boolean hasActive() {
        return _workflowRuns != null && !_workflowRuns.isEmpty();
    }

    public String toString() {
        if (_workflowRuns != null) {
            StringBuffer tBuff = new StringBuffer("Workflow runs: (" + _workflowRuns.size() + ")");
            for (WorkflowRun tRun : _workflowRuns) {
                tBuff.append(tRun.toString());
            }    
            return tBuff.toString();    
        } else {
            return "Workflow runs is null";
        }    
    }
}
