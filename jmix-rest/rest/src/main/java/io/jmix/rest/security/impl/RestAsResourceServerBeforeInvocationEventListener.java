/*
 * Copyright 2021 Haulmont.
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

package io.jmix.rest.security.impl;

import io.jmix.authserver.event.AsResourceServerBeforeInvocationEvent;
import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.rest.accesscontext.RestAccessContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

/**
 * A listener for {@link AsResourceServerBeforeInvocationEvent} that checks "rest.enabled" specific permission for
 * each REST API request, managed by resource server of Authorization Server add-on. If the current user doesn't have
 * this policy then the FORBIDDEN error is thrown.
 */
public class RestAsResourceServerBeforeInvocationEventListener {
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Messages messages;
    @Autowired
    protected RestAuthorizedUrlsRequestMatcher matcher;

    @EventListener(AsResourceServerBeforeInvocationEvent.class)
    public void doListen(AsResourceServerBeforeInvocationEvent event) {
        if (matcher.isAuthorizedUrl(event.getRequest())) {
            RestAccessContext restAccessContext = new RestAccessContext();
            Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
            try {
                SecurityContextHelper.setAuthentication(event.getAuthentication());
                accessManager.applyRegisteredConstraints(restAccessContext);
            } finally {
                SecurityContextHelper.setAuthentication(currentAuthentication);
            }

            if (!restAccessContext.isPermitted()) {
                event.preventInvocation();
                event.setErrorCode(HttpStatus.FORBIDDEN.value());
                event.setErrorMessage(messages.getMessage("io.jmix.rest/restApiAccessDenied"));
            }
        }
    }
}