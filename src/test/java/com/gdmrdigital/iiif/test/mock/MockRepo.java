package com.gdmrdigital.iiif.test.mock;

import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.ExtendedContentService;

import java.net.URL;

import java.io.IOException;

public class MockRepo extends Repo {
    protected ExtendedContentService _contentService = null;
    public MockRepo() {
    }
    
    protected ExtendedContentService getContentService() {
        return _contentService;
    }

    public void setContentService(final ExtendedContentService pService) {
        _contentService = pService;
    }

    public boolean checkAvilable(final URL pInfoJson, final int pTries) throws IOException {
        return true;
    }
}
