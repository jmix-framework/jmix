/*
 * Copyright (c) 2008-2018 Haulmont.
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

package com.haulmont.cuba.core.model.sales;

import io.jmix.data.EntityManager;
import io.jmix.data.listener.AfterDeleteEntityListener;
import io.jmix.data.listener.AfterInsertEntityListener;
import io.jmix.data.listener.AfterUpdateEntityListener;
import io.jmix.data.listener.BeforeDetachEntityListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class TestCustomerListener implements
        AfterInsertEntityListener<Customer>,
        AfterUpdateEntityListener<Customer>,
        AfterDeleteEntityListener<Customer>,
        BeforeDetachEntityListener<Customer> {

    public static final List<String> events = new ArrayList<>();

    @Override
    public void onAfterDelete(Customer entity, Connection connection) {
        events.add("onAfterDelete: " + getCurrentTransactionName());
    }

    @Override
    public void onAfterInsert(Customer entity, Connection connection) {
        events.add("onAfterInsert: " + getCurrentTransactionName());
    }

    @Override
    public void onAfterUpdate(Customer entity, Connection connection) {
        events.add("onAfterUpdate: " + getCurrentTransactionName());
    }

    @Override
    public void onBeforeDetach(Customer entity, EntityManager entityManager) {
        events.add("onBeforeDetach: " + getCurrentTransactionName());
    }

    private String getCurrentTransactionName() {
        return TransactionSynchronizationManager.getCurrentTransactionName();
    }
}