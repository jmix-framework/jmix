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
import io.jmix.core.impl.OrmStoreDescriptor;
import io.jmix.core.impl.UndefinedStoreDescriptor;
import io.jmix.core.metamodel.model.Store;
import io.jmix.core.metamodel.model.StoreDescriptor;
import io.jmix.core.metamodel.model.impl.StoreImpl;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;

/**
 * Utility class for accessing registered data source names.
 */
@Component(Stores.NAME)
public class Stores {

    public static final String NAME = "jmix_Stores";

    public static final String MAIN = "main";
    public static final String NOOP = "noop";
    public static final String UNDEFINED = "undefined";

    @Inject
    protected Environment environment;

    @Inject
    protected OrmStoreDescriptor ormStoreDescriptor;

    @Inject
    protected NoopStoreDescriptor noopStoreDescriptor;

    @Inject
    protected UndefinedStoreDescriptor undefinedStoreDescriptor;

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    protected Map<String, StoreDescriptor> descriptors;

    protected Map<String, Store> stores = new HashMap<>();

    protected static final Splitter SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();

    @PostConstruct
    protected void initialize() {
        stores.put(UNDEFINED, new StoreImpl(UNDEFINED, undefinedStoreDescriptor));

        StoreDescriptor mainDescriptor = getStoreDescriptor(MAIN);
        stores.put(MAIN, new StoreImpl(MAIN, mainDescriptor != null ? mainDescriptor : ormStoreDescriptor));

        StoreDescriptor noopDescriptor = getStoreDescriptor(NOOP);
        stores.put(NOOP, new StoreImpl(NOOP, noopDescriptor != null ? noopDescriptor : noopStoreDescriptor));
    }

    @Nullable
    protected StoreDescriptor getStoreDescriptor(String storeName) {
        String descriptorName = environment.getProperty("jmix." + storeName + "StoreDescriptor");
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
     * @return the list of additional data store names registered in the {@code cuba.additionalStores} app property
     */
    public List<String> getAdditional() {
        String dbProp = environment.getProperty("jmix.additionalStores");
        if (!Strings.isNullOrEmpty(dbProp))
            return SPLITTER.splitToList(dbProp);
        else
            return Collections.emptyList();
    }
}
