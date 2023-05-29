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

package io.jmix.eclipselink.impl;

import io.jmix.core.MetadataTools;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.spi.PersistenceUnitInfo;
import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.beans.factory.ListableBeanFactory;

import java.util.Map;

public class JmixPersistenceProvider extends PersistenceProvider {

    private ListableBeanFactory beanFactory;
    private MetadataTools metadataTools;

    public JmixPersistenceProvider(ListableBeanFactory beanFactory, MetadataTools metadataTools) {
        this.beanFactory = beanFactory;
        this.metadataTools = metadataTools;
    }

    @Override
    public EntityManagerFactory createContainerEntityManagerFactory(PersistenceUnitInfo info, Map properties) {
        EntityManagerFactory entityManagerFactory = super.createContainerEntityManagerFactory(info, properties);
        return new JmixEntityManagerFactory(entityManagerFactory, beanFactory, metadataTools);
    }
}
