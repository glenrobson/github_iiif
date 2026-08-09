package com.gdmrdigital.iiif.test.mock;

import com.gdmrdigital.iiif.model.github.ExtendedContentService;
import com.gdmrdigital.iiif.model.github.RepositoryPath;
import com.gdmrdigital.iiif.controllers.Repo;

import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.RequestError;
import org.eclipse.egit.github.core.RequestError;

import static org.mockito.Mockito.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.List;
import java.util.ArrayList;

public class MockContentService extends ExtendedContentService {
    final Logger _logger = LoggerFactory.getLogger(MockContentService.class);

    protected File _dir = null;
    public MockContentService(final File pDir) {
        _dir = pDir;
    }

    public List<RepositoryContents> getContents(final IRepositoryIdProvider pRepo, final String pPath) throws IOException {
        File tFileOrFolder = new File(_dir, pRepo.generateId() + "/" + pPath);
        _logger.debug("Looking for files " + tFileOrFolder.getPath());
        if (!tFileOrFolder.exists()) {
            RequestError tError = mock(RequestError.class);
            when(tError.getMessage()).thenReturn("Not Found");
            throw new RequestException(tError, 404);
        }
        List<RepositoryContents> tContents = new ArrayList<RepositoryContents>();
        if (tFileOrFolder.isFile()) {
            RepositoryContents tContent = new RepositoryContents();
            tContent.setPath(pPath);
            tContent.setType("file");
            tContent.setContent(Repo.encode(tFileOrFolder));
            tContent.setEncoding(RepositoryContents.ENCODING_BASE64);

            tContents.add(tContent);
        } else {
        }

        return tContents;
    }

    public ContentResponse setContents(final IRepositoryIdProvider pRepo, final RepositoryContents pContent) throws IOException {
        return this.setContents(pRepo, pContent, "");
    }

    public ContentResponse setContents(final IRepositoryIdProvider pRepo, final RepositoryContents pContent, final String pMessage) throws IOException {
        File toAdd = new File(_dir, pRepo.generateId() + "/" + pContent.getPath() + "/" + pContent.getName());
        System.out.println("File " + toAdd + " exists: " + toAdd.exists());
        if (toAdd.exists()){
            // Throw exception 
            throw new RequestException(new RequestError() {
                public String getMessage() {
                    return "For 'properties/sha', nil is not a string.";
                }
            }, 422);
        } else {
            _logger.debug("Writing to File: {}, Repo {}, Path: {}", toAdd, pRepo.generateId(), pContent.getPath());
            toAdd.getParentFile().mkdirs();

            PrintWriter tWriter = new PrintWriter(toAdd);
            tWriter.println(Repo.decode(pContent.getContent()));

            tWriter.flush();
            tWriter.close();
        }    

        return null;
    }

    public void deleteFile(final RepositoryPath pPath, final String pMessage) throws IOException {
        File tFileToDelete = new File(_dir, pPath.getRepo().generateId() + "/" + pPath.getPath());

        tFileToDelete.delete();
    }
}
