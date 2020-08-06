package io.jmix.rest.session;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OAuth2AccessTokenSessionIdResolver implements HttpSessionIdResolver {

    public static final String SESSION_ID = OAuth2AccessTokenSessionIdResolver.class.getSimpleName() + ".SESSION_ID";

    public static final String ACCESS_TOKEN = OAuth2AccessTokenSessionIdResolver.class.getSimpleName() + ".ACCESS_TOKEN";

    @Autowired
    protected TokenStore tokenStore;

    @Autowired
    private ObjectFactory<SessionData> sessionDataFactory;

    @Override
    public List<String> resolveSessionIds(HttpServletRequest request) {
        String tokenValue = fromRequest(request);
        if (tokenValue != null) {
            OAuth2AccessToken token = tokenStore.readAccessToken(tokenValue);
            if (token != null) {
                String sessionId = (String) token.getAdditionalInformation().get(SESSION_ID);
                if (sessionId != null) {
                    return Collections.singletonList(sessionId);
                }
            }
        }
        return Collections.emptyList();
    }

    private String fromRequest(HttpServletRequest request) {
        return (String) request.getAttribute(OAuth2AuthenticationDetails.ACCESS_TOKEN_VALUE);
    }

    @Override
    public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
        String tokenValue = fromRequest(request);
        OAuth2AccessToken token;
        if (tokenValue == null) {
            tokenValue = (String) sessionDataFactory.getObject().getAttribute(ACCESS_TOKEN);
        }
        if (tokenValue != null) {
            token = tokenStore.readAccessToken(tokenValue);
            if (token != null) {
                String originalSessionId = (String) token.getAdditionalInformation().get(SESSION_ID);
                if (!Objects.equals(originalSessionId, sessionId)) {
                    token.getAdditionalInformation().put(SESSION_ID, sessionId);
                    tokenStore.storeAccessToken(token, tokenStore.readAuthentication(token));
                }
            }
        }

    }

    @Override
    public void expireSession(HttpServletRequest request, HttpServletResponse response) {
        String tokenValue = fromRequest(request);
        if (tokenValue != null) {
            OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(tokenValue);
            if (oAuth2AccessToken!=null) {
                tokenStore.removeAccessToken(oAuth2AccessToken);
            }
        }
    }
}
