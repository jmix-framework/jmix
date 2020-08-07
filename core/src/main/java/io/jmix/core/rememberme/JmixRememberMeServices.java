/*
 * Copyright 2020 Haulmont.
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

package io.jmix.core.rememberme;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class JmixRememberMeServices extends PersistentTokenBasedRememberMeServices {

    public JmixRememberMeServices(String key, UserDetailsService userDetailsService, PersistentTokenRepository tokenRepository) {
        super(key, userDetailsService, tokenRepository);
    }

    @Override
    protected boolean rememberMeRequested(HttpServletRequest request, String parameter) {
        String rememberMe = request.getAttribute(parameter) != null ? String.valueOf(request.getAttribute(parameter)) : request.getParameter(parameter);

        if (rememberMe != null) {
            if (rememberMe.equalsIgnoreCase("true") || rememberMe.equalsIgnoreCase("on")
                    || rememberMe.equalsIgnoreCase("yes") || rememberMe.equals("1")) {
                return true;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Did not send remember-me cookie (principal did not set parameter '"
                    + parameter + "')");
        }

        return false;
    }

    @Override
    protected void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, Authentication successfulAuthentication) {
        super.onLoginSuccess(request, response, successfulAuthentication);
        enhanceSession(request.getSession());
    }

    protected void enhanceSession(HttpSession session) {
        session.setMaxInactiveInterval(getTokenValiditySeconds());
    }
}
