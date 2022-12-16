/*
 * Copyright 2022 Haulmont.
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

package io.jmix.core.metamodel.model.impl;

import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.SessionClassRegistrar;
import io.jmix.core.metamodel.model.SessionClassRegistrars;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("core_SessionClassRegistrars")
public class SessionClassRegistrarsImpl implements SessionClassRegistrars {

    protected List<SessionClassRegistrar> registrars;

    @Autowired
    public SessionClassRegistrarsImpl(List<SessionClassRegistrar> registrars) {
        this.registrars = registrars;
    }

    @Override
    public void registerMetaClass(Session session, MetaClassImpl metaClass) {
        for (SessionClassRegistrar registrar : registrars) {
            if (registrar.supports(session.getClass())) {
                registrar.registerMetaClass(session, metaClass);
                return;
            }
        }
    }

    @Override
    public void registerMetaClass(Session session, String name, Class javaClass, MetaClassImpl metaClass) {
        for (SessionClassRegistrar registrar : registrars) {
            if (registrar.supports(session.getClass())) {
                registrar.registerMetaClass(session, name, javaClass, metaClass);
                return;
            }
        }
        throw new IllegalStateException("No SessionClassRegistrar supporting sessions of " + session.getClass().getName() + " type found");
    }
}
