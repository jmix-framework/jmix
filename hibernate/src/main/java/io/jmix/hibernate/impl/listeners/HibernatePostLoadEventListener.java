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

package io.jmix.hibernate.impl.listeners;

import io.jmix.core.Entity;
import io.jmix.hibernate.impl.HibernatePersistenceSupport;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PostLoadEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static io.jmix.core.entity.EntitySystemAccess.getUncheckedEntityEntry;

@Component("hibernate_PostLoadEventListener")
public class HibernatePostLoadEventListener implements PostLoadEventListener {

    private static final long serialVersionUID = -2671560013482964425L;

    @Autowired
    protected HibernatePersistenceSupport persistenceSupport;

    @Override
    public void onPostLoad(PostLoadEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof Entity) {
            getUncheckedEntityEntry(entity).setNew(false);
        }
        EventSource eventSource = event.getSession();
        persistenceSupport.registerInstance(entity, eventSource);
    }
}
