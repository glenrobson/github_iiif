package com.gdmrdigital.iiif.controllers;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.annotation.PostConstruct;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.User;

import com.github.scribejava.core.model.OAuth2AccessToken;

import javax.faces.context.FacesContext;

import javax.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

@RequestScoped
@ManagedBean
public class Repo {
    
    @PostConstruct
    public void init() {
    }

    protected HttpSession getSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession tSession = (HttpSession)facesContext.getExternalContext().getSession(true);
        return tSession;
    }

    public GitHubClient getClient() {
        OAuth2AccessToken accessToken = (OAuth2AccessToken)this.getSession().getAttribute("user_token");

        GitHubClient tClient = new GitHubClient();
        tClient.setOAuth2Token(accessToken.getAccessToken());

        return tClient;
    }

    public User getUser() {
        HttpSession tSession = this.getSession();
        return (User)tSession.getAttribute("user");
    }

    public List<SearchRepository> getRepos() throws IOException {
        RepositoryService tService = new RepositoryService(this.getClient());
        
        Map<String, String> tParams = new HashMap<String,String>();
        tParams.put("topic", "iiif-training-workbench");
        //tParams.put("topic", "iiif-search");
        tParams.put("user", this.getUser().getLogin());
        System.out.println("User " + this.getUser().getLogin());

        List<SearchRepository> tResults = tService.searchRepositories(tParams);
        System.out.println("Found " + tResults.size() + " compatiable repos");
        int max = 0;
        for (SearchRepository tRepo : tResults) {
            System.out.println("Found repo "+  tRepo.getName() + " id: " + tRepo.getId());
            if (max++ > 10) { 
                break;
            }
        }

        return tResults;
    }

    public Repository createRepo(final String pName) {
        RepositoryService tService = new RepositoryService(this.getClient());
        
        Repository tSource = tService.getRepository("glenrobson", "iiif-training-workbench");

        // May take some time for the folk to actually happen
        Repository tLocalCopy = tService.forkRepository(tSource);

        // Sleep
        Repository tNewCopy = null;
        for (int i = 0; i < 20; i++) {
            tNewCopy = tService.getRepository(this.getUser().getLogin(), pName);
            if (tNewCopy != null) {
                break;
            } else {
                try {
                    //Pause for 4 seconds
                    Thread.sleep(4000);
                } catch (InterruptedException tExcpt) {
                }
            }
        }

        return tNewCopy;
    }
}
