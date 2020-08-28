package io.jmix.rest.session;

import io.jmix.core.session.SessionData;
import io.jmix.rest.security.RestAuthDetails;
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
import java.util.Map;

/**
 * A token enhancer that provides session info details
 * for {@link OAuth2AccessToken} and {@link OAuth2Authentication}
 **/
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

        authentication.setDetails(RestAuthDetails.builder()
                .sessionId(session.getId())
                .accessToken(accessToken.getValue())
                .refreshToken(accessToken.getRefreshToken().getValue())
                .build());
        return accessToken;
    }
}
