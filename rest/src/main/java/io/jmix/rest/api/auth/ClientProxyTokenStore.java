/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.rest.api.auth;

import com.google.common.base.Strings;
import io.jmix.core.CoreProperties;
import io.jmix.core.security.LoginException;
import io.jmix.core.security.UserSession;
import io.jmix.core.security.UserSessionManager;
import io.jmix.core.security.UserSessions;
import io.jmix.rest.api.common.RestAuthUtils;
import io.jmix.rest.api.common.RestTokenMasker;
import io.jmix.rest.rest.RestUserSessionInfo;
import io.jmix.rest.rest.ServerTokenStore;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.common.ExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * A token store that redirects request from the client to the {@link ServerTokenStore} located at the middleware.
 */
public class ClientProxyTokenStore implements TokenStore {

    private static final Logger log = LoggerFactory.getLogger(ClientProxyTokenStore.class);

    @Inject
    protected ServerTokenStore serverTokenStore;

    @Inject
    protected CoreProperties coreProperties;

//    @Inject
//    protected RestApiProperties restApiProperties;
    // todo AuthenticationService
//    @Inject
//    protected AuthenticationService authenticationService;
//
//    @Inject
//    protected TrustedClientService trustedClientService;

    @Inject
    protected UserSessionManager userSessionManager;

    protected AuthenticationKeyGenerator authenticationKeyGenerator;

    @Inject
    protected RestAuthUtils restAuthUtils;

    @Inject
    protected RestTokenMasker tokenMasker;

    @Inject
    protected UserSessions userSessions;

    public void setAuthenticationKeyGenerator(AuthenticationKeyGenerator authenticationKeyGenerator) {
        this.authenticationKeyGenerator = authenticationKeyGenerator;
    }

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        OAuth2Authentication authentication = null;
        byte[] authenticationBytes = serverTokenStore.getAuthenticationByTokenValue(token);
        if (authenticationBytes != null) {
            try {
                authentication = deserialize(authenticationBytes);
            } catch (DeserializationException e) {
                log.error("Error on OAuth2Authentication deserialization: {}", e.getMessage());
            }
        }
        if (authentication != null) {
            processSession(authentication, token);
        }
        return authentication;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        String authenticationKey = authenticationKeyGenerator.extractKey(authentication);
        String userLogin = authentication.getName();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        Locale locale = restAuthUtils.extractLocaleFromRequestHeader(request);
        String refreshTokenValue = token.getRefreshToken() != null ? token.getRefreshToken().getValue() : null;
        serverTokenStore.storeAccessToken(token.getValue(),
                serialize(token),
                authenticationKey,
                serialize(authentication),
                token.getExpiration(),
                userLogin,
                locale,
                refreshTokenValue);

        @SuppressWarnings("unchecked")
        Map<String, String> userAuthenticationDetails =
                (Map<String, String>) authentication.getUserAuthentication().getDetails();
        //sessionId parameter was put in the CubaUserAuthenticationProvider
        String sessionIdStr = userAuthenticationDetails.get("sessionId");
        if (!Strings.isNullOrEmpty(sessionIdStr)) {
            UUID sessionId = UUID.fromString(sessionIdStr);
            //Save the RestUserSessionInfo, so the "processSession()" method can find the UserSession associated with the access token.
            //We need to set a proper locale here for the case when a refresh token request comes with the 'Accept-Language' header
            serverTokenStore.putSessionInfo(token.getValue(), new RestUserSessionInfo(sessionId, locale));
        }
        log.info("REST API access token stored: [{}] {}", authentication.getPrincipal(), tokenMasker.maskToken(token.getValue()));
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        OAuth2AccessToken accessToken = null;
        byte[] accessTokenBytes = serverTokenStore.getAccessTokenByTokenValue(tokenValue);
        if (accessTokenBytes != null) {
            try {
                accessToken = deserialize(accessTokenBytes);
            } catch (DeserializationException e) {
                log.error("Error on OAuth2AccessToken deserialization: {}", e.getMessage());
            }
        }
        return accessToken;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        serverTokenStore.removeAccessToken(token.getValue());
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken accessToken = null;
        String key = authenticationKeyGenerator.extractKey(authentication);
        byte[] accessTokenBytes = serverTokenStore.getAccessTokenByAuthentication(key);
        if (accessTokenBytes != null) {
            try {
                accessToken = deserialize(accessTokenBytes);
            } catch (DeserializationException e) {
                log.error("Error on OAuth2AccessToken deserialization: {}", e.getMessage());
            }
        }
        return accessToken;
    }

    /**
     * Tries to find the session associated with the given {@code authentication}. If the session id is in the store and exists then it is set to the
     * {@link SecurityContext}. If the session id is not in the store or the session with the id doesn't exist in the middleware, then the trusted
     * login attempt is performed.
     */
    protected void processSession(OAuth2Authentication authentication, String tokenValue) {
        RestUserSessionInfo sessionInfo = serverTokenStore.getSessionInfoByTokenValue(tokenValue);
        UUID sessionId = sessionInfo != null ? sessionInfo.getId() : null;
        if (sessionId == null) {
            @SuppressWarnings("unchecked")
            Map<String, String> userAuthenticationDetails =
                    (Map<String, String>) authentication.getUserAuthentication().getDetails();
            //sessionId parameter was put in the CubaUserAuthenticationProvider
            String sessionIdStr = userAuthenticationDetails.get("sessionId");
            if (!Strings.isNullOrEmpty(sessionIdStr)) {
                sessionId = UUID.fromString(sessionIdStr);
            }
        }

        UserSession session = null;
        if (sessionId != null) {
            try {
                session = userSessions.getAndRefresh(sessionId);
                //session = trustedClientService.findSession(restApiConfig.getTrustedClientPassword(), sessionId);
            } catch (LoginException e) {
                throw new RuntimeException("Unable to login with trusted client password");
            }
        }

        if (session == null) {
            @SuppressWarnings("unchecked")
            Map<String, String> userAuthenticationDetails =
                    (Map<String, String>) authentication.getUserAuthentication().getDetails();
            String username = userAuthenticationDetails.get("username");

            if (Strings.isNullOrEmpty(username)) {
                throw new IllegalStateException("Empty username extracted from user authentication details");
            }

//            Locale locale = sessionInfo != null ?
//                    sessionInfo.getLocale() : null;
//            TrustedClientCredentials credentials = createTrustedClientCredentials(username, locale);
//            try {
//                session = authenticationService.login(credentials).getSession();
//            } catch (LoginException e) {
//                throw new OAuth2Exception("Cannot login to the middleware", e);
//            }
            log.debug("New session created for token '{}' since the original session has been expired", tokenMasker.maskToken(tokenValue));
        }

        if (session != null) {
            serverTokenStore.putSessionInfo(tokenValue, new RestUserSessionInfo(session));
            //CurrentUserSession.set(session);
        }
    }

