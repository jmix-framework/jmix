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

package io.jmix.audit.entity;

import io.jmix.core.annotation.Internal;
import io.jmix.core.entity.annotation.SystemLevel;
import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.core.metamodel.annotation.JmixProperty;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.session.SessionInformation;

import jakarta.persistence.Transient;
import java.util.Date;

@JmixEntity(name = "audit_UserSession", annotatedPropertiesOnly = true)
@SystemLevel
@Internal
public class UserSession {

    @Transient
    protected SessionInformation sessionInformation;

    public UserSession(SessionInformation sessionInformation) {
        this.sessionInformation = sessionInformation;
    }

    public SessionInformation getSessionInformation() {
        return sessionInformation;
    }

    public Object getPrincipal() {
        return sessionInformation.getPrincipal();
    }

    @JmixProperty
    public String getSessionId() {
        return sessionInformation.getSessionId();
    }

    @JmixProperty
    public String getPrincipalName() {
        return new TestingAuthenticationToken(getPrincipal(), null).getName();
    }

    @JmixProperty
    public Date getLastRequest() {
        return sessionInformation.getLastRequest();
    }

}
