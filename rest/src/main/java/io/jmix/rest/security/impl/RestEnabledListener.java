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

package io.jmix.rest.security.impl;

import io.jmix.core.AccessManager;
import io.jmix.core.Messages;
import io.jmix.core.security.ClientDetails;
import io.jmix.core.security.SecurityContextHelper;
import io.jmix.rest.context.RestEnabledContext;
import io.jmix.rest.exception.RestApiAccessDeniedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class RestEnabledListener {
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Messages messages;

    @EventListener
    void checkRestEnabled(AuthenticationSuccessEvent event) {
        Authentication authentication = event.getAuthentication();

        if (authentication.getDetails() instanceof ClientDetails) {
            ClientDetails clientDetails = (ClientDetails) authentication.getDetails();

            if ("REST".equals(clientDetails.getClientType())) {
                RestEnabledContext restEnabledContext = new RestEnabledContext();

                Authentication currentAuthentication = SecurityContextHelper.getAuthentication();
                try {
                    SecurityContextHelper.setAuthentication(authentication);
                    accessManager.applyRegisteredConstraints(restEnabledContext);
                } finally {
                    SecurityContextHelper.setAuthentication(currentAuthentication);
                }

                if (!restEnabledContext.isPermitted()) {
                    throw new RestApiAccessDeniedException(messages.getMessage("io.jmix.rest/restApiAccessDenied"));
                }
            }
        }
    }

}
