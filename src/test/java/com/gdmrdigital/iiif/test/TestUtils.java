package com.gdmrdigital.iiif.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.rules.TestWatcher;

import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;

import java.util.Map;

import java.io.IOException;
import java.io.File;

import com.github.jsonldjava.utils.JsonUtils;

public class TestUtils {
    final Logger _logger = LoggerFactory.getLogger(TestUtils.class);
    protected boolean _retainFailedData = true;

    @Rule
    public TemporaryFolder _tmp = new TemporaryFolder();
    protected File _persistentDir = null;

    public TestUtils() {
    }

    public void setup() throws IOException {
        File tTmpDir = new File(System.getProperty("java.io.tmpdir"));
        _persistentDir = new File(tTmpDir, new java.text.SimpleDateFormat("dd-mm-yyyy_HH-mm-ss-SS").format(new java.util.Date()));
    }

    protected File newFolder(final String pPath) throws IOException {
        if (_retainFailedData) {
            return new File(_persistentDir, pPath);
        } else {
            return _tmp.newFolder(pPath);
        }
    }

    protected Map<String, Object> getJSON(final String pPath) throws IOException {
        return (Map<String,Object>)JsonUtils.fromInputStream(getClass().getResourceAsStream(pPath));
    }

    protected Repository createRepo(final String pName, final String pUser) {
        Repository tRepo = new Repository();
        tRepo.setOwner(this.createUser(pUser));
        tRepo.setName(pName);
        tRepo.setHomepage("http://" + pUser + ".github.io/" + pName + "/");
        return tRepo;
    }

    protected User createUser(final String pId) {
        User tUser = new User();
        tUser.setName(pId);
        tUser.setLogin(pId);
        return tUser;
    }

    @Rule
    public TestWatcher watchman = new TestWatcher() {
        @Override
        protected void failed(Throwable e, org.junit.runner.Description description) {
            if (_retainFailedData && _persistentDir != null) {
                System.out.println("Test " + description + " failed. Test data is aviliable in: " + _persistentDir.getPath());
            } else {
                this.deleteData();
            }
        }
        @Override
        protected void succeeded(org.junit.runner.Description description) {
            this.deleteData();
        }

        protected void deleteData() {
            // Should really delete _persistentDir and all of its children
        }
    };


}
