package com.gdmrdigital.iiif.test.mock;

import com.gdmrdigital.iiif.controllers.Repo;
import com.gdmrdigital.iiif.model.github.ExtendedContentService;
import com.gdmrdigital.iiif.model.github.ExtendedRepositoryServices;
import com.gdmrdigital.iiif.model.iiif.Manifest;

import java.net.URL;

import org.eclipse.egit.github.core.Repository;

import java.io.IOException;

public class MockRepo extends Repo {
    protected ExtendedContentService _contentService = null;
    protected ExtendedRepositoryServices _repoService = null;
    protected Manifest _manifest = null;

    public MockRepo() {
    }
    
    public void setImages(final Manifest pManifest) {
        _manifest = pManifest;
    }

    public Manifest getImages(final Repository pRepo) throws IOException {
        if (_manifest == null) {
            return super.getImages(pRepo);
        } else {
            return _manifest;
        }
    }

    public Manifest getImagesFromRepo(final Repository pRepo) throws IOException {
        if (_manifest == null) {
            return super.getImagesFromRepo(pRepo);
        } else {
            return _manifest;
        }
    }

    protected ExtendedContentService getContentService() {
        return _contentService;
    }

    public void setContentService(final ExtendedContentService pService) {
        _contentService = pService;
    }

    protected ExtendedRepositoryServices getRepositoryService() {
        return _repoService;
    }

    public void setRepositoryService(final ExtendedRepositoryServices pService) {
        _repoService = pService;
    }

    public boolean checkAvilable(final URL pInfoJson, final int pTries) throws IOException {
        return true;
    }
}
