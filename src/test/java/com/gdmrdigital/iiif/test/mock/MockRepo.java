package com.gdmrdigital.iiif.test.mock;

import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.ExtendedContentService;

import java.net.URL;

import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;

public class MockRepo extends Repo {
    protected ExtendedContentService _contentService = null;
    protected RepositoryService _repoService = null;

    public MockRepo() {
    }
    
    protected ExtendedContentService getContentService() {
        return _contentService;
    }

    public void setContentService(final ExtendedContentService pService) {
        _contentService = pService;
    }

    protected RepositoryService getRepositoryService() {
        return _repoService;
    }

    public void setRepositoryService(final RepositoryService pService) {
        _repoService = pService;
    }

    public boolean checkAvilable(final URL pInfoJson, final int pTries) throws IOException {
        return true;
    }
}
