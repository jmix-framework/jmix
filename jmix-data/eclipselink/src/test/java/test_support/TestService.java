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

package test_support;

import io.jmix.core.Metadata;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import test_support.entity.multidb.Db1Customer;
import test_support.entity.sec.User;

import org.springframework.beans.factory.annotation.Autowired;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Component
public class TestService {

    @Autowired
    private Metadata metadata;

    @PersistenceContext
    private EntityManager entityManager;

    @PersistenceContext(unitName = "db1")
    private EntityManager db1EntityManager;

    @Transactional
    public User createUser() {
        User user = metadata.create(User.class);
        user.setLogin("user-" + RandomStringUtils.randomAlphabetic(5));
        entityManager.persist(user);
        return user;
    }

    @Transactional("db1TransactionManager")
    public Db1Customer createDb1Customer() {
        Db1Customer customer = metadata.create(Db1Customer.class);
        customer.setName("cust-" + RandomStringUtils.randomAlphabetic(5));
        db1EntityManager.persist(customer);
        return customer;
    }
}
