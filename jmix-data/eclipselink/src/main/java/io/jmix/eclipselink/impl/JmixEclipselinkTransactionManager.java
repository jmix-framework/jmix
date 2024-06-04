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

package io.jmix.eclipselink.impl;

import io.jmix.data.impl.JmixTransactionManager;
import jakarta.persistence.EntityManagerFactory;
import org.eclipse.persistence.internal.helper.JmixUtil;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.TransactionDefinition;

public class JmixEclipselinkTransactionManager extends JmixTransactionManager {

    public JmixEclipselinkTransactionManager(String storeName, EntityManagerFactory entityManagerFactory) {
        super(storeName, entityManagerFactory);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        super.setApplicationContext(applicationContext);

        addListener(new JmixEclipselinkTransactionExecutionListener(getKey(), applicationContext));
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        super.doBegin(transaction, definition);
        // set soft deletion at beginning of each new transaction
        JmixUtil.setSoftDeletion(true);
        JmixUtil.setOriginalSoftDeletion(true);
    }
}
