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

import io.jmix.core.CoreProperties;
import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.rest.api.common.RestAuthUtils;
import io.jmix.rest.property.RestProperties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JmixUserAuthenticationProvider implements AuthenticationProvider {

    protected static final String SESSION_ID_DETAILS_ATTRIBUTE = "sessionId";

    private static final Logger log = LoggerFactory.getLogger(JmixUserAuthenticationProvider.class);

    @Inject
    protected Messages messages;

    @Inject
    protected MessageTools messageTools;

    @Inject
    protected RestAuthUtils restAuthUtils;

    @Inject
    protected RestProperties restProperties;

    @Inject
    protected CoreProperties coreProperties;

    private UserDetailsService userDetailsService;

    private PasswordEncoder passwordEncoder;


    public JmixUserAuthenticationProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String ipAddress = request.getRemoteAddr();

        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            if (!restProperties.isStandardAuthenticationEnabled()) {
                log.debug("Standard authentication is disabled. Property cuba.rest.standardAuthenticationEnabled is false");

                throw new InvalidGrantException("Authentication disabled");
            }

            UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) authentication;

            String login = (String) token.getPrincipal();

            //noinspection unchecked
            Map<String, Object> details = (Map<String, Object>) authentication.getDetails();

//            UserSession session;
//            try {
//                LoginPasswordCredentials credentials = new LoginPasswordCredentials(login, (String) token.getCredentials());
//                credentials.setIpAddress(ipAddress);
//                //credentials.setClientType(ClientType.REST_API);
//                credentials.setClientType(new ClientType("REST API"));
//                credentials.setClientInfo(makeClientInfo(request.getHeader(HttpHeaders.USER_AGENT)));
//                //credentials.setSecurityScope(restApiConfig.getSecurityScope());
//                credentials.setParams(details);
//
//                //if the locale value is explicitly passed in the Accept-Language header then set its value to the
//                //credentials. Otherwise, the locale of the user should be used
//                Locale locale = restAuthUtils.extractLocaleFromRequestHeader(request);
//                if (locale != null) {
//                    credentials.setLocale(locale);
//                    credentials.setOverrideLocale(true);
//                } else {
//                    credentials.setOverrideLocale(false);
//                }
//
//                return loginUser(credentials);
////            } catch (AccountLockedException le) {
////                log.info("Blocked user login attempt: login={}, ip={}", login, ipAddress);
////                throw new LockedException("User temporarily blocked");
//            } catch (RestApiAccessDeniedException ex) {
//                log.info("User is not allowed to use the REST API {}", login);
//                throw new BadCredentialsException("User is not allowed to use the REST API");
//            } catch (LoginException e) {
//                log.info("REST API authentication failed: {} {}", login, ipAddress);
//                throw new BadCredentialsException("Bad credentials");
//            }
        }
        return null;
    }

//    protected AuthenticationDetails loginMiddleware(LoginPasswordCredentials credentials) throws LoginException {
//        return authenticationService.login(credentials);
//    }

//    protected SystemAuthenticationToken loginUser(LoginPasswordCredentials loginPasswordCredentials) {
//        String login = loginPasswordCredentials.getName();
//
//        Locale credentialsLocale = loginPasswordCredentials.getLocale() == null ?
//                messageTools.getDefaultLocale() : loginPasswordCredentials.getLocale();
//
//        if (Strings.isNullOrEmpty(login)) {¶
//            // empty login is not valid
//            throw new LoginException(getInvalidCredentialsMessage(login, credentialsLocale));
//        }
//
//        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
//        if (userDetails == null) {
//            throw new LoginException(getInvalidCredentialsMessage(login, credentialsLocale));
//        }
//        // todo PasswordEncryption
////        if (!passwordEncryption.checkPassword(user, credentials.getPassword())) {
////            throw new LoginException(getInvalidCredentialsMessage(login, credentialsLocale));
////        }
//
//        if (!passwordEncoder.matches(loginPasswordCredentials.getPassword(), userDetails.getPassword())) {
//            throw new LoginException(getInvalidCredentialsMessage(login, credentialsLocale));
//        }
//
//        SystemAuthenticationToken result = new SystemAuthenticationToken(userDetails, Collections.emptyList());
//        result.setDetails(loginPasswordCredentials.getParams());
//        return result;
//    }

    protected String getInvalidCredentialsMessage(String login, Locale locale) {
        return messages.formatMessage("", "LoginException.InvalidLoginOrPassword", locale, login);
    }

    protected String makeClientInfo(String userAgent) {
        //noinspection UnnecessaryLocalVariable
        String serverInfo = String.format("REST API (%s:%s/%s) %s",
                coreProperties.getWebHostName(),
                coreProperties.getWebPort(),
                coreProperties.getWebContextName(),
                StringUtils.trimToEmpty(userAgent));

        return serverInfo;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }

    protected List<GrantedAuthority> getRoleUserAuthorities(Authentication authentication) {
        return new ArrayList<>();
    }
}
