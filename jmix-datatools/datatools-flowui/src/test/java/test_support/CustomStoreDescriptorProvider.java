/*
 * Copyright 2026 Haulmont.
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

package test_support;

import io.jmix.core.datastore.AdditionalStoreDescriptorProvider;
import io.jmix.core.metamodel.model.StoreDescriptor;

/**
 * Registers a non-JPA data store, so that the store-based rules of the entity inspector can be
 * checked without a JPA entity. Nothing is ever loaded from it: only its metadata is used.
 */
public class CustomStoreDescriptorProvider implements AdditionalStoreDescriptorProvider {

    public static final String STORE_NAME = "custom";

    @Override
    public String getStoreName() {
        return STORE_NAME;
    }

    @Override
    public StoreDescriptor getStoreDescriptor() {
        return new StoreDescriptor() {
            @Override
            public String getBeanName() {
                return "test_CustomDataStore";
            }

            @Override
            public boolean isJpa() {
                return false;
            }
        };
    }
}
