package com.gdmrdigital.iiif.login;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.apis.GitHubApi;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;

import java.io.IOException;

import com.gdmrdigital.iiif.Config;

public class OAuth extends HttpServlet {

	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
	}

	public void doGet(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
        HttpSession tSession = pReq.getSession();

        final OAuth20Service service = Config.getConfig().getOAuthService(pReq);

        final String secretState = "github_" + new Random().nextInt(999_999);
        Map<String, String> additionalParams = new HashMap<String,String>();
        additionalParams.put("scope", "public_repo, workflow");
        final String authorizationUrl = service.createAuthorizationUrlBuilder()
                .state(secretState)
                .additionalParams(additionalParams)
                .build();        

        tSession.setAttribute("oauth_state", secretState);        
        pRes.sendRedirect(authorizationUrl);
    }
}
