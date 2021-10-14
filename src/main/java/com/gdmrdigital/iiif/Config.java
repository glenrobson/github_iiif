package com.gdmrdigital.iiif;

import javax.servlet.http.HttpServletRequest;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.apis.GitHubApi;

import java.io.File;

public class Config {

    public Config() {
    }

    public String getClientId() {
        return getConfig("github_client_id");
    }

    private String getConfig(final String pKey) {
        if (System.getProperty(pKey) != null) {
            return System.getProperty(pKey);
        } else {
            return System.getenv(pKey);
        }

    }

    public String getStorage() {
        return this.getConfig("storage");
    }

    public String getClientSecret() {
        return getConfig("github_client_secret");
    }

    public String getBaseURI(final HttpServletRequest pReq) {
        if (getConfig("baseURI") != null) {
            return getConfig("baseURI");
        } else {
            int tBase = 0;
			String[] tURL = pReq.getRequestURL().toString().split("/");
			String tServletName = "";
			if (pReq.getServletPath().matches(".*/[a-zA-Z0-9.]*$")) {
				tServletName = pReq.getServletPath().replaceAll("/[a-zA-Z0-9.]*$","").replaceAll("/","");
                if (tServletName.isEmpty()) {
                    tServletName = pReq.getServletPath().replaceAll("/","");
                }
			} else {
				tServletName = pReq.getServletPath().replaceAll("\\/","");
			}

			for (int i = tURL.length - 1; i >=0 ; i--) {
				if (tURL[i].equals(tServletName)) {
					tBase = i;
					break;
				}
			}
			StringBuffer tBaseURL = new StringBuffer();
			for (int i = 0; i < tBase; i++) {
				tBaseURL.append(tURL[i] + "/");
			}
			String tBaseURLStr = tBaseURL.toString();
			if (tBaseURLStr.endsWith("/")) {
				 tBaseURLStr = tBaseURLStr.replaceAll("/$","");
			}
			return tBaseURLStr;
        }
    }

    public File getRepoTemplate() {
        String tEnvDetails = getConfig("repo_template");
        if (tEnvDetails == null || tEnvDetails.isEmpty()) {
            // Running locally
            return new File("docs");
        } else {
            return new File(tEnvDetails);
        }
    }

    public OAuth20Service getOAuthService(final HttpServletRequest pReq) {
        return new ServiceBuilder(this.getClientId())
                    .apiSecret(this.getClientSecret())
                    .callback(this.getBaseURI(pReq) + "/login-callback")
                    .build(GitHubApi.instance());
    }


    protected static Config _config = null;
    public static Config getConfig() {
        if (_config == null) {
            _config = new Config();
        }
        return _config;
    }
}
