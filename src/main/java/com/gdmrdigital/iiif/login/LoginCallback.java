package com.gdmrdigital.iiif.login;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;

import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

import java.util.concurrent.ExecutionException;

import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.User;

import com.gdmrdigital.iiif.Config;

import java.io.IOException;

import java.util.Collections;
import java.util.Random;
import java.util.Map;

public class LoginCallback extends HttpServlet {
	public void init(final ServletConfig pConfig) throws ServletException {
		super.init(pConfig);
	}

	public void doGet(final HttpServletRequest pReq, final HttpServletResponse pRes) throws IOException {
        HttpSession tSession = pReq.getSession();
        try {
            final OAuth20Service service = Config.getConfig().getOAuthService(pReq);

            OAuth2AccessToken accessToken = service.getAccessToken(pReq.getParameter("code"));
            tSession.setAttribute("user_token", accessToken);

            GitHubClient client = new GitHubClient();
            client.setOAuth2Token(accessToken.getAccessToken());

            UserService tService = new UserService(client);
            User tUser = tService.getUser();
            tSession.setAttribute("user", tUser);
            
            if (tSession.getAttribute("oauth_url") != null) {
                pRes.sendRedirect((String)tSession.getAttribute("oauth_url"));
            } else {
                pRes.sendRedirect("index.xhtml");
            }
        } catch (InterruptedException tExcpt) {
            tExcpt.printStackTrace();
        } catch (ExecutionException tExcpt) {
            tExcpt.printStackTrace();
            // should redirect to login fail page
        }
    }
}
