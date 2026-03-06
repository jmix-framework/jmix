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

package io.jmix.reportsrest.security.event;

import io.jmix.oidc.resourceserver.OidcResourceServerBeforeInvocationEvent;
import org.springframework.context.event.EventListener;

/**
 * A listener for {@link OidcResourceServerBeforeInvocationEvent} that checks "reports.rest.enabled" specific policy
 * for /rest/reports/** requests, managed by resource server of the OIDC add-on. If the current user
 * doesn't have this policy then the FORBIDDEN error is thrown.
 */
public class ReportOidcResourceServerBeforeInvocationEventListener extends AbstractReportBeforeInvocationEventListener {

    @EventListener(OidcResourceServerBeforeInvocationEvent.class)
    public void doListen(OidcResourceServerBeforeInvocationEvent event) {
        if (!checkAccess(event.getRequest(), event.getAuthentication())) {
            event.preventInvocation();
            event.setErrorCode(getForbiddenErrorCode());
        }
    }
}
