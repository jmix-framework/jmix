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

package com.haulmont.cuba.core.sys;

import com.haulmont.chile.core.datatypes.DatatypeRegistry;
import io.jmix.core.metamodel.datatype.Datatype;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.Set;

@Component(DatatypeRegistry.NAME)
public class DatatypeRegistryImpl implements DatatypeRegistry {

    @Autowired
    private io.jmix.core.metamodel.datatype.DatatypeRegistry registry;

    @Override
    public Datatype get(String id) {
        return registry.get(id);
    }

    @Nullable
    @Override
    public <T> Datatype<T> get(Class<T> javaClass) {
        return registry.find(javaClass);
    }

    @Override
    public <T> Datatype<T> getNN(Class<T> javaClass) {
        return registry.get(javaClass);
    }

    @Override
    public String getId(Datatype datatype) {
        return registry.getId(datatype);
    }

    @Override
    public String getIdByJavaClass(Class<?> javaClass) {
        return registry.getIdByJavaClass(javaClass);
    }

    @Override
    public Set<String> getIds() {
        return registry.getIds();
    }

    @Override
    public void register(Datatype datatype, String id, boolean defaultForJavaClass) {
        registry.register(datatype, id, defaultForJavaClass);
    }
}
