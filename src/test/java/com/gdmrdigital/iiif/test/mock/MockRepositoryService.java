package com.gdmrdigital.iiif.test.mock;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.File;

public class MockRepositoryService extends RepositoryService {
    protected File _project = null;

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
}
