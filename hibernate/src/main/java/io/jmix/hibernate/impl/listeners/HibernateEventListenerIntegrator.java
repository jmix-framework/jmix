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

package io.jmix.hibernate.impl.listeners;

import org.hibernate.boot.Metadata;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HibernateEventListenerIntegrator implements Integrator {

    @Autowired
    protected HibernateFireEventsEventListener eventListener;

    @Override
    public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
        EventListenerRegistry eventListenerRegistry =
                sessionFactoryServiceRegistry.getService(EventListenerRegistry.class);

        eventListenerRegistry.getEventListenerGroup(EventType.PRE_INSERT).prependListener(eventListener);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_INSERT).prependListener(eventListener);
        eventListenerRegistry.getEventListenerGroup(EventType.PRE_UPDATE).prependListener(eventListener);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_UPDATE).prependListener(eventListener);
        eventListenerRegistry.getEventListenerGroup(EventType.PRE_DELETE).prependListener(eventListener);
        eventListenerRegistry.getEventListenerGroup(EventType.POST_DELETE).prependListener(eventListener);
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactoryImplementor, SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
    }
}