//    protected TrustedClientCredentials createTrustedClientCredentials(String username, Locale locale) {
//        TrustedClientCredentials credentials = new TrustedClientCredentials(username,
//                restApiConfig.getTrustedClientPassword(), locale);
//        credentials.setClientType(ClientType.REST_API);
//
//        ServletRequestAttributes attributes =
//                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
//        if (attributes != null) {
//            HttpServletRequest request = attributes.getRequest();
//            credentials.setIpAddress(request.getRemoteAddr());
//            credentials.setClientInfo(makeClientInfo(request.getHeader(HttpHeaders.USER_AGENT)));
//        } else {
//            credentials.setClientInfo(makeClientInfo(""));
//        }
//
//        credentials.setSecurityScope(restApiConfig.getSecurityScope());
//        //if locale was not determined then use the user locale
//        if (locale == null) {
//            credentials.setOverrideLocale(false);
//        }
//        return credentials;
//    }

    protected String makeClientInfo(String userAgent) {
        //noinspection UnnecessaryLocalVariable
        String serverInfo = String.format("REST API (%s:%s/%s) %s",
                coreProperties.getWebHostName(),
                coreProperties.getWebPort(),
                coreProperties.getWebContextName(),
                StringUtils.trimToEmpty(userAgent));

        return serverInfo;
    }

    protected <T> T deserialize(byte[] bytes) throws DeserializationException {
        try {
            return (T) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            log.debug("Error on deserialization", e);
            throw new DeserializationException(e.getMessage(), e);
        }
    }

    protected <T> byte[] serialize(T object) {
        return SerializationUtils.serialize(object);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        Date tokenExpiry = refreshToken instanceof ExpiringOAuth2RefreshToken ?
                ((ExpiringOAuth2RefreshToken) refreshToken).getExpiration() :
                null;
        String userLogin = authentication.getName();
        serverTokenStore.storeRefreshToken(refreshToken.getValue(),
                serialize(refreshToken),
                serialize(authentication),
                tokenExpiry,
                userLogin);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        OAuth2RefreshToken refreshToken = null;
        byte[] refreshTokenBytes = serverTokenStore.getRefreshTokenByTokenValue(tokenValue);
        if (refreshTokenBytes != null) {
            try {
                refreshToken = deserialize(refreshTokenBytes);
            } catch (DeserializationException e) {
                log.error("Error on OAuth2RefreshToken deserialization: {}", e.getMessage());
            }
        }
        return refreshToken;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        OAuth2Authentication authentication = null;
        byte[] authenticationBytes = serverTokenStore.getAuthenticationByRefreshTokenValue(token.getValue());
        if (authenticationBytes != null) {
            try {
                authentication = deserialize(authenticationBytes);
            } catch (DeserializationException e) {
                log.error("Error on OAuth2Authentication deserialization: {}", e.getMessage());
            }
        }
        return authentication;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        serverTokenStore.removeRefreshToken(token.getValue());
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        serverTokenStore.removeAccessTokenUsingRefreshToken(refreshToken.getValue());
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        throw new UnsupportedOperationException();
    }

    protected static class DeserializationException extends Exception {
        public DeserializationException(Throwable throwable) {
            super(throwable);
        }

        public DeserializationException(String s, Throwable throwable) {
            super(s, throwable);
        }
    }
}
