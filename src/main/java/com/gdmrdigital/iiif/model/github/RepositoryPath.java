package com.gdmrdigital.iiif.model.github;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;

import java.nio.charset.StandardCharsets;

import java.util.List;
import java.util.ArrayList;
import java.util.Base64;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

public class RepositoryPath {
    protected Repository _repo = null;
    protected String _path = null;
    protected List<RepositoryContents> _contents = null;
    protected boolean _directory = false;
    protected boolean _isRoot = false;

    public RepositoryPath(final Repository pRepo, final String pPath) {
        System.out.println("Path " + pPath);
        _repo = pRepo;
        _path = pPath;

        if (pPath == null) {
            _isRoot = true;
            _directory = true;
        } else {
            _isRoot = false;
        }
    }

    public boolean isRoot() {
        return _isRoot;
    }

    public List<RepositoryContents> getContents() {
        return _contents;
    }

    public List<RepositoryContents> getType(final String pType) {
        List<RepositoryContents> tContents = new ArrayList<RepositoryContents>();
        for (RepositoryContents tContent : _contents) {
            if (tContent.getType().equals(pType)) {
                tContents.add(tContent);
            }
        }

        return tContents;
    }

    public List<RepositoryContents> getDirectories() {
        return this.getType("dir");
    }

    public List<RepositoryContents> getFiles() {
        return this.getType("file");
    }

    public List<RepositoryContents> getImages() {
        List<RepositoryContents> tContents = new ArrayList<RepositoryContents>();
        if (_path.equals("images")) {
            for (RepositoryContents tContent : _contents) {
                if (!tContent.getName().equals("README.md")) {
                    tContents.add(tContent);
                }
            }
        }

        return tContents;
    }


    public String getWeb() {
        StringBuffer tURLBuff = new StringBuffer(_repo.getHomepage().substring(0,_repo.getHomepage().lastIndexOf("/")));
        tURLBuff.append("/");
        if (_path != null) {
            tURLBuff.append(_path);
        }
        String tURL = tURLBuff.toString();
        if (tURL.endsWith("README.md")) {
            if (_path.equals("README.md")) {
                // No way to access the root README
                tURL = null;
            } else {
                tURL = tURL.substring(0, tURL.length() - "README.md".length()) + "index.html";
            }
        } else if (tURL.endsWith(".md")) {
            tURL = tURL.substring(0, tURL.length() - ".md".length()) + ".html";
        }

        return tURL;
    }

    public void setContents(final List<RepositoryContents> pContents) {
        _contents = pContents;
        if (_contents.size() == 1) {
            System.out.println("supplied path " + _path + " retrieved path " + _contents.get(0).getPath());
            _directory = !_path.equals(_contents.get(0).getPath());
        } else {
            _directory = true;
        }
    }

    public String getParentPath() {
        return new File(_path).getParent();
    }

    public String getName() {
        return new File(_path).getName();
    }

    public String getExtension() {
        String tFilename = this.getName();
        return tFilename.substring(tFilename.indexOf(".") + 1);
    }

    public String getContent() {
        byte[] decodedBytes = Base64.getDecoder().decode(_contents.get(0).getContent().replace("\n","").getBytes(StandardCharsets.UTF_8));

        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    /**
     * Get repo.
     *
     * @return repo as Repository.
     */
    public Repository getRepo() {
        return _repo;
    }
    
    /**
     * Set repo.
     *
     * @param repo the value to set.
     */
    public void setRepo(final Repository pRepo) {
         _repo = pRepo;
    }
    
    /**
     * Get path.
     *
     * @return path as String.
     */
    public String getPath() {
        return _path;
    }
    
    /**
     * Set path.
     *
     * @param path the value to set.
     */
    public void setPath(final String pPath) {
         _path = pPath;
    }
    
    /**
     * Get directory.
     *
     * @return directory as boolean.
     */
    public boolean isDirectory() {
        return _directory;
    }
    
    /**
     * Set directory.
     *
     * @param directory the value to set.
     */
    public void setDirectory(final boolean pDirectory) {
         _directory = pDirectory;
    }
}
