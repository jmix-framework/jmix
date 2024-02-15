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

package io.jmix.core.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.metamodel.model.StoreDescriptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.jmix.core.Stores.*;

/**
 * Resolves descriptors of data stores
 */
@Component("core_StoreDescriptorsRegistry")
public class StoreDescriptorsRegistry {

    @Autowired
    protected UndefinedStoreDescriptor undefinedStoreDescriptor;
    @Autowired
    protected JpaStoreDescriptor jpaStoreDescriptor;
    @Autowired
    protected NoopStoreDescriptor noopStoreDescriptor;
    @Autowired
    protected Map<String, StoreDescriptor> descriptors;
    @Autowired
    protected Environment environment;

    protected static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    @PostConstruct
    protected void initialize() {
        initUndefinedStoreDescriptor();
        initMainStoreDescriptor();
        initNoopStoreDescriptor();

        for (String storeName : getAdditionalDataStoreNames()) {
            initAdditionalStoreDescriptor(storeName);
        }
    }

    public StoreDescriptor getStoreDescriptor(String storeName) {
        StoreDescriptor storeDescriptor = descriptors.get(storeName);
        if (storeDescriptor == null) {
            throw new IllegalStateException("Store descriptor not found for store: " + storeName);
        }
        return storeDescriptor;
    }

    protected void initMainStoreDescriptor() {
        initStoreDescriptor(MAIN, jpaStoreDescriptor);
    }

    protected void initUndefinedStoreDescriptor() {
        descriptors.put(UNDEFINED, undefinedStoreDescriptor);
    }

    protected void initNoopStoreDescriptor() {
        initStoreDescriptor(NOOP, noopStoreDescriptor);
    }

    protected void initAdditionalStoreDescriptor(String storeName) {
        initStoreDescriptor(storeName, jpaStoreDescriptor);
    }

    protected void initStoreDescriptor(String storeName, StoreDescriptor defaultStoreDescriptor) {
        StoreDescriptor storeDescriptor = resolveStoreDescriptor(storeName);
        descriptors.put(storeName, storeDescriptor != null ? storeDescriptor : defaultStoreDescriptor);
    }

    @Nullable
    protected StoreDescriptor resolveStoreDescriptor(String storeName) {
        String descriptorName = environment.getProperty("jmix.core.store-descriptor-" + storeName);
        if (descriptorName != null) {
            StoreDescriptor descriptor = descriptors.get(descriptorName);
            if (descriptor != null) {
                return descriptor;
            } else {
                throw new IllegalStateException("Store descriptor not found: " + descriptorName);
            }
        } else {
            return null;
        }
    }

    /**
     * @return the list of additional data store names registered in the {@code jmix.core.additional-stores} property
     */
    public List<String> getAdditionalDataStoreNames() {
        String property = environment.getProperty("jmix.core.additional-stores");
        if (!Strings.isNullOrEmpty(property)) {
            return SPLITTER.splitToList(property);
        } else {
            return Collections.emptyList();
        }
    }
}
