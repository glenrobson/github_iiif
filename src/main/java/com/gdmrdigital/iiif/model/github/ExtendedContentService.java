package com.gdmrdigital.iiif.model.github;

import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.client.GitHubClient;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

public class ExtendedContentService extends ContentsService {
    public ExtendedContentService() {
        super();
    }

    public ExtendedContentService(final GitHubClient pClient) {
        super(pClient);
    }

    public ContentResponse setContents(final IRepositoryIdProvider pRepo, final RepositoryContents pContent) throws IOException {
        return this.setContents(pRepo, pContent, "Adding " + pContent.getName());
    }


    public ContentResponse setContents(final IRepositoryIdProvider pRepo, final RepositoryContents pContent, final String pMessage) throws IOException {
        // put /repos/{owner}/{repo}/contents/{path}
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append("/").append(pRepo.generateId());
		uri.append("/contents");
        if (pContent.getPath() != null) {
            uri.append(pContent.getPath());
        }
		uri.append("/").append(pContent.getName());

        Map<String, Object> tParams = new HashMap<String,Object>();
        tParams.put("content", pContent.getContent());
        tParams.put("message", pMessage);
        tParams.put("sha", pContent.getSha());

        client.setHeaderAccept("application/vnd.github.v3+json");
        return (ContentResponse)client.put(uri.toString(), tParams, ContentResponse.class);
    }

    public class ContentResponse {
        protected RepositoryContents _content = null;
        protected RepositoryCommit _commit = null;
        public ContentResponse() {
        }
        
        /**
         * Get content.
         *
         * @return content as RepositoryContents.
         */
        public RepositoryContents getContent() {
            return _content;
        }
        
        /**
         * Set content.
         *
         * @param content the value to set.
         */
        public void setContent(final RepositoryContents pContent) {
             _content = pContent;
        }
        
        /**
         * Get commit.
         *
         * @return commit as RepositoryCommit.
         */
        public RepositoryCommit getCommit() {
            return _commit;
        }
        
        /**
         * Set commit.
         *
         * @param commit the value to set.
         */
        public void setCommit(final RepositoryCommit pCommit) {
             _commit = pCommit;
        }
    }
}
