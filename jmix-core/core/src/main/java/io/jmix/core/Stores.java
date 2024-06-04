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

import io.jmix.core.impl.StoreDescriptorsRegistry;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.StoreDescriptor;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    protected ApplicationContext applicationContext;

    @Autowired
    protected StoreDescriptorsRegistry descriptorsRegistry;

    protected Map<String, Store> stores = new HashMap<>();

    @PostConstruct
    protected void initialize() {
        StoreDescriptor undefinedStoreDescriptor = descriptorsRegistry.getStoreDescriptor(UNDEFINED);
        stores.put(UNDEFINED, applicationContext.getBean(Store.class, UNDEFINED, undefinedStoreDescriptor));

        StoreDescriptor mainDescriptor = descriptorsRegistry.getStoreDescriptor(MAIN);
        stores.put(MAIN, applicationContext.getBean(Store.class, MAIN, mainDescriptor));

        StoreDescriptor noopDescriptor = descriptorsRegistry.getStoreDescriptor(NOOP);
        stores.put(NOOP, applicationContext.getBean(Store.class, NOOP, noopDescriptor));

        for (String storeName : getAdditional()) {
            StoreDescriptor storeDescriptor = descriptorsRegistry.getStoreDescriptor(storeName);
            stores.put(storeName, applicationContext.getBean(Store.class, storeName, storeDescriptor));
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
        return descriptorsRegistry.getAdditionalDataStoreNames();
    }
}
