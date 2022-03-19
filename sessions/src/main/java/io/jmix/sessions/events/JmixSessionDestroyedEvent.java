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

package io.jmix.sessions.events;

import io.jmix.sessions.SessionRepositoryWrapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.session.Session;

import java.util.ArrayList;
import java.util.List;

/**
 * Published by the {@link SessionRepositoryWrapper} before a {@code Session}
 * destroyed by the Spring session repository
 *
 */
public final class JmixSessionDestroyedEvent<S extends Session> extends SessionDestroyedEvent {

    public JmixSessionDestroyedEvent(S source) {
        super(source);
    }

    @SuppressWarnings("unchecked")
    public S getSession() {
        return (S) getSource();
    }

    @Override
    public List<SecurityContext> getSecurityContexts() {
        S session = getSession();

        ArrayList<SecurityContext> contexts = new ArrayList<>();

        for (String attributeName : session.getAttributeNames()) {
            Object attributeValue = session.getAttribute(attributeName);
            if (attributeValue instanceof SecurityContext) {
                contexts.add((SecurityContext) attributeValue);
            }
        }

        return contexts;
    }

    @Override
    public String getId() {
        return getSession().getId();
    }
}
