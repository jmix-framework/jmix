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

package com.haulmont.chile.core.model.impl;

import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.Session;
import io.jmix.core.metamodel.model.SessionClassRegistrar;
import io.jmix.core.metamodel.model.impl.MetaClassImpl;
import io.jmix.core.metamodel.model.impl.SessionImpl;
import org.springframework.stereotype.Component;

public class CubaSessionClassRegistrar implements SessionClassRegistrar {
    @Override
    public boolean supports(Class<? extends Session> sessionClass) {
        return CubaSession.class.isAssignableFrom(sessionClass);
    }

    @Override
    public void registerMetaClass(Session session, MetaClass metaClass) {
        SessionImpl delegateSession = (SessionImpl) ((CubaSession) session).getDelegate();
        delegateSession.registerClass(metaClass);
    }

    @Override
    public void registerMetaClass(Session session, String name, Class javaClass, MetaClass metaClass) {
        SessionImpl delegateSession = (SessionImpl) ((CubaSession) session).getDelegate();
        delegateSession.registerClass(name, javaClass, metaClass);
    }
}
