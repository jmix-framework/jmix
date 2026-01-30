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

package io.jmix.samples.rest.service;

import com.google.common.base.Strings;
import io.jmix.core.*;
import io.jmix.samples.rest.entity.sales.Customer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomerService implements SaveDelegate<Customer>, RemoveDelegate<Customer> {

    private final DataManager dataManager;
    private final EntityStates entityStates;

    public CustomerService(DataManager dataManager, EntityStates entityStates) {
        this.dataManager = dataManager;
        this.entityStates = entityStates;
    }

    @Override
    public Customer save(Customer entity, SaveContext saveContext) {
        if (entityStates.isNew(entity)) {
            entity.setComments("New customer created at " + LocalDateTime.now() + "\n");
        } else {
            if (!entityStates.isLoaded(entity, "comments")) {
                entity = dataManager.load(Id.of(entity)).one();
            }
            entity.setComments(Strings.nullToEmpty(entity.getComments()) + "Customer updated at " + LocalDateTime.now() + "\n");
        }
        return SaveDelegate.save(dataManager, entity, saveContext);
    }

    @Override
    public void remove(Customer entity) {
        if (!entityStates.isLoaded(entity, "comments")) {
            entity = dataManager.load(Id.of(entity)).one();
        }
        entity.setComments(Strings.nullToEmpty(entity.getComments()) + "Customer removed at " + LocalDateTime.now() + "\n");
        dataManager.save(entity);
    }
}
