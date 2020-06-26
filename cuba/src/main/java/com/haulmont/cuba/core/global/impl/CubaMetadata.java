/*
 * Copyright 2019 Haulmont.
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

package com.haulmont.cuba.core.global.impl;

import com.haulmont.chile.core.model.Session;
import com.haulmont.chile.core.model.impl.CubaSession;
import com.haulmont.cuba.core.global.Metadata;
import com.haulmont.cuba.core.global.ViewRepository;
import io.jmix.core.ExtendedEntities;
import io.jmix.core.MetadataTools;
import io.jmix.core.impl.MetadataLoader;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;

import org.springframework.beans.factory.annotation.Autowired;

public class CubaMetadata extends io.jmix.core.impl.MetadataImpl implements Metadata {

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected ViewRepository viewRepository;

    @Autowired
    protected ExtendedEntities extendedEntities;

    public CubaMetadata(MetadataLoader metadataLoader) {
        super(metadataLoader);
    }

    @Override
    public Session getSession() {
        return new CubaSession(super.getSession());
    }

    @Override
    public MetaClass getClassNN(String name) {
        return getSession().getClassNN(name);
    }

    @Override
    public MetaClass getClassNN(Class<?> javaClass) {
        return getSession().getClassNN(javaClass);
    }

    @Override
    public ViewRepository getViewRepository() {
        return viewRepository;
    }

    @Override
    public ExtendedEntities getExtendedEntities() {
        return extendedEntities;
    }

    @Override
    public MetadataTools getTools() {
        return tools;
    }

    @Override
    public DatatypeRegistry getDatatypes() {
        return datatypeRegistry;
    }

}
