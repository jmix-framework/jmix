/*
 * Copyright 2024 Haulmont.
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

package io.jmix.samples.restservice.app;

import io.jmix.core.DataManager;
import io.jmix.rest.annotation.RestMethod;
import io.jmix.rest.annotation.RestService;
import io.jmix.samples.restservice.entity.Customer;
import io.jmix.samples.restservice.entity.CustomerContact;
import org.springframework.beans.factory.annotation.Autowired;

@RestService("CustomerService")
public class CustomerService {

    @Autowired
    private DataManager dataManager;

    @RestMethod
    public CustomerContact getPreferredContact(Customer customer) {
        return dataManager.load(CustomerContact.class)
                .query("e.customer = ?1", customer)
                .list().stream()
                .filter(contact -> Boolean.TRUE.equals(contact.getPreferred()))
                .findAny()
                .orElse(null);
    }
}
