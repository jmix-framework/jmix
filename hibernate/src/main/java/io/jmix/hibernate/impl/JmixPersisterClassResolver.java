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

package io.jmix.hibernate.impl;

import org.hibernate.mapping.Collection;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.internal.StandardPersisterClassResolver;
import org.springframework.stereotype.Component;

@Component("hibernate_JmixPersisterClassResolver")
public class JmixPersisterClassResolver extends StandardPersisterClassResolver {

    @Override
    public Class<? extends CollectionPersister> getCollectionPersisterClass(Collection metadata) {
        return metadata.isOneToMany() ? oneToManyPersister() : basicCollectionPersister();
    }

    private Class<OneToManyPersister> oneToManyPersister() {
        return OneToManyPersister.class;
    }

    private Class<BasicCollectionPersister> basicCollectionPersister() {
        return BasicCollectionPersister.class;
    }
}
