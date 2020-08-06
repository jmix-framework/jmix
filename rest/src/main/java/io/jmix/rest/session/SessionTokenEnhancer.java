package io.jmix.rest.session;

import io.jmix.core.session.SessionData;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.HashMap;

@Component
@Order(500)
public class SessionTokenEnhancer implements TokenEnhancer {

    @Autowired
    private ObjectFactory<SessionData> sessionDataFactory;

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        DefaultOAuth2AccessToken mutableAccessToken = (DefaultOAuth2AccessToken) accessToken;
        HashMap<String, Object> additionalInformation = new HashMap<>(accessToken.getAdditionalInformation());
        mutableAccessToken.setAdditionalInformation(additionalInformation);

        SessionData sessionData = sessionDataFactory.getObject();
        HttpSession session = sessionData.getHttpSession();
        session.setMaxInactiveInterval(accessToken.getExpiresIn());
        sessionData.setAttribute(OAuth2AccessTokenSessionIdResolver.ACCESS_TOKEN, accessToken.getValue());

        mutableAccessToken.getAdditionalInformation().put(OAuth2AccessTokenSessionIdResolver.SESSION_ID, session.getId());
        return accessToken;
    }
}
