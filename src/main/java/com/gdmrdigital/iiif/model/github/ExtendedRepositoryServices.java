package com.gdmrdigital.iiif.model.github;

import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.client.RequestException;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;

import com.gdmrdigital.iiif.model.github.pages.SiteStatus;
import com.gdmrdigital.iiif.model.github.pages.Source;

import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.Gson;

import java.io.IOException;

public class ExtendedRepositoryServices extends RepositoryService {

    public ExtendedRepositoryServices() {
        super();
    }

    public ExtendedRepositoryServices(final GitHubClient pClient) {
        super(pClient);
    }

    public List<String> setTopic(final IRepositoryIdProvider pRepo, final String pTopic) throws IOException {
        List<String> tTopics = new ArrayList<String>();
        tTopics.add(pTopic);
        return setTopics(pRepo, tTopics);
    }

    public List<String> setTopics(final IRepositoryIdProvider pRepo, final List<String> pTopics) throws IOException {
        // PUT /repos/{owner}/{repo}/topics
        // "names":["names"]
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append("/").append(pRepo.generateId());
		uri.append("/topics");

        System.out.println(new Gson().toJson(pTopics));
        Map<String, Object> tParams = new HashMap<String,Object>();
        tParams.put("names", pTopics);

        System.out.println(new Gson().toJson(tParams));

        client.setHeaderAccept("application/vnd.github.mercy-preview+json");
        return ((Topics)client.put(uri.toString(), tParams, Topics.class)).getNames();
    }


    public List<String> getTopics(final IRepositoryIdProvider pRepo) throws IOException {
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append("/").append(pRepo.generateId());
		uri.append("/topics");

        GitHubRequest tRequest = new GitHubRequest();
        tRequest.setUri(uri);
        tRequest.setArrayType(String.class);

        return (List<String>)client.get(tRequest).getBody();
    }

    public SiteStatus createPages(final IRepositoryIdProvider pRepo, final String pBranch, final String pPath) throws IOException {
        //-H "Accept: application/vnd.github.switcheroo-preview+json" \
        // POST
        //  https://api.github.com/repos/octocat/hello-world/pages 
        // -d '{"source":{"branch":"branch","path":"path"}}'

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append("/").append(pRepo.generateId());
		uri.append("/pages");
        System.out.println("Creating pages URI: " + uri.toString());

        Map<String, Object> tParams = new HashMap<String,Object>();
        tParams.put("source", new Source(pBranch, pPath).toMap());
        System.out.println(tParams);

        client.setHeaderAccept("application/vnd.github.switcheroo-preview+json");
        return (SiteStatus)client.post(uri.toString(), tParams, SiteStatus.class);
    }

    public SiteStatus pagesEnforceHttps(final IRepositoryIdProvider pRepo, final String pBranch, final String pPath) throws IOException {
        //-H "Accept: application/vnd.github.switcheroo-preview+json" \
        // PUT
        //  https://api.github.com/repos/octocat/hello-world/pages 
        // -d '{"cname":"octocatblog.com","source":{"branch":"main","path":"/"}}'

        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
		uri.append("/").append(pRepo.generateId());
		uri.append("/pages");
        System.out.println("Creating pages URI: " + uri.toString());

        Map<String, Object> tParams = new HashMap<String,Object>();
        tParams.put("source", new Source(pBranch, pPath).toMap());
        tParams.put("https_enforced", true);
        System.out.println(tParams);

        client.setHeaderAccept("application/vnd.github.switcheroo-preview+json");
        try {
            return (SiteStatus)client.put(uri.toString(), tParams, SiteStatus.class);
        } catch (RequestException tExcpt) {
            // If this method is called straight after creating the repo
            // then the Certificate hasn't been created and an exception is thrown...
            System.err.println("Failed to set enforceHTTPs on " + pRepo.generateId() + " due to: " + tExcpt.getMessage());
        }
        return null;
    }

    public class Topics {
        protected List<String> _names = null;

        public void setNames(final List<String> pNames) {
            _names = pNames;
        }

        public List<String> getNames() {
            return _names;
        }
    }
}
