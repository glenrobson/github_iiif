package com.gdmrdigital.iiif.test.mock;

import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import com.gdmrdigital.iiif.model.github.ExtendedRepositoryServices;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowStatus;
import com.gdmrdigital.iiif.model.github.workflows.WorkflowRun;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;

public class MockRepositoryService extends ExtendedRepositoryServices {
    protected File _project = null;
    protected File _workflowJson = null;

    public MockRepositoryService(final File pFile) {
        _project = pFile;
    }

    public Repository getRepository(final String pUser, final String pRepo) {
        Repository tRepo = new Repository();

        User tUser = new User();
        tUser.setName(pUser);
        tUser.setLogin(pUser);

        tRepo.setOwner(tUser);
        tRepo.setName(pRepo);
        tRepo.setHomepage("http://" + pUser + ".github.io/" + pRepo + "/");

        return tRepo;
    }

    public void setWorkflowJson(final File pJson) {
        _workflowJson = pJson;
    }

    public WorkflowStatus getWorkflowRuns(final IRepositoryIdProvider pRepo) {
        return this.getWorkflowRuns(pRepo,"all");
    }
    public WorkflowStatus getWorkflowRuns(final IRepositoryIdProvider pRepo, final String pWorkflow) {
        if (_workflowJson == null) {
            return new WorkflowStatus();
        }
        try {

            // Create Gson instance with custom date format
            Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
                .create();
            JsonReader reader = new JsonReader(new FileReader(_workflowJson));
            WorkflowStatus tStatus = gson.fromJson(reader, WorkflowStatus.class);
            if (!pWorkflow.equals("all")) {
                List<WorkflowRun> tRuns = new ArrayList<WorkflowRun>();
                for (WorkflowRun tRun : tStatus.getWorkflowRuns()) {
                    if (pWorkflow.contains(tRun.getName())) {
                        tRuns.add(tRun);
                    }
                }
                tStatus.setWorkflowRuns(tRuns);
            }
            return tStatus; 
        } catch (IOException tExcpt) {
            tExcpt.printStackTrace();
        }
        return null;
    }
}
