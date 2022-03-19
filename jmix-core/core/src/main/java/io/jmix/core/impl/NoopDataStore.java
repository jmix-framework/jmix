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

package io.jmix.core.impl;

import io.jmix.core.DataStore;
import io.jmix.core.LoadContext;
import io.jmix.core.SaveContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.entity.KeyValueEntity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Empty implementation of the {@link DataStore} interface.
 * {@code DataManager} routes here non-JPA entities that are not explicitly associated with any data store.
 */
@Component("core_NoopDataStore")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class NoopDataStore implements DataStore {

    protected String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public Object load(LoadContext<?> context) {
        return null;
    }

    @Override
    public List<Object> loadList(LoadContext<?> context) {
        return Collections.emptyList();
    }

    @Override
    public long getCount(LoadContext<?> context) {
        return 0;
    }

    @Override
    public Set<?> save(SaveContext context) {
        Set<Object> set = new HashSet<>();
        set.addAll(context.getEntitiesToSave());
        set.addAll(context.getEntitiesToRemove());
        return set;
    }

    @Override
    public List<KeyValueEntity> loadValues(ValueLoadContext context) {
        return Collections.emptyList();
    }

    @Override
    public long getCount(ValueLoadContext context) {
        return 0;
    }
}
