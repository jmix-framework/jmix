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

package io.jmix.core;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import io.jmix.core.impl.NoopStoreDescriptor;
import io.jmix.core.impl.JpaStoreDescriptor;
import io.jmix.core.impl.UndefinedStoreDescriptor;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.StoreDescriptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * Utility class for accessing registered data store names.
 */
@Component("core_Stores")
public class Stores {

    public static final String MAIN = "main";
    public static final String NOOP = "noop";
    public static final String UNDEFINED = "undefined";

    @Autowired
    protected Environment environment;

    @Autowired
    protected JpaStoreDescriptor jpaStoreDescriptor;

    @Autowired
    protected NoopStoreDescriptor noopStoreDescriptor;

    @Autowired
    protected UndefinedStoreDescriptor undefinedStoreDescriptor;

    @Autowired
    protected ApplicationContext applicationContext;

    @Autowired
    protected Map<String, StoreDescriptor> descriptors;

    protected Map<String, Store> stores = new HashMap<>();

    protected static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    @PostConstruct
    protected void initialize() {
        stores.put(UNDEFINED, applicationContext.getBean(Store.class, UNDEFINED, undefinedStoreDescriptor));

        StoreDescriptor mainDescriptor = getStoreDescriptor(MAIN);
        stores.put(MAIN, applicationContext.getBean(Store.class, MAIN, mainDescriptor != null ? mainDescriptor : jpaStoreDescriptor));

        StoreDescriptor noopDescriptor = getStoreDescriptor(NOOP);
        stores.put(NOOP, applicationContext.getBean(Store.class, NOOP, noopDescriptor != null ? noopDescriptor : noopStoreDescriptor));

        for (String storeName : getAdditional()) {
            StoreDescriptor storeDescriptor = getStoreDescriptor(storeName);
            stores.put(storeName, applicationContext.getBean(Store.class, storeName, storeDescriptor != null ? storeDescriptor : jpaStoreDescriptor));
        }
    }

    @Nullable
    protected StoreDescriptor getStoreDescriptor(String storeName) {
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
     * @return true if the given name is the name of the main data store
     */
    public static boolean isMain(String name) {
        return MAIN.equals(name);
    }

    public Store get(String name) {
        Store store = stores.get(name);
        if (store == null) {
            throw new IllegalArgumentException(String.format("Store %s is not registered", name));
        }
        return store;
    }

    /**
     * @return the list of all data store names including main
     * @see #getAdditional()
     */
    public List<String> getAll() {
        List<String> all = new ArrayList<>();
        all.add(MAIN);
        all.addAll(getAdditional());
        return all;
    }

    /**
     * @return the list of additional data store names registered in the {@code jmix.core.additional-stores} property
     */
    public List<String> getAdditional() {
        String property = environment.getProperty("jmix.core.additional-stores");
        if (!Strings.isNullOrEmpty(property))
            return SPLITTER.splitToList(property);
        else
            return Collections.emptyList();
    }
}
