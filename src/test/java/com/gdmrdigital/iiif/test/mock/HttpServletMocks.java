package com.gdmrdigital.iiif.test.mock;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;
import org.mockito.stubbing.Answer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.Mockito;

import org.eclipse.egit.github.core.User;

import com.github.scribejava.core.model.OAuth2AccessToken;

import org.eclipse.egit.github.core.Repository;

import java.util.Map;
import java.util.HashMap;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServletMocks {
    static final Logger _logger = LoggerFactory.getLogger(HttpServletMocks.class);

    public static HttpSession createSession(final Map pItems) {
        return createSession(pItems, null);
    }

    public static HttpSession createSession(final Map pItems, final Repository pRepo) {
        HttpSession tSession = mock(HttpSession.class);

        if (pRepo != null) {
            pItems.put("user", pRepo.getOwner());
            pItems.put("user_token", new OAuth2AccessToken("access_token"));

            pItems.put("repo/" + pRepo.getName(), pRepo);
        }

        when(tSession.getAttribute(anyString())).thenAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                String key = (String) aInvocation.getArguments()[0];
                return pItems.get(key);
            }
        });
        Mockito.doAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                String key = (String) aInvocation.getArguments()[0];
                Object value = aInvocation.getArguments()[1];
                pItems.put(key, value);
                return null;
            }
        }).when(tSession).setAttribute(anyString(), any());

        return tSession;
    }

    public static HttpServletRequest createRequest(final String pRequestURI, final String pMethod, final Map pParameters, final HttpSession pSession) {
        HttpServletRequest tReq = mock(HttpServletRequest.class);       

        _logger.debug("Request URI: {} ", pRequestURI);

        when(tReq.getParameter(anyString())).thenAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                String key = (String) aInvocation.getArguments()[0];
                return pParameters.get(key);
            }
        });

        when(tReq.getMethod()).thenReturn(pMethod);
        when(tReq.getRequestURI()).thenReturn(pRequestURI);
        when(tReq.getSession()).thenReturn(pSession);

        return tReq;
    }

    public static HttpServletResponse createResponse(final StringWriter pWriter) throws IOException {
        HttpServletResponse tResponse = mock(HttpServletResponse.class);

        PrintWriter writer = new PrintWriter(pWriter);
        when(tResponse.getWriter()).thenReturn(writer);

        Map<String,Object> tValues = new HashMap<String,Object>();
        when(tResponse.getStatus()).thenAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                return (int)tValues.get("status");
            }
        });

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                Object value = aInvocation.getArguments()[0];
                tValues.put("status", value);
                return null;
            }
        }).when(tResponse).setStatus(anyInt());
        when(tResponse.getContentType()).thenAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                return (String)tValues.get("mime");
            }
        });
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                String value = (String) aInvocation.getArguments()[0];
                tValues.put("mime", value);
                return null;
            }
        }).when(tResponse).setContentType(anyString());

        when(tResponse.getCharacterEncoding()).thenAnswer(new Answer() {
            /**
             * @see org.mockito.stubbing.Answer#answer(org.mockito.invocation.InvocationOnMock)
             */
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                return (String)tValues.get("encoding");
            }
        });
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock aInvocation) throws Throwable {
                String value = (String) aInvocation.getArguments()[0];
                tValues.put("encoding", value);
                return null;
            }
        }).when(tResponse).setCharacterEncoding(anyString());

        return tResponse;
    }
}
